package it.pagopa.pn.apikey.manager.repository;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import java.util.List;

@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository{

    private final DynamoDbAsyncTable<ApiKeyModel> tableVirtualKey;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.tableVirtualKey = dynamoDbEnhancedClient.table("virtual-key", TableSchema.fromBean(ApiKeyModel.class));
    }

    @Override
    public Mono<List<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups){
        return Mono.empty();
    }

}
