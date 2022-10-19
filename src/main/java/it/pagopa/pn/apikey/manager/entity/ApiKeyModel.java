package it.pagopa.pn.apikey.manager.entity;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@Data
@DynamoDbBean
public class ApiKeyModel {

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("id")}))
    private String id;

    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("virtualKey")}))
    private String virtualKey;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("name")}))
    private String name;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("lastUpdate")}))
    private String lastUpdate;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("status")}))
    private String status;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("groups")}))
    private List<String>  groups;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("statusHistory")}))
    private List<ApiKeyHistory> statusHistory = new ArrayList<>();

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("x-pagopa-pn-uid")}))
    private String uid;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("x-pagopa-pn-cx-id")}))
    private String cxId;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("x-pagopa-pn-cx-type")}))
    private String cxType;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("x-pagopa-pn-cx-groups")}))
    private List<String> cxGroup;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("correlationId")}))
    private String correlationId;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("apiKey")}))
    private String apiKey;
}
