package it.pagopa.pn.apikey.manager.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "virtual-key")
public class ApiKeyModel {

    @DynamoDBAttribute(attributeName = "id")
    private String id;

    @DynamoDBHashKey(attributeName = "virtualKey")
    private String virtualKey;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "lastUpdate")
    private String lastUpdate;

    @DynamoDBAttribute(attributeName = "status")
    private String status;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.L)
    @DynamoDBAttribute(attributeName = "groups")
    private List<String>  groups;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.L)
    @DynamoDBAttribute(attributeName = "statusHistory")
    private List<ApiKeyHistory> statusHistory = new ArrayList<>();

    @DynamoDBAttribute(attributeName = "x-pagopa-pn-uid")
    private String uid;

    @DynamoDBAttribute(attributeName = "x-pagopa-pn-cx-id")
    private String cxId;

    @DynamoDBAttribute(attributeName = "x-pagopa-pn-cx-type")
    private String cxType;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.L)
    @DynamoDBAttribute(attributeName = "x-pagopa-pn-cx-groups")
    private List<String> cxGroup;

    @DynamoDBAttribute(attributeName = "correlationId")
    private String correlationId;

    @DynamoDBAttribute(attributeName = "apiKey")
    private String apiKey;
}
