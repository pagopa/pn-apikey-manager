package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@DynamoDbBean
public class ApiKeyModel {

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(ApiKeyConstant.PK),
            @DynamoDbSecondarySortKey(indexNames = ApiKeyConstant.GSI_VK)
    }))
    private String id;

    @Getter(onMethod = @__({
            @DynamoDbSecondaryPartitionKey(indexNames = ApiKeyConstant.GSI_VK),
            @DynamoDbAttribute("virtualKey")
    }))
    private String virtualKey;

    @Getter(onMethod = @__({@DynamoDbAttribute("name")}))
    private String name;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(ApiKeyConstant.LAST_UPDATE),
            @DynamoDbSecondarySortKey(indexNames = ApiKeyConstant.GSI_PA)
    }))
    private LocalDateTime lastUpdate;

    @Getter(onMethod = @__({@DynamoDbAttribute("status")}))
    private String status;

    @Getter(onMethod = @__({@DynamoDbAttribute(ApiKeyConstant.GROUPS)}))
    private List<String> groups;

    @Getter(onMethod = @__({@DynamoDbAttribute("statusHistory")}))
    private List<ApiKeyHistoryModel> statusHistory = new ArrayList<>();

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-uid")}))
    private String uid;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(ApiKeyConstant.PA_ID),
            @DynamoDbSecondaryPartitionKey(indexNames = ApiKeyConstant.GSI_PA)
    }))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-cx-type")}))
    private String cxType;

    @Getter(onMethod = @__({@DynamoDbAttribute("x-pagopa-pn-cx-groups")}))
    private List<String> cxGroup;

    @Getter(onMethod = @__({@DynamoDbAttribute("correlationId")}))
    private String correlationId;

}
