package it.pagopa.pn.apikey.manager.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "api-key")
public class ApiKeyAggregation {

    @DynamoDBHashKey(attributeName = "aggregationId")
    private String aggregationId;

    @DynamoDBAttribute(attributeName = "aggregationName")
    private String aggregationName;

    @DynamoDBAttribute(attributeName = "creationDate")
    private String creationDate;

    @DynamoDBAttribute(attributeName = "lastUpdate")
    private String lastUpdate;

    @DynamoDBAttribute(attributeName = "realApiKey")
    private String apiKey;
}
