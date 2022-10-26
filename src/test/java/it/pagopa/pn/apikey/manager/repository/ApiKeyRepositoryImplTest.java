package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ApiKeyRepositoryImplTest {

    @MockBean
    private DynamoDbAsyncTable<ApiKeyModel> table;

    @MockBean
    private DynamoDbAsyncIndex<ApiKeyModel> index;

    @Test
    void delete() throws NoSuchFieldException, IllegalAccessException {
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = ApiKeyRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(apiKeyRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        apiKeyModel.setId("42");
        CompletableFuture<ApiKeyModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyModel);
        when(table.deleteItem((Key) any())).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.delete("42")).expectNext(apiKeyModel.getId())
                .verifyComplete();
    }

    @Test
    void save() throws NoSuchFieldException, IllegalAccessException  {
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = ApiKeyRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(apiKeyRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        when(table.putItem(apiKeyModel)).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.save(apiKeyModel))
                .expectNext(apiKeyModel);

    }

    @Test
    void findById() throws NoSuchFieldException, IllegalAccessException {
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"");
        Field field = ApiKeyRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(apiKeyRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        apiKeyModel.setId("id");

        CompletableFuture<ApiKeyModel> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> apiKeyModel);
        when(table.getItem((Key) any())).thenReturn(completableFuture);

        StepVerifier.create(apiKeyRepository.findById("42")).expectNext(apiKeyModel).verifyComplete();
    }

   /* @Test
    void getAllWithFilter() throws NoSuchFieldException, IllegalAccessException {
        ApiKeyRepositoryImpl apiKeyRepository = new ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient.builder().build(),"","");
        Field field = ApiKeyRepositoryImpl.class.getDeclaredField("table");
        Field modifier = Field.class.getDeclaredField("modifiers");
        modifier.setAccessible(true);
        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(apiKeyRepository,table);
        field.setAccessible(false);
        modifier.setAccessible(false);

        ApiKeyModel apiKeyModel= new ApiKeyModel();
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);

        List<String> list = new ArrayList<>();
        list.add("test2");
        list.add("test1");

        PagePublisher<ApiKeyModel> pagePublisher = mock(PagePublisher.class);
        Mockito.when(table.index("")).thenReturn(index);
        Mockito.when(index.query((QueryEnhancedRequest) any())).thenReturn(pagePublisher);

        StepVerifier.create(apiKeyRepository.getAllWithFilter("paId",list,1,"id"))
                .expectNext(apiKeyModelList);

    }*/
}
