package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
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
    void save_withValidPublicKeyModel_returnsSavedPublicKeyModel() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "");

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem(publicKeyModel)).thenReturn(completableFuture);

        StepVerifier.create(publicKeyRepository.save(publicKeyModel))
                .expectNext(publicKeyModel).verifyComplete();
    }

    @Test
    void findByKidAndCxId_withValidKidAndCxId_returnsPublicKeyModel() {

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");

        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "");
        when(dynamoDbAsyncTable.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(publicKeyModel));

        StepVerifier.create(publicKeyRepository.findByKidAndCxId("kid", "cxId"))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void findByKidAndCxId_withNonExistentKidAndCxId_returnsEmpty() {

        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "");
        when(dynamoDbAsyncTable.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(publicKeyRepository.findByKidAndCxId("nonExistentKid", "nonExistentCxId"))
                .verifyComplete();
    }

    @Test
    void findByKidAndCxId_withException_throwsException() {

        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "");
        when(dynamoDbAsyncTable.getItem(any(Key.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("DynamoDB error")));

        StepVerifier.create(publicKeyRepository.findByKidAndCxId("kid", "cxId"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().contains("DynamoDB error"))
                .verify();
    }
}
