package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ApiKeyRepositoryImplTest {

    @MockBean
    private DynamoDbAsyncIndex<Object> index;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void changePdnd(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        apiKeyModel.setId("42");
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyModel);
        when(dynamoDbAsyncTable.updateItem((UpdateItemEnhancedRequest<Object>) any())).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.changePdnd("42",true)).expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void delete(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        apiKeyModel.setId("42");
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyModel);
        when(dynamoDbAsyncTable.deleteItem((Key) any())).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.delete("42")).expectNext(apiKeyModel.getId())
                .verifyComplete();
    }

    @Test
    void save(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> null);
        when(dynamoDbAsyncTable.putItem(apiKeyModel)).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.save(apiKeyModel))
                .expectNext(apiKeyModel).verifyComplete();

    }

    @Test
    void findById(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        apiKeyModel.setId("id");

        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyModel);
        when(dynamoDbAsyncTable.getItem((Key) any())).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.findById("42")).expectNext(apiKeyModel).verifyComplete();
    }

    @Test
    void testSetNewVirtualKey(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(apiKeyModel);

        when(dynamoDbAsyncTable.updateItem((ApiKeyModel) any())).thenReturn(CompletableFuture.completedFuture(apiKeyModel));

        StepVerifier.create(apiKeyRepository.setNewVirtualKey(apiKeyModels,"virtualKey"))
                .expectNextCount(0);

    }

    @Test
    void findByCxId(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        StepVerifier.create(apiKeyRepository.findByCxId("cxId", ApiKeyModel.Scope.APIKEY.name()))
                .expectNextCount(0);

    }

    @Test
    void findByCxIdAndStatusRotateAndEnabled(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        StepVerifier.create(apiKeyRepository.findByCxIdAndStatusRotateAndEnabled("cxId"))
                .expectNextCount(0);

    }

    @Test
    void getAllWithFilter() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        List<String> list = new ArrayList<>();
        list.add("test2");
        list.add("test1");

        Mockito.when(dynamoDbAsyncTable.index("")).thenReturn(index);
        Mockito.when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        ApiKeyPageable pageable = ApiKeyPageable.builder()
                .limit(10)
                .lastEvaluatedKey("id")
                .lastEvaluatedLastUpdate("")
                .build();
        StepVerifier.create(apiKeyRepository.getAllWithFilter("paId", list, pageable))
                .expectNext(Page.create(apiKeyModelList));
    }


    @Test
    void testCount(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        StepVerifier.create(apiKeyRepository.countWithFilters("id",new ArrayList<>())).expectNext(0);
    }

    @Test
    void findByUidAndCxIdAndStatusAndScope(){
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"","");

        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        StepVerifier.create(apiKeyRepository.findByUidAndCxIdAndStatusAndScope("uid","cxId", "ENABLED", ApiKeyModel.Scope.CLIENTID.name())).expectNextCount(0);
    }

    @Test
    void getVirtualKeys() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        Mockito.when(dynamoDbAsyncTable.index("")).thenReturn(index);
        Mockito.when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        ApiKeyPageable pageable = ApiKeyPageable.builder()
                .limit(10)
                .lastEvaluatedKey("id")
                .lastEvaluatedLastUpdate("")
                .build();
        StepVerifier.create(apiKeyRepository.getVirtualKeys("uid","cxId", new ArrayList<>(), pageable, true))
                .expectNext(Page.create(apiKeyModelList));
    }

    @Test
    void testCountVirtualKeys(){
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "", "");

        SdkPublisher<Page<Object>> sdkPublisher = mock(SdkPublisher.class);
        DynamoDbAsyncIndex<Object> index = mock(DynamoDbAsyncIndex.class);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(sdkPublisher);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        StepVerifier.create(apiKeyRepository.countWithFilters("uid", "cxId", true)).expectNext(0);
    }
}
