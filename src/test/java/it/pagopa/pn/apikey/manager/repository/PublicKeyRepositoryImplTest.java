package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicKeyRepositoryImplTest {

    private PublicKeyRepositoryImpl repository;
    private DynamoDbAsyncTable<PublicKeyModel> table;

    @BeforeEach
    void setUp() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient = mock(DynamoDbEnhancedAsyncClient.class);
        table = mock(DynamoDbAsyncTable.class);
        when(dynamoDbEnhancedClient.table(anyString(), any(TableSchema.class))).thenReturn(table);
        PnApikeyManagerConfig pnApikeyManagerConfig = new PnApikeyManagerConfig();
        PnApikeyManagerConfig.Dao dao = new PnApikeyManagerConfig.Dao();
        dao.setPublicKeyTableName("pn-publicKey");
        pnApikeyManagerConfig.setDao(dao);

        repository = new PublicKeyRepositoryImpl(dynamoDbEnhancedClient, pnApikeyManagerConfig);
    }

    @Test
    void changeStatusSuccessfully() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        when(table.updateItem(any(UpdateItemEnhancedRequest.class))).thenReturn(CompletableFuture.completedFuture(publicKeyModel));

        Mono<PublicKeyModel> result = repository.updateItemStatus(publicKeyModel, Collections.singletonList("DELETED"));

        StepVerifier.create(result)
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void findByKidAndCxIdSuccessfully() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        when(table.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(publicKeyModel));

        Mono<PublicKeyModel> result = repository.findByKidAndCxId("kid", "cxId");

        StepVerifier.create(result)
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void findByKidAndCxIdNotFound() {
        when(table.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(null));
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        Mono<PublicKeyModel> result = repository.findByKidAndCxId("kid","cxId");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

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

        StepVerifier.create(repository.findByCxIdAndStatus("cxId", "ACTIVE"))
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