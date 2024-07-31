package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AggregateRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<ApiKeyAggregateModel> dynamoDbAsyncTable;

    @Test
    void saveAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
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
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.getApiKeyAggregation("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void deleteAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.deleteItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.delete("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void findAllAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        SdkPublisher<Page<ApiKeyAggregateModel>> sdkPublisher = mock(SdkPublisher.class);
        doAnswer(invocation -> {
            ApiKeyAggregateModel entity1 = new ApiKeyAggregateModel();
            entity1.setAggregateId("id1");
            ApiKeyAggregateModel entity2 = new ApiKeyAggregateModel();
            entity2.setAggregateId("id2");

            List<ApiKeyAggregateModel> entities = Arrays.asList(entity1, entity2);
            Page<ApiKeyAggregateModel> page = Page.create(entities, null);

            Subscriber<? super Page<ApiKeyAggregateModel>> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if (n != 0) {
                        subscriber.onNext(page);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() { }
            });
            return null;
        }).when(sdkPublisher).subscribe((Subscriber<? super Page<ApiKeyAggregateModel>>) any());

        PagePublisher<ApiKeyAggregateModel> pagePublisher = PagePublisher.create(sdkPublisher);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);


        StepVerifier.create(aggregateRepository.findAll(new AggregatePageable(10,"id", "")))
                .expectNextCount(0);
    }

    @Test
    void findByNameAggregation() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        SdkPublisher<Page<ApiKeyAggregateModel>> sdkPublisher = mock(SdkPublisher.class);
        doAnswer(invocation -> {
            ApiKeyAggregateModel entity1 = new ApiKeyAggregateModel();
            entity1.setAggregateId("id1");
            ApiKeyAggregateModel entity2 = new ApiKeyAggregateModel();
            entity2.setAggregateId("id2");

            List<ApiKeyAggregateModel> entities = Arrays.asList(entity1, entity2);
            Page<ApiKeyAggregateModel> page = Page.create(entities, null);

            Subscriber<? super Page<ApiKeyAggregateModel>> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if (n != 0) {
                        subscriber.onNext(page);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() { }
            });
            return null;
        }).when(sdkPublisher).subscribe((Subscriber<? super Page<ApiKeyAggregateModel>>) any());

        PagePublisher<ApiKeyAggregateModel> pagePublisher = PagePublisher.create(sdkPublisher);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);


        StepVerifier.create(aggregateRepository.findByName("test", new AggregatePageable(10,"id", "")))
                .expectNextCount(0);
    }


    @Test
    void findById() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setAggregateId("id");
        completableFuture.completeAsync(() -> apiKeyAggregateModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.findById("id")).expectNext(apiKeyAggregateModel).verifyComplete();
    }

    @Test
    void testCount(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        PagePublisher<ApiKeyAggregateModel> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
        StepVerifier.create(aggregateRepository.count()).expectNext(0);
    }

    @Test
    void testCountByName(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        SdkPublisher<Page<ApiKeyAggregateModel>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<ApiKeyAggregateModel> index = mock(DynamoDbAsyncIndex.class);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        StepVerifier.create(aggregateRepository.countByName("name")).expectNext(0);
    }
}
