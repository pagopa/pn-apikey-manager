package it.pagopa.pn.apikey.manager.repository;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.KEY_DOES_NOT_EXISTS;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.KEY_IS_NOT_UNIQUE;

@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository{

    private final DynamoDbAsyncTable<ApiKeyModel> table;
    private final String gsiLastUpdate;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                @Value("${pn.apikey.manager.dynamodb.apikey.gsi-name.last-update}") String gsiLastUpdate) {
        this.table = dynamoDbEnhancedClient.table("pn-apiKey", TableSchema.fromBean(ApiKeyModel.class));
        this.gsiLastUpdate = gsiLastUpdate;
    }


    @Override
    public Mono<String> delete(String key) {
        return Mono.fromFuture(table.deleteItem(Key.builder().partitionValue(key).build()))
                .map(ApiKeyModel::getId);
    }

    @Override
    public Mono<ApiKeyModel> save(ApiKeyModel apiKeyModel) {
        return Mono.fromFuture(table.putItem(apiKeyModel))
                .map(r -> apiKeyModel);
    }

    @Override
    public Mono<ApiKeyModel> findById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();

        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(KEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    @Override
    public Mono<List<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey){
        Map<String, String> expressionNames = new HashMap<>();
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        expressionNames.put("#cxid", "x-pagopa-pn-cx-id");

        AttributeValue pnCxId = AttributeValue.builder().s(xPagopaPnCxId).build();
        expressionValues.put(":cxid", pnCxId);

        StringBuilder expressionGroup = new StringBuilder();
        if(!xPagopaPnCxGroups.isEmpty()){
            for(int i = 0; i < xPagopaPnCxGroups.size(); i++){
                AttributeValue pnCxGroup = AttributeValue.builder().s(xPagopaPnCxGroups.get(i)).build();
                expressionValues.put(":group"+i, pnCxGroup);
                expressionGroup.append(" contains(groups,:group").append(i).append(") OR");
            }
            expressionGroup.append("AND (").append(expressionGroup.substring(0, expressionGroup.length() - 2)).append(")");
        }
        else{
            expressionGroup.append("AND ( contains(groups,:group1))");
            AttributeValue pnCxGroup = AttributeValue.builder().s("").build();
            expressionValues.put(":group1", pnCxGroup);
        }

        Expression expression = Expression.builder()
                .expression("#cxid = :cxid "+expressionGroup)
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
                .build();

        Map<String,AttributeValue> startKey = null;
        if(lastKey!=null){
            startKey = new HashMap<>();
            startKey.put("virtualKey", AttributeValue.builder().s(lastKey).build());
        }
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .exclusiveStartKey(startKey)
                .filterExpression(expression)
                .build();

        return Mono.from(table.scan(request)
                .map(Page::items));
    }
}
