package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.PaAggregationConstant;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AddPaListRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class PaAggregationRepositoryImpl implements PaAggregationRepository {

    private final DynamoDbAsyncTable<PaAggregationModel> table;
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient;
    private final String gsiAggregateId;

    public static final int MAX_BATCH_SIZE = 25;

    public PaAggregationRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                       @Value("${pn.apikey.manager.dynamodb.pa-aggregations.gsi-name.aggregate-id}") String gsiAggregateId,
                                       @Value("${pn.apikey.manager.dynamodb.tablename.pa-aggregations}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PaAggregationModel.class));
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.gsiAggregateId = gsiAggregateId;
    }

    @Override
    public Mono<PaAggregationModel> searchAggregation(String xPagopaPnCxId) {
        Key key = Key.builder()
                .partitionValue(xPagopaPnCxId)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .doOnNext(s -> log.info("aggregation found: {}", s.toString()));
    }

    @Override
    public Mono<PaAggregationModel> savePaAggregation(PaAggregationModel toSave) {
        return Mono.fromFuture(table.putItem(toSave)).thenReturn(toSave);
    }

    @Override
    public Flux<BatchWriteResult> savePaAggregation(List<PaAggregationModel> toSave) {
        log.info("List of PaAggreggationModel size: {}", toSave.size());
        return Flux.fromIterable(toSave)
                .window(MAX_BATCH_SIZE)
                .flatMap(chunk -> {
                    WriteBatch.Builder<PaAggregationModel> builder = WriteBatch.builder(PaAggregationModel.class)
                            .mappedTableResource(table);
                    Mono<BatchWriteResult> deferred = Mono.defer(() ->
                            Mono.fromFuture(dynamoDbEnhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest.builder()
                                    .writeBatches(builder.build())
                                    .build())));
                    return chunk
                            .doOnNext(builder::addPutItem)
                            .then(deferred);
                });
    }

    @Override
    public Mono<Page<PaAggregationModel>> getAllPaAggregations() {
        return Mono.from(table.scan());
    }

    @Override
    public Mono<Page<PaAggregationModel>> findByAggregateId(String aggregateId, PaAggregationPageable pageable) {
        Map<String, AttributeValue> attributeValue = null;
        if (pageable.isPage()) {
            attributeValue = new HashMap<>();
            attributeValue.put(PaAggregationConstant.AGGREGATE_ID, AttributeValue.builder().s(pageable.getLastEvaluatedKey()).build());
        }
        Key key = Key.builder()
                .partitionValue(aggregateId)
                .build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(attributeValue)
                .limit(pageable.getLimit())
                .build();
        if (pageable.isPage()) {
            return Mono.from(table.index(gsiAggregateId).query(queryEnhancedRequest));
        } else {
            log.debug("executing non paged query");
            return Flux.from(table.index(gsiAggregateId).query(queryEnhancedRequest).flatMapIterable(Page::items))
                    .collectList()
                    .map(Page::create);
        }
    }

    @Override
    public Mono<Integer> countByAggregateId(String aggregateId) {
        Key key = Key.builder()
                .partitionValue(aggregateId)
                .build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(gsiAggregateId).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
    }

    @Override
    public Flux<BatchGetResultPage>  batchGetItem(AddPaListRequestDto addPaListRequestDto) {
        log.info("List of PaAggreggationModel in AddPaListRequestDto size: {}", addPaListRequestDto.getItems().size());
        return Flux.fromIterable(addPaListRequestDto.getItems())
                .window(25)
                .flatMap(chunk -> {
                    ReadBatch.Builder<PaAggregationModel> builder = ReadBatch.builder(PaAggregationModel.class)
                            .mappedTableResource(table);
                    Mono<BatchGetResultPage> deferred = Mono.defer(() ->
                            Mono.from(dynamoDbEnhancedClient.batchGetItem(BatchGetItemEnhancedRequest.builder()
                                    .readBatches(builder.build())
                                    .build())));
                    return chunk
                            .doOnNext(paDetailDto -> builder.addGetItem(Key.builder().partitionValue(paDetailDto.getId()).build()))
                            .then(deferred);
                });
    }
}
