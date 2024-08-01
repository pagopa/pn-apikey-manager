package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
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

    private AggregateRepositoryImpl aggregateRepository;

    @BeforeEach
    void setup() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any(TableSchema.class))).thenReturn(dynamoDbAsyncTable);
        aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
    }

    @Test
    void saveAggregation() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem((ApiKeyAggregateModel) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.saveAggregation(apikeyAggregateModel))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void getApiKeyAggregation() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.getApiKeyAggregation("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void deleteAggregation() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        when(dynamoDbAsyncTable.deleteItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.delete("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void findAllAggregation() {
        ApiKeyAggregateModel entity1 = new ApiKeyAggregateModel();
        entity1.setAggregateId("id1");
        ApiKeyAggregateModel entity2 = new ApiKeyAggregateModel();
        entity2.setAggregateId("id2");

        List<ApiKeyAggregateModel> entities = Arrays.asList(entity1, entity2);
        TestUtilsRepository.mockScanEnhancedRequestToRetrievePage(dynamoDbAsyncTable, entities);

        StepVerifier.create(aggregateRepository.findAll(new AggregatePageable(1,"id", "")))
                .expectNextMatches(p -> p.items().size() == 1)
                .verifyComplete();
    }

    @Test
    void findByNameAggregation() {
        ApiKeyAggregateModel entity1 = new ApiKeyAggregateModel();
        entity1.setAggregateId("id1");
        ApiKeyAggregateModel entity2 = new ApiKeyAggregateModel();
        entity2.setAggregateId("id2");

        List<ApiKeyAggregateModel> entities = Arrays.asList(entity1, entity2);

        TestUtilsRepository.mockScanEnhancedRequestToRetrievePage(dynamoDbAsyncTable, entities);

        StepVerifier.create(aggregateRepository.findByName("test", new AggregatePageable(10,"id", "")))
                .expectNextMatches(p -> p.items().size() == 2)
                .verifyComplete();
    }


    @Test
    void findById() {
        CompletableFuture<ApiKeyAggregateModel> completableFuture = new CompletableFuture<>();
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setAggregateId("id");
        completableFuture.completeAsync(() -> apiKeyAggregateModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.findById("id")).expectNext(apiKeyAggregateModel).verifyComplete();
    }

    @Test
    void testCount(){
        PagePublisher<ApiKeyAggregateModel> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
        StepVerifier.create(aggregateRepository.count()).expectNext(0);
    }

    @Test
    void testCountByName(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");
        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        PagePublisher<Object> pagePublisher = mock(PagePublisher.class);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
        StepVerifier.create(aggregateRepository.countByName("name")).expectNext(0);
    }
}
