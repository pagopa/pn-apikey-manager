package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
@Slf4j
public class PaRepositoryImpl implements PaRepository{

    private final DynamoDbAsyncTable<PaAggregation> table;

    public PaRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("pn-paAggregations", TableSchema.fromBean(PaAggregation.class));
    }
    @Override
    public Mono<PaAggregation> searchAggregation(String xPagopaPnCxId) {

        Key key = Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build();

        return Mono.fromFuture(table.getItem(key).thenApply(paAggregation -> paAggregation))
                .doOnNext(s -> log.info("aggregation found: {}",s.toString()));

    }

    @Override
    public Mono<PaAggregation> savePaAggregation(PaAggregation toSave) {
        return Mono.fromFuture(table.putItem(toSave).thenApply(r -> toSave));
    }
}
