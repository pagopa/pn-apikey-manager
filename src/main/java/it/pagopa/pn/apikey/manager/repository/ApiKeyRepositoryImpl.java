package it.pagopa.pn.apikey.manager.repository;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiKeyRepositoryImpl implements ApiKeyRepository{

    private final DynamoDbAsyncTable<ApiKeyModel> table;

    public ApiKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("pn-apiKey", TableSchema.fromBean(ApiKeyModel.class));
    }

    public Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey, String lastUpdate){

        Map<String, AttributeValue> expressionValues = new HashMap<>();

        String expressionGroup = "";
        if(xPagopaPnCxGroups.size()!=0){
            for(int i = 0; i < xPagopaPnCxGroups.size(); i++){
                AttributeValue pnCxGroup = AttributeValue.builder().s(xPagopaPnCxGroups.get(i)).build();
                expressionValues.put(":group"+i, pnCxGroup);
                expressionGroup = expressionGroup + " contains(groups,:group"+i+") OR";
            }
            expressionGroup = "("+expressionGroup.substring(0,expressionGroup.length()-2) + ")";
        }
        else{
            expressionGroup = "attribute_exists(groups)";
        }

        Expression expression = Expression.builder()
                .expression(expressionGroup)
                .expressionValues(expressionValues)
                .build();

        Map<String,AttributeValue> startKey = null;
        if(lastKey!=null && lastUpdate!=null){
            startKey = new HashMap<>();
            startKey.put("id", AttributeValue.builder().s(lastKey).build());
            startKey.put("lastUpdate", AttributeValue.builder().s(lastUpdate).build());
            startKey.put("x-pagopa-pn-cx-id", AttributeValue.builder().s(xPagopaPnCxId).build());
        }

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        QueryEnhancedRequest queryEnhancedRequest= QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .exclusiveStartKey(startKey)
                .filterExpression(expression)
                .limit(limit)
                .build();

        return Mono.from(table.index("paId-lastUpdate-index").query(queryEnhancedRequest)
                .map(apiKeyModelPage -> {
                    return apiKeyModelPage;
                }));

    }

}
