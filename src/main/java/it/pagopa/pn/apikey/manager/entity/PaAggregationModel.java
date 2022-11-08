package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
public class PaAggregationModel {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("x-pagopa-pn-cx-id"), @DynamoDbSecondarySortKey(indexNames = "paAggregates-aggregateId-index")}))
    private String paId;

    @Getter(onMethod=@__({@DynamoDbAttribute("aggregateId"), @DynamoDbSecondaryPartitionKey(indexNames = "paAggregates-aggregateId-index")}))
    private String aggregateId;

    @Getter(onMethod = @__({@DynamoDbAttribute("paName")}))
    private String paName;

}
