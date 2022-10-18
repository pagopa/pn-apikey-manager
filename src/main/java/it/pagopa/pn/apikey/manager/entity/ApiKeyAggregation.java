package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class ApiKeyAggregation {

    private String aggregationId;

    private String aggregationName;

    private String creationDate;

    private String lastUpdate;

    private String apiKey;
}
