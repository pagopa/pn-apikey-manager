package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class PaAggregationModel {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("x-pagopa-pn-cx-id")}))
    private String paId;

    @Getter(onMethod=@__({@DynamoDbAttribute("aggregateId")}))
    private String aggregateId;

    @Getter(onMethod = @__({@DynamoDbAttribute("paName")}))
    private String name;

}
