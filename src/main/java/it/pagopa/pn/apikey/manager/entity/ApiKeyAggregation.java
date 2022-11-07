package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@Data
@DynamoDbBean
public class ApiKeyAggregation {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("aggregateId")}))
    private String aggregateId;

    @Getter(onMethod=@__({@DynamoDbAttribute("aggregateName")}))
    private String aggregateName;

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
