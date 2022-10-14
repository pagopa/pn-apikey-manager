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
@DynamoDBTable(tableName = "pa-aggregation")
public class PaAggregation {

    @DynamoDBAttribute(attributeName = "paId")
    private String paId;

    @DynamoDBHashKey(attributeName = "aggregationId")
    private String aggregationId;
}
