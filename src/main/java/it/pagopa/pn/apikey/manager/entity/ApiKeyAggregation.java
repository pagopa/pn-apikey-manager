package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class ApiKeyAggregation {

    @Setter
    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("aggregateId")}))
    private String aggregateId;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("aggregationName")}))
    private String aggregationName;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("createdAt")}))
    private String createdAt;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("lastUpdate")}))
    private String lastUpdate;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("realApiKey")}))
    private String apiKey;
}
