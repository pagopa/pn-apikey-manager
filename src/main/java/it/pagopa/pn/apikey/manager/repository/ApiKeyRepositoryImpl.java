package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_DOES_NOT_EXISTS;

@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    private final DynamoDbAsyncTable<ApiKeyModel> table;
    private final String gsiLastUpdate;

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

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(startKey)
                .filterExpression(expression)
                .scanIndexForward(false)
                .limit(pageable.getLimit())
                .build();

        if (pageable.hasLimit()) {
            return Mono.from(table.index(gsiLastUpdate).query(queryEnhancedRequest));
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
            expressionGroup.append("(").append(expressionGroup.substring(0, expressionGroup.length() - 2)).append(")");
        } else {
            expressionGroup.append("attribute_exists(" + ApiKeyConstant.GROUPS + ")");
        }
        return expressionGroup.toString();
    }
}
