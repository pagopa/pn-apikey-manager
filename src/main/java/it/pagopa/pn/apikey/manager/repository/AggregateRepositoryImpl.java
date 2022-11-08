package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class AggregateRepositoryImpl implements AggregateRepository {

    private final DynamoDbAsyncTable<ApiKeyAggregation> table;

    public AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("pn-aggregates", TableSchema.fromBean(ApiKeyAggregation.class));
    }

    @Override
    public Mono<ApiKeyAggregation> saveAggregation(ApiKeyAggregation toSave) {
        return Mono.fromFuture(table.putItem(toSave)).thenReturn(toSave);
    }

    @Override
    public Mono<ApiKeyAggregation> getApiKeyAggregation(String aggregationId) {
        Key key = Key.builder()
                .partitionValue(aggregationId)
                .build();
        return Mono.fromFuture(table.getItem(key));
    }
}
