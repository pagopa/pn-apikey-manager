package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class PaRepositoryImpl implements PaRepository{

    private final DynamoDbAsyncTable<PaAggregation> table;

    public PaRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("pn-paAggregations", TableSchema.fromBean(PaAggregation.class));
    }
    @Override
    public Mono<String> searchAggregation(String xPagopaPnCxId) {
        Key key = Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build();

        return Mono.fromFuture(table.getItem(key).thenApply(PaAggregation::getAggregationId));
    }
}
