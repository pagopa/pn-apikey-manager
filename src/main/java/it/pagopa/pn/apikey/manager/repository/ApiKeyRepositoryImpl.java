package it.pagopa.pn.apikey.manager.repository;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;

@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository{

    private final DynamoDbAsyncTable<ApiKeyModel> table;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("pn-apiKey", TableSchema.fromBean(ApiKeyModel.class));
    }


    @Override
    public Mono<String> delete(String key) {
        return Mono.fromFuture(table.deleteItem(Key.builder().partitionValue(key).build()).thenApply(r->key));
    }

    @Override
    public Mono<ApiKeyModel> save(ApiKeyModel apiKeyModel) {
        return Mono.fromFuture(table.putItem(apiKeyModel).thenApply(r -> apiKeyModel));
    }

    @Override
    public Mono<ApiKeyModel> findById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();

        return Mono.fromFuture(table.getItem(key).thenApply(apiKeyModel -> apiKeyModel));
    }


}
