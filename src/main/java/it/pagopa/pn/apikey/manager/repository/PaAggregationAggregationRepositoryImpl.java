package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Slf4j
@Component
public class PaAggregationAggregationRepositoryImpl implements PaAggregationRepository {

    private final DynamoDbAsyncTable<PaAggregationModel> table;

    public PaAggregationAggregationRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                                  @Value("${pn.apikey.manager.dynamodb.tablename.pa-aggregations}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PaAggregationModel.class));
    }

    @Override
    public Mono<PaAggregationModel> searchAggregation(String xPagopaPnCxId) {
        Key key = Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build();
        return Mono.fromFuture(table.getItem(key).thenApply(paAggregation -> paAggregation))
                .doOnNext(s -> log.info("aggregation found: {}",s.toString()));
    }

    @Override
    public Mono<PaAggregationModel> savePaAggregation(PaAggregationModel toSave) {
        return Mono.fromFuture(table.putItem(toSave).thenApply(r -> toSave));
    }
}
