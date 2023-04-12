package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PaAggregationRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;


    @Test
    void getAllWithFilter() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paAggregationRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
        StepVerifier.create(paAggregationRepository.getAllPageableWithFilter(new PaAggregationPageable(10,"id"),"paName"))
                .expectNextCount(0);
    }

    @Test
    void searchAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaId("id");
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> paAggregationModel);
        when(dynamoDbAsyncTable.getItem((Key)any())).thenReturn(completableFuture);

        StepVerifier.create(paRepository.searchAggregation("id"))
                .expectNext(paAggregationModel).verifyComplete();
    }

    @Test
    void savePaAggregation() {
        when(dynamoDbAsyncTable.tableName()).thenReturn("tableName");
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem(paAggregationModel)).thenReturn(completableFuture);

        StepVerifier.create(paRepository.savePaAggregation(paAggregationModel))
                .expectNext(paAggregationModel)
                .verifyComplete();
    }

   /* @Test
    void savePaAggregationList() {
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        BatchWriteResult batchWriteResult = BatchWriteResult.builder().unprocessedRequests(new HashMap<>()).build();

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        StepVerifier.create(paRepository.savePaAggregation(List.of(paAggregationModel)))
                .expectNext(batchWriteResult).verifyComplete();
    }

    @Test
    void batchGetItemTest() {
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);

        BatchGetResultPagePublisher batchGetResultPagePublisher = mock(BatchGetResultPagePublisher.class);
        when(dynamoDbEnhancedAsyncClient.batchGetItem((BatchGetItemEnhancedRequest) any())).thenReturn(batchGetResultPagePublisher);

       PaDetailDto paDetailDto = new PaDetailDto();
       paDetailDto.setName("name");
       paDetailDto.setId("id");
        AddPaListRequestDto addPaListRequestDto = new AddPaListRequestDto();
        addPaListRequestDto.setItems(List.of(paDetailDto));
        StepVerifier.create(paRepository.batchGetItem(addPaListRequestDto)).verifyComplete();
    }

    @Test
    void testSavePaAggregation() {
        when(dynamoDbAsyncTable.tableName()).thenReturn("tableName");
        TableSchema tableSchema = mock(TableSchema.class);
        when(dynamoDbAsyncTable.tableSchema()).thenReturn(tableSchema);
        DynamoDbEnhancedClientExtension extension = mock(DynamoDbEnhancedClientExtension.class);
        when(dynamoDbAsyncTable.mapperExtension()).thenReturn(extension);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        CompletableFuture<BatchWriteResult> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> BatchWriteResult.builder().build());
        when(dynamoDbEnhancedAsyncClient.batchWriteItem((BatchWriteItemEnhancedRequest) any()))
                .thenReturn(completableFuture);

        PaAggregationModel model = new PaAggregationModel();
        model.setPaId("id");
        StepVerifier.create(paRepository.savePaAggregation(List.of(model)))
                .verifyComplete();
    }*/

    @Test
    void getAllPaAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan()).thenReturn(pagePublisher);

        StepVerifier.create(paRepository.getAllPaAggregations())
                .expectNextCount(0);
    }

    @Test
    void findByAggregateId() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest)any())).thenReturn(pagePublisher);

        PaAggregationPageable pageable = PaAggregationPageable.builder()
                .limit(10)
                .lastEvaluatedKey("id")
                .build();
        StepVerifier.create(paRepository.findByAggregateId("id", pageable))
                .expectNextCount(0);
    }

    @Test
    void testCount(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        StepVerifier.create(paRepository.countByAggregateId("id")).expectNext(0);
    }
}
