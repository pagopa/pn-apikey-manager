package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_DOES_NOT_EXISTS;

@Slf4j
@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    private final DynamoDbAsyncTable<ApiKeyModel> table;
    private final String gsiLastUpdate;

    private static final int MIN_LIMIT = 100;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                @Value("${pn.apikey.manager.dynamodb.apikey.gsi-name.pa-id}") String gsiLastUpdate,
                                @Value("${pn.apikey.manager.dynamodb.tablename.apikey}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(ApiKeyModel.class));
        this.gsiLastUpdate = gsiLastUpdate;
    }

    @Override
    public Mono<String> delete(String key) {
        return Mono.fromFuture(table.deleteItem(Key.builder().partitionValue(key).build()))
                .map(ApiKeyModel::getId);
    }

    @Override
    public Mono<ApiKeyModel> save(ApiKeyModel apiKeyModel) {
        return Mono.fromFuture(table.putItem(apiKeyModel)).thenReturn(apiKeyModel);
    }

    @Override
    public Mono<ApiKeyModel> findById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();

        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(APIKEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    @Override
    public Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, ApiKeyPageable pageable) {
        return getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, new ArrayList<>(), pageable);
    }

    private Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId,
                                                     List<String> xPagopaPnCxGroups,
                                                     List<ApiKeyModel> cumulativeQueryResult,
                                                     ApiKeyPageable pageable) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        Expression expression = Expression.builder()
                .expression(buildExpressionGroupFilter(xPagopaPnCxGroups, expressionValues))
                .expressionValues(expressionValues)
                .build();

        Map<String, AttributeValue> startKey = null;
        if (pageable.isPage()) {
            startKey = new HashMap<>();
            startKey.put(ApiKeyConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedKey()).build());
            startKey.put(ApiKeyConstant.LAST_UPDATE, AttributeValue.builder().s(pageable.getLastEvaluatedLastUpdate()).build());
            startKey.put(ApiKeyConstant.PA_ID, AttributeValue.builder().s(xPagopaPnCxId).build());
        }

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        Integer limit = pageable.getLimit();
        if (limit != null && limit < MIN_LIMIT) {
            limit = MIN_LIMIT;
        }
        log.debug("limit from pageable {} - actual limit {}", pageable.getLimit(), limit);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(startKey)
                .filterExpression(expression)
                .scanIndexForward(false)
                .limit(limit)
                .build();

        if (pageable.hasLimit()) {
            return Mono.from(table.index(gsiLastUpdate).query(queryEnhancedRequest))
                    .flatMap(page -> {
                        cumulativeQueryResult.addAll(page.items());
                        Map<String, AttributeValue> lastEvaluatedKey = null;
                        if (page.lastEvaluatedKey() != null) {
                            lastEvaluatedKey = new HashMap<>(page.lastEvaluatedKey());
                        }
                        if (cumulativeQueryResult.size() <= pageable.getLimit() && page.lastEvaluatedKey() != null) {
                            ApiKeyPageable newPageable = QueryUtils.getNewPageable(page, pageable);
                            log.trace("get new page with pageable {}", newPageable);
                            return getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, cumulativeQueryResult, newPageable);
                        }
                        List<ApiKeyModel> result = QueryUtils.adjustPageResult(cumulativeQueryResult, pageable, lastEvaluatedKey);
                        return Mono.just(Page.create(result, lastEvaluatedKey));
                    });
        } else {
            return Flux.from(table.index(gsiLastUpdate).query(queryEnhancedRequest).flatMapIterable(Page::items))
                    .collectList()
                    .map(Page::create);
        }
    }

    @Override
    public Mono<Integer> countWithFilters(String xPagopaPnCxId, List<String> xPagopaPnCxGroups) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        Expression expression = Expression.builder()
                .expression(buildExpressionGroupFilter(xPagopaPnCxGroups, expressionValues))
                .expressionValues(expressionValues)
                .build();

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(expression)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(gsiLastUpdate).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    private String buildExpressionGroupFilter(List<String> xPagopaPnCxGroups, Map<String, AttributeValue> expressionValues) {
        StringBuilder expressionGroup = new StringBuilder();
        if (xPagopaPnCxGroups != null && !xPagopaPnCxGroups.isEmpty()) {
            for (int i = 0; i < xPagopaPnCxGroups.size(); i++) {
                AttributeValue pnCxGroup = AttributeValue.builder().s(xPagopaPnCxGroups.get(i)).build();
                expressionValues.put(":group" + i, pnCxGroup);
                expressionGroup.append(" contains(" + ApiKeyConstant.GROUPS + ",:group").append(i).append(") OR");
            }
            expressionGroup.replace(expressionGroup.length() - 2, expressionGroup.length(), "");
        } else {
            expressionGroup.append("attribute_exists(" + ApiKeyConstant.GROUPS + ")");
        }
        return expressionGroup.toString();
    }
}
