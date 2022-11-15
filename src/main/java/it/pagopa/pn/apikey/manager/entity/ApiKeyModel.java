package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
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

    public ApiKeyModel(ApiKeyModel apiKeyModel) {
        id = apiKeyModel.id;
        virtualKey = apiKeyModel.virtualKey;
        name = apiKeyModel.name;
        lastUpdate = apiKeyModel.lastUpdate;
        status = apiKeyModel.status;
        if (apiKeyModel.groups != null) {
            groups = new ArrayList<>(apiKeyModel.groups);
        }
        if (apiKeyModel.statusHistory != null) {
            statusHistory = new ArrayList<>(apiKeyModel.statusHistory);
        }
        uid = apiKeyModel.uid;
        cxId = apiKeyModel.cxId;
        cxType = apiKeyModel.cxType;
        cxGroup = apiKeyModel.cxGroup;
        correlationId = apiKeyModel.correlationId;
    }

}
