package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@Data
@ToString
@DynamoDbBean
public class ApiKeyAggregation {

    @Setter
    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("aggregateId")}))
    private String aggregateId;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("aggregateName")}))
    private String aggregateName;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("createdAt")}))
    private LocalDateTime createdAt;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("lastUpdate")}))
    private LocalDateTime lastUpdate;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("AWSApiKey")}))
    private String apiKey;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("AWSApiKeyId")}))
    private String apiKeyId;
}
