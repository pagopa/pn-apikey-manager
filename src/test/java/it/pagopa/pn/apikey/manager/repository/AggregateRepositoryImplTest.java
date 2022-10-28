package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Mockito.when(dynamoDbAsyncTable.putItem(apiKeyAggregation)).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.saveAggregation(apiKeyAggregation))
                .expectNext(apiKeyAggregation);
    }

    @Test
    void getApiKeyAggregation(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn( dynamoDbAsyncTable);
        AggregateRepositoryImpl aggregateRepository = new AggregateRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyAggregation);
        Mockito.when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregateRepository.getApiKeyAggregation("42"))
                .expectNext(apiKeyAggregation).verifyComplete();
    }
}
