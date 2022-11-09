package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

@Component
public class AggregateRepositoryImpl implements AggregateRepository {

    private final DynamoDbAsyncTable<ApiKeyAggregateModel> table;

    public AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dynamodb.tablename.aggregates}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(ApiKeyAggregateModel.class));
    }

    @Override
    public Mono<Page<ApiKeyAggregateModel>> findAll(AggregatePageable pageable) {
        Map<String, AttributeValue> attributeValue = new HashMap<>();
        if (pageable.isPageable()) {
            attributeValue.put(AggregationConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .exclusiveStartKey(attributeValue.isEmpty() ? null : attributeValue)
                .limit(pageable.getLimit())
                .build();
        return Mono.from(table.scan(scanEnhancedRequest));
    }

    @Override
    public Mono<Page<ApiKeyAggregateModel>> findByName(String name, AggregatePageable pageable) {
        Map<String, AttributeValue> attributeValue = new HashMap<>();
        if (pageable.isPageable()) {
            attributeValue.put(AggregationConstant.PK, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }

        Map<String, AttributeValue> expressionValue = new HashMap<>();
        expressionValue.put(":name", AttributeValue.builder().s(name).build());

        // FIXME trasformare aggregateName to name (Nota -> name è una reserved word)
        Expression expression = Expression.builder()
                .expression("begins_with(aggregateName, :name)")
                .expressionValues(expressionValue)
                .build();

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .exclusiveStartKey(attributeValue.isEmpty() ? null : attributeValue)
                .limit(pageable.getLimit())
                .build();

        return Mono.from(table.scan(scanEnhancedRequest));
    }

    @Override
    public Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel toSave) {
        return Mono.fromFuture(table.putItem(toSave)).thenReturn(toSave);
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
}
