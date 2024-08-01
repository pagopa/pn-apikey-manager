package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.model.PnLastEvaluatedKey;
import it.pagopa.pn.apikey.manager.model.ResultPaginationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Component
@lombok.CustomLog
public class AggregateRepositoryImpl extends BaseRepository<ApiKeyAggregateModel> implements AggregateRepository {

    private final String gsiName;

    private static final Function<ApiKeyAggregateModel, PnLastEvaluatedKey> APIKEY_AGGREGATE_MODEL_KEY_MAKER = (ApiKeyAggregateModel item) -> {
        PnLastEvaluatedKey pnLastEvaluatedKey = new PnLastEvaluatedKey();
        pnLastEvaluatedKey.setExternalLastEvaluatedKey(item.getAggregateId());
        pnLastEvaluatedKey.setInternalLastEvaluatedKey(Map.of(
                AggregationConstant.PK, AttributeValue.builder().s(item.getAggregateId()).build()));
        return pnLastEvaluatedKey;
    };

    public AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dynamodb.tablename.aggregates}") String tableName,
                                   @Value("${pn.apikey.manager.dynamodb.aggregations.gsi-name.aggregate-name}") String gsiName) {
        super(dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(ApiKeyAggregateModel.class)));
        this.gsiName = gsiName;
    }

    @Override
    public Mono<Page<ApiKeyAggregateModel>> findAll(AggregatePageable pageable) {
        Map<String, AttributeValue> attributeValue = null;
        if (pageable.isPage()) {
            attributeValue = new HashMap<>();
            attributeValue.put(AggregationConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .exclusiveStartKey(attributeValue)
                .limit(pageable.getLimit())
                .build();
        if (pageable.hasLimit()) {
            ResultPaginationDto<ApiKeyAggregateModel> resultPaginationDto = new ResultPaginationDto<>();
            return scanByFilterPaginated(scanEnhancedRequest, resultPaginationDto, pageable.getLimit(), attributeValue, APIKEY_AGGREGATE_MODEL_KEY_MAKER);
        } else {
            return Flux.from(table.scan(scanEnhancedRequest).items())
                    .collectList()
                    .map(Page::create);
        }
    }

    @Override
    public Mono<Integer> count() {
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .addAttributeToProject(AggregationConstant.PK)
                .build();
        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.scan(scanEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    @Override
    public Mono<Page<ApiKeyAggregateModel>> findByName(String name, AggregatePageable pageable) {
        Map<String, AttributeValue> lastEvaluatedKey = null;
        if (pageable.isPage()) {
            lastEvaluatedKey = new HashMap<>();
            lastEvaluatedKey.put(AggregationConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }

        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#searchterm", AggregationConstant.SEARCHTERM);

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder().expression("contains(#searchterm, :name)")
                        .expressionNames(expressionNames)
                        .putExpressionValue(":name", AttributeValue.builder().s(name.toLowerCase()).build())
                        .build())
                .limit(pageable.getLimit())
                .build();

        if (pageable.hasLimit()) {
            ResultPaginationDto<ApiKeyAggregateModel> resultPaginationDto = new ResultPaginationDto<>();
            return scanByFilterPaginated(scanEnhancedRequest, resultPaginationDto, pageable.getLimit(), lastEvaluatedKey, APIKEY_AGGREGATE_MODEL_KEY_MAKER);
        } else {
            return Flux.from(table.scan(scanEnhancedRequest).items())
                    .collectList()
                    .map(Page::create);
        }

    }

    @Override
    public Mono<Integer> countByName(String name) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#searchterm", AggregationConstant.SEARCHTERM);

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("contains(#searchterm, :name)")
                        .expressionNames(expressionNames)
                        .putExpressionValue(":name", AttributeValue.builder().s(name.toLowerCase()).build())
                        .build())
                .addAttributeToProject(AggregationConstant.PK)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.scan(scanEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    @Override
    public Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel toSave) {
        log.debug("Inserting data {} in DynamoDB table {}", toSave, table);
        toSave.setSearchterm(toSave.getName() != null ? toSave.getName().toLowerCase() : "");
        return Mono.fromFuture(table.putItem(toSave))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}", table))
                .thenReturn(toSave);
    }

    @Override
    public Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregateId) {
        Key key = Key.builder()
                .partitionValue(aggregateId)
                .build();
        return Mono.fromFuture(table.getItem(key));
    }

    @Override
    public Mono<ApiKeyAggregateModel> delete(String aggregateId) {
        Key key = Key.builder()
                .partitionValue(aggregateId)
                .build();
        return Mono.fromFuture(table.deleteItem(key));
    }

    @Override
    public Mono<ApiKeyAggregateModel> findById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        return Mono.fromFuture(table.getItem(key));
    }
}
