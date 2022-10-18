package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class PaAggregation {

    private String paId;

    private String aggregationId;
}
