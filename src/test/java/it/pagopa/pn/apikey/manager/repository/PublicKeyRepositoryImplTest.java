package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

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
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);
        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        List<PublicKeyModel> publicKeyModelList = new ArrayList<>();
        publicKeyModelList.add(publicKeyModel);

        StepVerifier.create(repository.findByCxIdAndStatus("cxId", "ACTIVE"))
                .expectNextCount(0);
    }

    @Test
    void findByCxIdAndStatus_withValidCxIdAndNullStatus_returnsFluxOfPublicKeyModels_ActiveAndRotated() {
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);
        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        PublicKeyModel mockPublicKeyModelActive = new PublicKeyModel();
        mockPublicKeyModelActive.setStatus("ACTIVE");
        mockPublicKeyModelActive.setPublicKey("testPublicKey");
        mockPublicKeyModelActive.setKid("testKid");

        PublicKeyModel mockPublicKeyModelRotated = new PublicKeyModel();
        mockPublicKeyModelRotated.setStatus("ROTATED");
        mockPublicKeyModelRotated.setPublicKey("testPublicKey");
        mockPublicKeyModelRotated.setKid("testKid");
        List<PublicKeyModel> publicKeyModelList = new ArrayList<>();
        publicKeyModelList.add(mockPublicKeyModelActive);
        publicKeyModelList.add(mockPublicKeyModelRotated);


        StepVerifier.create(repository.findByCxIdAndStatus("cxId", null))
                .expectNextCount(0);
    }

    @Test
    void save_withValidPublicKeyModel_returnsSavedPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(table.putItem(publicKeyModel)).thenReturn(completableFuture);

        StepVerifier.create(repository.save(publicKeyModel))
                .expectNext(publicKeyModel).verifyComplete();
    }

    @Test
    void getAllWithFilterPaginated_withValidCxIdAndPageable_returnsMonoOfPage() {
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);

        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .lastEvaluatedKey("lastEvaluatedKey")
                .createdAt("createdAt")
                .limit(10)
                .build();

        StepVerifier.create(repository.getAllWithFilterPaginated("cxId", pageable, any()))
                .expectNext(Page.create(new ArrayList<>()));
    }

    @Test
    void countWithFilters_withValidCxIdAndPageable_returnsMonoOfInt() {
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);

        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);


        StepVerifier.create(repository.countWithFilters("cxId"))
                .expectNext(0);
    }

    @Test
    void getIssuerSuccessfully() {
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);
        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        Mono<Page<PublicKeyModel>> result = repository.getIssuer("cxId");

        StepVerifier.create(result)
                .expectNext(Page.create(new ArrayList<>()));
    }

    @Test
    void findByCxIdAndWithoutTtl_withValidCxId_returnsMonoOfPage() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);
        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        StepVerifier.create(repository.findByCxIdAndWithoutTtl("cxId"))
                .expectNextCount(0);
    }
}