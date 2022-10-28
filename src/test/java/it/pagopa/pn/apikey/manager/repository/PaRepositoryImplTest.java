package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
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
class PaRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void searchAggregation(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaRepositoryImpl paRepository = new PaRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        paAggregation.setPaId("id");
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> paAggregation);
        Mockito.when(dynamoDbAsyncTable.getItem((Key)any())).thenReturn(completableFuture);

        StepVerifier.create(paRepository.searchAggregation("id"))
                .expectNext(paAggregation).verifyComplete();

    }

    @Test
    void savePaAggregation(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PaRepositoryImpl paRepository = new PaRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        Mockito.when(dynamoDbAsyncTable.putItem(paAggregation)).thenReturn(completableFuture);

        StepVerifier.create(paRepository.savePaAggregation(paAggregation))
                .expectNext(paAggregation).verifyComplete();

    }
}
