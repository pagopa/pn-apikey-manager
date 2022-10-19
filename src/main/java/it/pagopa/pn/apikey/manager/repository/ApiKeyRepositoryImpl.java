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
    public Mono<List<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups){
        Map<String, String> expressionNames = new HashMap<>();
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        expressionNames.put("#cxid", "x-pagopa-pn-cx-id");

        AttributeValue pnCxId = AttributeValue.builder().s(xPagopaPnCxId).build();
        expressionValues.put(":cxid", pnCxId);

        int indexGroup = 1;
        String expressionGroup = "(";
        for(String group : xPagopaPnCxGroups){
            AttributeValue pnCxGroup = AttributeValue.builder().s(group).build();
            expressionValues.put(":group"+indexGroup, pnCxGroup);
            expressionGroup = expressionGroup + " contains(groups,:group"+indexGroup+") OR";
            indexGroup = indexGroup + 1;
        }
        expressionGroup = expressionGroup.substring(0,expressionGroup.length()-2) + ")";

        Expression expression = Expression.builder()
                .expression("#cxid = :cxid AND "+expressionGroup)
                .expressionValues(expressionValues)
                .expressionNames(expressionNames)
                .build();


        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .build();

        return Mono.from(table.scan(request)
                .map(Page::items));
    }

}
