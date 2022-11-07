package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
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
class PaAggregationModelRepositoryImplTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void searchAggregation() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationAggregationRepositoryImpl paRepository = new PaAggregationAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "");

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaId("id");
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> paAggregationModel);
        Mockito.when(dynamoDbAsyncTable.getItem((Key)any())).thenReturn(completableFuture);

        StepVerifier.create(paRepository.searchAggregation("id"))
                .expectNext(paAggregationModel).verifyComplete();
    }

    @Test
    void savePaAggregation() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PaAggregationAggregationRepositoryImpl paRepository = new PaAggregationAggregationRepositoryImpl(dynamoDbEnhancedAsyncClient, "");

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        Mockito.when(dynamoDbAsyncTable.putItem(paAggregationModel)).thenReturn(completableFuture);

        StepVerifier.create(paRepository.savePaAggregation(paAggregationModel))
                .expectNext(paAggregationModel).verifyComplete();
    }
}
