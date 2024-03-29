package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.constant.PaAggregationConstant;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.AddPaListRequestDto;
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
    private final String gsiPageablePaName;

    public static final int MAX_BATCH_SIZE = 25;

    public PaAggregationRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                       @Value("${pn.apikey.manager.dynamodb.pa-aggregations.gsi-name.aggregate-id}") String gsiAggregateId,
                                       @Value("${pn.apikey.manager.dynamodb.pa-aggregations.gsi-name.pageable-pa-name}") String gsiPageablePaName,
                                       @Value("${pn.apikey.manager.dynamodb.tablename.pa-aggregations}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PaAggregationModel.class));
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.gsiAggregateId = gsiAggregateId;
        this.gsiPageablePaName = gsiPageablePaName;
    }

    @Override
    public Mono<Page<PaAggregationModel>> getAllPa(PaPageable pageable) {
        Map<String, AttributeValue> attributeValue = null;
        if (pageable.isPage()) {
            attributeValue = new HashMap<>();
            attributeValue.put(PaAggregationConstant.PA_ID, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
        }
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .exclusiveStartKey(attributeValue)
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
    public Mono<Page<PaAggregationModel>> getAllPaByPaName(PaPageable pageable, String paName) {
        Map<String, AttributeValue> attributeValue = null;
        if (pageable.isPageByName()) {
            attributeValue = new HashMap<>();
            attributeValue.put(PaAggregationConstant.PA_ID, AttributeValue.builder().s(pageable.getLastEvaluatedId()).build());
            attributeValue.put(PaAggregationConstant.PA_NAME, AttributeValue.builder().s(pageable.getLastEvaluatedName()).build());
            attributeValue.put(PaAggregationConstant.PAGEABLE, AttributeValue.builder().s(AggregationConstant.PAGEABLE_VALUE).build());
        }

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue(AggregationConstant.PAGEABLE_VALUE)
                .sortValue(paName)
                .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(attributeValue)
                .limit(pageable.getLimit())
                .build();

        if (pageable.hasLimit()) {
            return Mono.from(table.index(gsiPageablePaName).query(queryEnhancedRequest));
        } else {
            return Flux.from(table.index(gsiPageablePaName).query(queryEnhancedRequest).flatMapIterable(Page::items))
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
    public Mono<Integer> countByName(String name) {
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(Key.builder()
                .partitionValue(PaAggregationConstant.PAGEABLE_VALUE)
                .sortValue(name)
                .build());

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .addAttributeToProject(PaAggregationConstant.PA_ID)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        return Flux.from(table.index(gsiPageablePaName).query(queryEnhancedRequest))
                .doOnNext(page -> counter.getAndAdd(page.items().size()))
                .then(Mono.defer(() -> Mono.just(counter.get())));
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
        log.debug("Inserting data {} in DynamoDB table {}",toSave,table);
        return Mono.fromFuture(table.putItem(toSave))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
                .thenReturn(toSave);
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
                    log.debug("Inserting data {} in DynamoDB table {}",chunk,table);
                    return chunk
                            .doOnNext(builder::addPutItem)
                            .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
                            .then(deferred);
                });
    }

    @Override
    public Mono<Page<PaAggregationModel>> getAllPaAggregations() {
        return getAllPa(PaPageable.builder().build());
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
    public Flux<BatchGetResultPage> batchGetItem(AddPaListRequestDto addPaListRequestDto) {
        log.info("List of PaAggreggationModel in AddPaListRequestDto size: {}", addPaListRequestDto.getItems().size());
        return Flux.fromIterable(addPaListRequestDto.getItems())
                .window(MAX_BATCH_SIZE)
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
