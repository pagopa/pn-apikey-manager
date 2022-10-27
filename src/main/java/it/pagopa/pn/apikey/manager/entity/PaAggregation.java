package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@ToString
@DynamoDbBean
public class PaAggregation {

    @Setter
    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("x-pagopa-pn-cx-id")}))
    private String paId;

    @Setter
    @Getter(onMethod=@__({@DynamoDbAttribute("aggregateId")}))
    private String aggregationId;
}
