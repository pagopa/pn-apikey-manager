package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class PaRepositoryImplTest {

    @MockBean
    private DynamoDbAsyncTable<PaAggregation> table;

    @Test
    void searchAggregation() throws IllegalAccessException, NoSuchFieldException {
        PaRepositoryImpl paRepository = new PaRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = PaRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(paRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        paAggregation.setPaId("id");
        CompletableFuture<PaAggregation> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> paAggregation);
        Mockito.when(table.getItem((Key)any())).thenReturn(completableFuture);

        StepVerifier.create(paRepository.searchAggregation("id"))
                .expectNext(paAggregation).verifyComplete();

    }

    @Test
    void savePaAggregation() throws IllegalAccessException, NoSuchFieldException {
        PaRepositoryImpl paRepository = new PaRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = PaRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(paRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Mockito.when(table.putItem(paAggregation)).thenReturn(completableFuture);

        StepVerifier.create(paRepository.savePaAggregation(paAggregation))
                .expectNext(paAggregation);

    }
}
