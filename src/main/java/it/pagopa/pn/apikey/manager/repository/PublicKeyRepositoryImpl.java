package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.utils.QueryUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.PUBLIC_KEY_DOES_NOT_EXISTS;
import static it.pagopa.pn.apikey.manager.utils.QueryUtils.expressionBuilder;

@lombok.CustomLog
@Component
public class PublicKeyRepositoryImpl implements PublicKeyRepository {
    private final DynamoDbAsyncTable<PublicKeyModel> table;

    public PublicKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   PnApikeyManagerConfig pnApikeyManagerConfig) {
        this.table = dynamoDbEnhancedClient.table(pnApikeyManagerConfig.getDao().getPublicKeyTableName(), TableSchema.fromBean(PublicKeyModel.class));
    }

    @Override
    public Mono<PublicKeyModel> updateItemStatus(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus) {
        return Mono.fromFuture(table.updateItem(createUpdateItemEnhancedRequest(publicKeyModel, invalidStartedStatus)));
    }

    @Override
    public Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId) {
        Key key = Key.builder()
                .partitionValue(kid)
                .sortValue(cxId)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(PUBLIC_KEY_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND)));

    }

    private UpdateItemEnhancedRequest<PublicKeyModel> createUpdateItemEnhancedRequest(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#status", "status");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        StringBuilder expressionBuilder = new StringBuilder();
        IntStream.range(0, invalidStartedStatus.size()).forEach(idx -> {
            expressionValues.put(":" + invalidStartedStatus.get(idx), AttributeValue.builder().s(invalidStartedStatus.get(idx)).build());
            if (idx == invalidStartedStatus.size() - 1) {
                expressionBuilder.append("#status <> :").append(invalidStartedStatus.get(idx));
            } else {
                expressionBuilder.append("#status <> :").append(invalidStartedStatus.get(idx)).append(" AND ");
            }
        });

        return UpdateItemEnhancedRequest
                .builder(PublicKeyModel.class)
                .conditionExpression(expressionBuilder(expressionBuilder.toString(), expressionValues, expressionNames))
                .item(publicKeyModel)
                .ignoreNulls(true)
                .build();
    }

    @Override
    public Flux<PublicKeyModel> findByCxIdAndStatus(String xPagopaPnCxId, String status) {
        Key.Builder keyBuilder = Key.builder().partitionValue(xPagopaPnCxId);
        if (status != null) {
            keyBuilder.sortValue(status);
        }
        Key key = keyBuilder.build();

        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        QueryEnhancedRequest.Builder qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);

        return Flux.from(this.table.index(PublicKeyModel.GSI_CXID_STATUS).query(qeRequest.build()).flatMapIterable(Page::items));
    }

    @Override
    public Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel) {
        log.debug("Inserting data {} in DynamoDB table {}", publicKeyModel, table);
        return Mono.fromFuture(table.putItem(publicKeyModel))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}", table))
                .thenReturn(publicKeyModel);
    }


    @Override
    public Mono<Page<PublicKeyModel>> getAllWithFilterPaginated(String xPagopaPnCxId,
                                                                PublicKeyPageable pageable,
                                                                List<PublicKeyModel> cumulativeQueryResult) {
        Map<String, AttributeValue> startKey = null;
        if (pageable.isPage()) {
            startKey = new HashMap<>();
            startKey.put(PublicKeyConstant.KID, AttributeValue.builder().s(pageable.getLastEvaluatedKey()).build());
            startKey.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s(pageable.getCreatedAt()).build());
            startKey.put(PublicKeyConstant.CXID, AttributeValue.builder().s(xPagopaPnCxId).build());
        }

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        log.debug("limit from pageable {}", pageable.getLimit());
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(startKey)
                .filterExpression(getFilterExpressionForGetAllQuery())
                .scanIndexForward(false)
                .limit(pageable.getLimit())
                .build();

        return Mono.from(table.index(PublicKeyModel.GSI_CXID_CREATEDAT).query(queryEnhancedRequest))
                .flatMap(page -> {
                    cumulativeQueryResult.addAll(page.items());
                    Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
                    if (page.lastEvaluatedKey() != null) {
                        lastEvaluatedKey = new HashMap<>(page.lastEvaluatedKey());
                    }
                    if (cumulativeQueryResult.size() <= pageable.getLimit() && page.lastEvaluatedKey() != null) {
                        PublicKeyPageable newPageable = QueryUtils.getNewPageable(page, pageable);
                        log.trace("get new page with pageable {}", newPageable);
                        return getAllWithFilterPaginated(xPagopaPnCxId, newPageable, cumulativeQueryResult);
                    }
                    List<PublicKeyModel> result = QueryUtils.adjustPageResult(cumulativeQueryResult, pageable, lastEvaluatedKey);
                    lastEvaluatedKey = lastEvaluatedKey.isEmpty() ? null : lastEvaluatedKey;
                    return Mono.just(Page.create(result, lastEvaluatedKey));
                });

    }

    @Override
    public Mono<Integer> countWithFilters(String xPagopaPnCxId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(getFilterExpressionForGetAllQuery())
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(PublicKeyModel.GSI_CXID_CREATEDAT).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    @NotNull
    private static Expression getFilterExpressionForGetAllQuery() {
        Map<String, String> names = new HashMap<>();
        names.put("#ttl", PublicKeyModel.COL_TTL);
        names.put("#status", PublicKeyModel.COL_STATUS);

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":ACTIVE", AttributeValue.builder().s("ACTIVE").build());
        values.put(":ROTATED", AttributeValue.builder().s("ROTATED").build());
        values.put(":BLOCKED", AttributeValue.builder().s("BLOCKED").build());

        return Expression.builder()
                .expression("attribute_not_exists(#ttl) AND (#status = :ACTIVE OR #status = :ROTATED OR #status = :BLOCKED)")
                .expressionNames(names)
                .expressionValues(values)
                .build();
    }

    @Override
    public Mono<Page<PublicKeyModel>> findByCxIdAndWithoutTtl(String xPagopaPnCxId) {

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#ttl", "ttl");

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(Expression.builder().expression("attribute_not_exists(#ttl)").expressionNames(expressionNames).build())
                .scanIndexForward(false)
                .build();

        return Mono.from(table.index(PublicKeyModel.GSI_CXID_STATUS)
                .query(queryEnhancedRequest));
    }

}
