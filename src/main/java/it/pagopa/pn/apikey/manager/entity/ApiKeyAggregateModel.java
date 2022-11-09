package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@Data
@DynamoDbBean
public class ApiKeyAggregateModel {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(AggregationConstant.PK)}))
    private String aggregateId;

    @Getter(onMethod=@__({@DynamoDbAttribute("name")}))
    private String name;
    
    @Getter(onMethod=@__(@DynamoDbAttribute("description")))
    private String description;

    @Getter(onMethod=@__({@DynamoDbAttribute("createdAt")}))
    private LocalDateTime createdAt;

    @Getter(onMethod=@__({@DynamoDbAttribute("lastUpdate")}))
    private LocalDateTime lastUpdate;

    @Getter(onMethod=@__({@DynamoDbAttribute("AWSApiKey")}))
    private String apiKey;

    @Getter(onMethod=@__({@DynamoDbAttribute("AWSApiKeyId")}))
    private String apiKeyId;

    @Getter(onMethod = @__({@DynamoDbAttribute("usagePlanId")}))
    private String usagePlanId;

}
