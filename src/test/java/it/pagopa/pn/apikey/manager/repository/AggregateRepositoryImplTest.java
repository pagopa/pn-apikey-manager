package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AggregateRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void saveAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem((ApiKeyAggregateModel) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.saveAggregation(apikeyAggregateModel))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void getApiKeyAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.getApiKeyAggregation("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void deleteAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.deleteItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.delete("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void findAllAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
        StepVerifier.create(aggregateRepository.findAll(new AggregatePageable(10,"id")))
                .expectNextCount(0);
    }

    @Test
    void findByName() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        StepVerifier.create(aggregateRepository.findByName("name", new AggregatePageable(10,"id")))
                .expectNextCount(0);
    }

}
