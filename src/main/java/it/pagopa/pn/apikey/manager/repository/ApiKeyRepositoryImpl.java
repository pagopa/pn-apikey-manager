package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.utils.QueryUtils;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_DOES_NOT_EXISTS;
import static it.pagopa.pn.apikey.manager.utils.QueryUtils.expressionBuilder;

@lombok.CustomLog
@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    private final DynamoDbAsyncTable<ApiKeyModel> table;
    private final String gsiLastUpdate;

    private static final int MIN_LIMIT = 100;
    private static final int EXPRESSION_GROUP = 2;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                @Value("${pn.apikey.manager.dynamodb.apikey.gsi-name.pa-id}") String gsiLastUpdate,
                                @Value("${pn.apikey.manager.dynamodb.tablename.apikey}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(ApiKeyModel.class));
        this.gsiLastUpdate = gsiLastUpdate;
    }

    @Override
    public Mono<ApiKeyModel> changePdnd(String id, boolean flagPdnd) {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId(id);
        apiKeyModel.setPdnd(flagPdnd);

        return Mono.fromFuture(table.updateItem(createUpdateItemEnhancedRequest(apiKeyModel)));
    }

    @Override
    public Mono<String> delete(String key) {
        return Mono.fromFuture(table.deleteItem(Key.builder().partitionValue(key).build()))
                .map(ApiKeyModel::getId);
    }

    @Override
    public Mono<ApiKeyModel> save(ApiKeyModel apiKeyModel) {
        log.debug("Inserting data {} in DynamoDB table {}", apiKeyModel, table);
        return Mono.fromFuture(table.putItem(apiKeyModel))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}", table))
                .thenReturn(apiKeyModel);
    }

    @Override
    public Mono<List<ApiKeyModel>> setNewVirtualKey(List<ApiKeyModel> apiKeyModels, String virtualKey) {
        return Flux.fromIterable(apiKeyModels)
                .flatMap(apiKeyModel -> {
                    apiKeyModel.setVirtualKey(virtualKey);
                    return Mono.fromFuture(table.updateItem(apiKeyModel));
                })
                .collectList();
    }

    @Override
    public Mono<ApiKeyModel> findById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(APIKEY_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<List<ApiKeyModel>> findByCxId(String xPagopaPnCxId, String scope) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build());

        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#scope", "scope");
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":scope", AttributeValue.builder().s(scope).build());


        Expression expression = Expression.builder()
                .expression("#scope = :scope")
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
                .build();

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(expression)
                .scanIndexForward(false)
                .build();

        return Flux.from(table.index(gsiLastUpdate).query(queryEnhancedRequest).flatMapIterable(Page::items))
                .collectList();
    }

    @Override
    public Mono<Page<ApiKeyModel>> findByCxIdAndStatusRotateAndEnabled(String xPagopaPnCxId) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#status", "status");
        expressionNames.put("#scope", "scope");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":statusEnabled", AttributeValue.builder().s("ENABLED").build());
        expressionValues.put(":statusRotated", AttributeValue.builder().s("ROTATED").build());
        expressionValues.put(":scope", AttributeValue.builder().s(ApiKeyModel.Scope.APIKEY.name()).build());

        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(expressionBuilder("(#status = :statusEnabled OR #status = :statusRotated) AND #scope = :scope", expressionValues, expressionNames))
                .scanIndexForward(false)
                .build();

        return Flux.from(table.index(gsiLastUpdate).query(queryEnhancedRequest).flatMapIterable(Page::items))
                .collectList()
                .map(Page::create);
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
        Map<String, String> expressionNames = new HashMap<>();
        Expression expression = Expression.builder()
                .expression(buildExpressionGroupFilter(xPagopaPnCxGroups, expressionValues, expressionNames))
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
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
                        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
                        if (page.lastEvaluatedKey() != null) {
                            lastEvaluatedKey = new HashMap<>(page.lastEvaluatedKey());
                        }
                        if (cumulativeQueryResult.size() <= pageable.getLimit() && page.lastEvaluatedKey() != null) {
                            ApiKeyPageable newPageable = QueryUtils.getNewPageable(page, pageable);
                            log.trace("get new page with pageable {}", newPageable);
                            return getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, cumulativeQueryResult, newPageable);
                        }
                        List<ApiKeyModel> result = QueryUtils.adjustPageResult(cumulativeQueryResult, pageable, lastEvaluatedKey);
                        lastEvaluatedKey = lastEvaluatedKey.isEmpty() ? null : lastEvaluatedKey;
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
        Map<String, String> expressionNames = new HashMap<>();
        Expression expression = Expression.builder()
                .expression(buildExpressionGroupFilter(xPagopaPnCxGroups, expressionValues, expressionNames))
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
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

    private String buildExpressionGroupFilter(List<String> xPagopaPnCxGroups, Map<String, AttributeValue> expressionValues, Map<String, String> expressionNames) {
        StringBuilder expressionGroup = new StringBuilder();
        if (xPagopaPnCxGroups != null && !xPagopaPnCxGroups.isEmpty()) {
            for (int i = 0; i < xPagopaPnCxGroups.size(); i++) {
                AttributeValue pnCxGroup = AttributeValue.builder().s(xPagopaPnCxGroups.get(i)).build();
                expressionValues.put(":group" + i, pnCxGroup);
                expressionGroup.append(" contains(" + ApiKeyConstant.GROUPS + ",:group").append(i).append(") OR");
            }
            expressionGroup.replace(expressionGroup.length() - EXPRESSION_GROUP, expressionGroup.length(), "");
        } else {
            expressionGroup.append("attribute_exists(" + ApiKeyConstant.GROUPS + ")");
        }

        expressionGroup.append(" AND #scope = :scope");
        expressionValues.put(":scope", AttributeValue.builder().s(ApiKeyModel.Scope.APIKEY.name()).build());
        expressionNames.put("#scope", "scope");


        return expressionGroup.toString();
    }

    private UpdateItemEnhancedRequest<ApiKeyModel> createUpdateItemEnhancedRequest(ApiKeyModel apiKeyModel) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#id", "id");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":id", AttributeValue.builder().s(apiKeyModel.getId()).build());

        return UpdateItemEnhancedRequest
                .builder(ApiKeyModel.class)
                .conditionExpression(expressionBuilder("#id = :id", expressionValues, expressionNames))
                .item(apiKeyModel)
                .ignoreNulls(true)
                .build();
    }

    @Override
    public Mono<Page<ApiKeyModel>> getVirtualKeys(String xPagopaPnUid,
                                                  String xPagopaPnCxId,
                                                  List<ApiKeyModel> cumulativeQueryResult,
                                                  ApiKeyPageable pageable,
                                                  boolean admin) {

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
                .filterExpression(getExpressionFilter(xPagopaPnUid, admin))
                .scanIndexForward(false)
                .limit(pageable.getLimit())
                .build();

        return Mono.from(table.index(gsiLastUpdate).query(queryEnhancedRequest))
                .flatMap(page -> {
                    cumulativeQueryResult.addAll(page.items());
                    Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
                    if (page.lastEvaluatedKey() != null) {
                        lastEvaluatedKey = new HashMap<>(page.lastEvaluatedKey());
                    }
                    if (cumulativeQueryResult.size() <= pageable.getLimit() && page.lastEvaluatedKey() != null) {
                        ApiKeyPageable newPageable = QueryUtils.getNewPageable(page, pageable);
                        log.trace("get new page with pageable {}", newPageable);
                        return getVirtualKeys(xPagopaPnUid, xPagopaPnCxId, cumulativeQueryResult, newPageable, admin);
                    }
                    List<ApiKeyModel> result = QueryUtils.adjustPageResult(cumulativeQueryResult, pageable, lastEvaluatedKey);
                    lastEvaluatedKey = lastEvaluatedKey.isEmpty() ? null : lastEvaluatedKey;
                    return Mono.just(Page.create(result, lastEvaluatedKey));
                });
    }

    @Override
    public Mono<Integer> countWithFilters(String xPagopaPnUid, String xPagopaPnCxId, boolean admin) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(getExpressionFilter(xPagopaPnUid, admin))
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(gsiLastUpdate).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    private Expression getExpressionFilter(String xPagopaPnUid, boolean admin) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        Map<String, String> expressionNames = new HashMap<>();
        StringBuilder filterExpression = new StringBuilder();

        if (!admin) {
            filterExpression.append("#uid = :uid AND ");
            expressionNames.put("#uid", "x-pagopa-pn-uid");
            expressionValues.put(":uid", AttributeValue.builder().s(xPagopaPnUid).build());
        }

        filterExpression.append("#scope = :scope AND (#status = :statusEnabled OR #status = :statusRotated)");
        expressionNames.put("#scope", "scope");
        expressionNames.put("#status", "status");
        expressionValues.put(":scope", AttributeValue.builder().s(ApiKeyModel.Scope.CLIENTID.name()).build());
        expressionValues.put(":statusEnabled", AttributeValue.builder().s(VirtualKeyStatusDto.ENABLED.name()).build());
        expressionValues.put(":statusRotated", AttributeValue.builder().s(VirtualKeyStatusDto.ROTATED.name()).build());

        return Expression.builder()
                .expression(filterExpression.toString())
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
                .build();
    }

    @Override
    public Mono<Page<ApiKeyModel>> findByUidAndCxIdAndStatusAndScope(String uid, String cxId, String status, String scope) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#status", "status");
        expressionNames.put("#scope", "scope");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":status", AttributeValue.builder().s(status).build());
        expressionValues.put(":scope", AttributeValue.builder().s(scope).build());

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(uid).sortValue(cxId)
                        .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(expressionBuilder("(#status = :status AND #scope = :scope)", expressionValues, expressionNames))
                .build();

        return Flux.from(table.index(ApiKeyConstant.GSI_UID_CXID).query(queryEnhancedRequest).flatMapIterable(Page::items))
                .collectList()
                .map(Page::create);
    }

}
