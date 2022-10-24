package it.pagopa.pn.apikey.manager.repository;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
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

    @Override
    public Mono<List<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey){
        Map<String, String> expressionNames = new HashMap<>();
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        expressionNames.put("#cxid", "x-pagopa-pn-cx-id");

        AttributeValue pnCxId = AttributeValue.builder().s(xPagopaPnCxId).build();
        expressionValues.put(":cxid", pnCxId);

        String expressionGroup = "";
        if(xPagopaPnCxGroups.size()!=0){
            for(int i = 0; i < xPagopaPnCxGroups.size(); i++){
                AttributeValue pnCxGroup = AttributeValue.builder().s(xPagopaPnCxGroups.get(i)).build();
                expressionValues.put(":group"+i, pnCxGroup);
                expressionGroup = expressionGroup + " contains(groups,:group"+i+") OR";
            }
            expressionGroup = "AND ("+expressionGroup.substring(0,expressionGroup.length()-2) + ")";
        }
        else{
            expressionGroup = "AND ( contains(groups,:group1))";
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
