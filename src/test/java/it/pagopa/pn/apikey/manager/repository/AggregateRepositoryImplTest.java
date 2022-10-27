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
    DynamoDbAsyncTable<ApiKeyAggregation> apiKeyAggregationDynamoDbAsyncTable;

    @Test
    void saveAggregation() throws NoSuchFieldException, IllegalAccessException {
        AggregateRepositoryImpl aggregationRepository = new AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = AggregateRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(aggregationRepository, apiKeyAggregationDynamoDbAsyncTable);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Mockito.when(apiKeyAggregationDynamoDbAsyncTable.putItem(apiKeyAggregation)).thenReturn(completableFuture);
        StepVerifier.create(aggregationRepository.saveAggregation(apiKeyAggregation))
                .expectNext(apiKeyAggregation);
    }

    @Test
    void getApiKeyAggregation() throws IllegalAccessException, NoSuchFieldException {

        AggregateRepositoryImpl aggregationRepository = new AggregateRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = AggregateRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(aggregationRepository,apiKeyAggregationDynamoDbAsyncTable);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        CompletableFuture<ApiKeyAggregation> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyAggregation);
        Mockito.when(apiKeyAggregationDynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);
        StepVerifier.create(aggregationRepository.getApiKeyAggregation("42"))
                .expectNext(apiKeyAggregation).verifyComplete();
    }
}
