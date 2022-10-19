package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class PaAggregation {

    @Setter
    @Getter(onMethod=@__({@DynamoDbAttribute("x-pagopa-pn-cx-id")}))
    private String paId;

    @Setter
    @Getter(onMethod=@__({@DynamoDbAttribute("aggregationId")}))
    private String aggregationId;
}
