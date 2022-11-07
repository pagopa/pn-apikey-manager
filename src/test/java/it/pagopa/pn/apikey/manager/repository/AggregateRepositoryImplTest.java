package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
@ExtendWith(SpringExtension.class)
class AggregateRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void saveAggregation() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        Mockito.when(dynamoDbAsyncTable.putItem((ApiKeyAggregateModel)any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.saveAggregation(apikeyAggregateModel))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }

    @Test
    void getApiKeyAggregation(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn( dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apikeyAggregateModel);
        Mockito.when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.getApiKeyAggregation("42"))
                .expectNext(apikeyAggregateModel).verifyComplete();
    }
}
