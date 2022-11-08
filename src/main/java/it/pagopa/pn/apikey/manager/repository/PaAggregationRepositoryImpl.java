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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@Slf4j
@Component
public class PaAggregationRepositoryImpl implements PaAggregationRepository {

    private final DynamoDbAsyncTable<PaAggregationModel> table;
    private final String gsiAggregateId;

    public PaAggregationRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                       @Value("${pn.apikey.manager.dynamodb.pa-aggregations.gsi-name.aggregate-id}") String gsiAggregateId,
                                       @Value("${pn.apikey.manager.dynamodb.tablename.pa-aggregations}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PaAggregationModel.class));
        this.gsiAggregateId = gsiAggregateId;
    }

    @Override
    public Mono<PaAggregationModel> searchAggregation(String xPagopaPnCxId) {
        Key key = Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .doOnNext(s -> log.info("aggregation found: {}",s.toString()));
    }

    @Override
    public Mono<PaAggregationModel> savePaAggregation(PaAggregationModel toSave) {
        return Mono.fromFuture(table.putItem(toSave)).thenReturn(toSave);
    }

    @Override
    public Mono<Page<PaAggregationModel>> findByAggregateId(String aggregateId, Integer limit, String lastKey) {
        Key key = Key.builder()
                .partitionValue(aggregateId)
                .build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(lastKey != null ? Map.of("aggregateId", AttributeValue.builder().s(lastKey).build()) : null)
                .limit(limit)
                .build();
        return Mono.from(table.index(gsiAggregateId).query(queryEnhancedRequest));
    }
}
