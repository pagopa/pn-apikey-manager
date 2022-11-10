package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AddPaListRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.PaDetailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PaAggregationModelRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

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
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem(paAggregationModel)).thenReturn(completableFuture);

        StepVerifier.create(paRepository.savePaAggregation(paAggregationModel))
                .expectNext(paAggregationModel).verifyComplete();
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
    }*/

    @Test
    void getAllPaAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        Mockito.when(dynamoDbAsyncTable.scan()).thenReturn(pagePublisher);

        StepVerifier.create(paRepository.getAllPaAggregations())
                .expectNextCount(0);
    }

    @Test
    void findByAggregateId() {
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationRepositoryImpl paRepository = new PaAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        Mockito.when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest)any())).thenReturn(pagePublisher);

        StepVerifier.create(paRepository.findByAggregateId("id",10,"id"))
                .expectNextCount(0);
    }
}
