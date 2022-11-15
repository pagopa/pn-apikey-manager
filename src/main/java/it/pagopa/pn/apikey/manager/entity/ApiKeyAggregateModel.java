package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@DynamoDbBean
public class ApiKeyAggregateModel {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(AggregationConstant.PK)}))
    private String aggregateId;

    @Getter(onMethod=@__({
            @DynamoDbAttribute(AggregationConstant.NAME),
            @DynamoDbSecondarySortKey(indexNames = AggregationConstant.GSI_NAME)
    }))
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

    @Getter(onMethod = @__({
            @DynamoDbAttribute(AggregationConstant.PAGEABLE),
            @DynamoDbSecondaryPartitionKey(indexNames = AggregationConstant.GSI_NAME)
    }))
    private String pageable = AggregationConstant.PAGEABLE_VALUE;

    public ApiKeyAggregateModel(ApiKeyAggregateModel model) {
        aggregateId = model.aggregateId;
        name = model.name;
        description = model.description;
        createdAt =  model.createdAt;
        lastUpdate = model.lastUpdate;
        apiKey = model.apiKey;
        apiKeyId = model.apiKeyId;
        usagePlanId = model.usagePlanId;
        pageable = model.pageable;
    }
}
