package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyRepositoryImplTest {

    @MockBean
    private DynamoDbAsyncIndex<Object> index;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void findByCxIdAndStatus_withValidCxIdAndStatus_returnsFluxOfPublicKeyModels() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        List<PublicKeyModel> publicKeyModelList = new ArrayList<>();
        publicKeyModelList.add(publicKeyModel);

        StepVerifier.create(publicKeyRepository.findByCxIdAndStatus("cxId", "ACTIVE"))
                .expectNextCount(0);
    }

    @Test
    void save_withValidPublicKeyModel_returnsSavedPublicKeyModel() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"");

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem(publicKeyModel)).thenReturn(completableFuture);

        StepVerifier.create(publicKeyRepository.save(publicKeyModel))
                .expectNext(publicKeyModel).verifyComplete();
    }
}