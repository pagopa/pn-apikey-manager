package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@lombok.CustomLog
public class AggregateRepositoryImpl implements AggregateRepository {

    private final DynamoDbAsyncTable<ApiKeyAggregateModel> table;
    private final String gsiName;

    public AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dynamodb.tablename.aggregates}") String tableName,
                                   @Value("${pn.apikey.manager.dynamodb.aggregations.gsi-name.aggregate-name}") String gsiName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(ApiKeyAggregateModel.class));
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
            return Mono.from(table.scan());
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
        Map<String, AttributeValue> attributeValue = null;
        if (pageable.isPage()) {
            attributeValue = new HashMap<>();
            attributeValue.put(AggregationConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }

        Map<String,String> expressionNames = new HashMap<>();
        expressionNames.put("#name", AggregationConstant.NAME);

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
            .exclusiveStartKey(attributeValue)
            .filterExpression(Expression.builder().expression("contains(#name, :name)")
                .expressionNames(expressionNames)
                .putExpressionValue(":name", AttributeValue.builder().s(name).build())
                .build())
            .limit(pageable.getLimit())
            .build();
        if (pageable.hasLimit()) {
            return Mono.from(table.scan(scanEnhancedRequest));
        } else {
            return Flux.from(table.scan(scanEnhancedRequest).items())
                .collectList()
                .map(Page::create);
        }

    }

    @Override
    public Mono<Integer> countByName(String name) {
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue(AggregationConstant.PAGEABLE_VALUE)
                .sortValue(name)
                .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .addAttributeToProject(AggregationConstant.PK)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(gsiName).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    @Override
    public Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel toSave) {
        log.debug("Inserting data {} in DynamoDB table {}",toSave,table);
        return Mono.fromFuture(table.putItem(toSave))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
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
    public Mono<ApiKeyAggregateModel> findById(String id){
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        return Mono.fromFuture(table.getItem(key));
    }
}
