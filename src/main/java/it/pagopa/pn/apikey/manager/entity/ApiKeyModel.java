package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@DynamoDbBean
public class ApiKeyModel {

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute("id"), @DynamoDbSecondarySortKey(indexNames = "virtualKey-id-index")}))
    private String id;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = "virtualKey-id-index"), @DynamoDbAttribute("virtualKey")}))
    private String virtualKey;

    @Getter(onMethod = @__({@DynamoDbAttribute("name")}))
    private String name;

    @Getter(onMethod = @__({@DynamoDbAttribute("lastUpdate"), @DynamoDbSecondarySortKey(indexNames = "paId-lastUpdate-index")}))
    private LocalDateTime lastUpdate;

    @Getter(onMethod = @__({@DynamoDbAttribute("status")}))
    private String status;

    @Getter(onMethod = @__({@DynamoDbAttribute("groups")}))
    private List<String> groups;

    @Getter(onMethod = @__({@DynamoDbAttribute("statusHistory")}))
    private List<ApiKeyHistory> statusHistory = new ArrayList<>();

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-uid")}))
    private String uid;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = "paId-lastUpdate-index"), @DynamoDbAttribute("x-pagopa-pn-cx-id")}))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-cx-type")}))
    private String cxType;

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-cx-groups")}))
    private List<String> cxGroup;

    @Getter(onMethod = @__({@DynamoDbAttribute("correlationId")}))
    private String correlationId;

}
