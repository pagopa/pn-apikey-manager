package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@DynamoDbBean
public class PublicKeyModel {

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute("kid")
    }))
    private String kid;

    @Getter(onMethod = @__({@DynamoDbAttribute("name")}))
    private String name;

    @Getter(onMethod = @__({@DynamoDbAttribute("correlationId")}))
    private String correlationId;

    @Getter(onMethod = @__({@DynamoDbAttribute("publickey")}))
    private String publicKey;

    @Getter(onMethod = @__({@DynamoDbAttribute("expireAt")}))
    private Instant expireAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute("createdAt"),
            @DynamoDbSecondarySortKey(indexNames = PublicKeyConstant.GSI_CXID_CREATEDAT)}))
    private Instant createdAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute("status"),
            @DynamoDbSecondarySortKey(indexNames = PublicKeyConstant.GSI_CXID_STATUS)
    }))
    private String status;

    @Getter(onMethod = @__({
            @DynamoDbSortKey,
            @DynamoDbAttribute("cxId"),
            @DynamoDbSecondaryPartitionKey(indexNames = PublicKeyConstant.GSI_CXID_STATUS),
            @DynamoDbSecondaryPartitionKey(indexNames = PublicKeyConstant.GSI_CXID_CREATEDAT),
    }))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbAttribute("statusHistory")}))
    private List<StatusHistoryItem> statusHistory = new ArrayList<>();

    @Getter(onMethod = @__({@DynamoDbAttribute("issuer")}))
    private String issuer;

    @Getter(onMethod = @__({@DynamoDbAttribute("ttl")}))
    private Instant ttl;

    public PublicKeyModel(PublicKeyModel publicKeyModel) {
        this.kid = publicKeyModel.kid;
        this.name = publicKeyModel.name;
        this.correlationId = publicKeyModel.correlationId;
        this.publicKey = publicKeyModel.publicKey;
        this.expireAt = publicKeyModel.expireAt;
        this.createdAt = publicKeyModel.createdAt;
        this.status = publicKeyModel.status;
        this.cxId = publicKeyModel.cxId;
        this.statusHistory = new ArrayList<>(publicKeyModel.statusHistory);
        this.issuer = publicKeyModel.issuer;
        this.ttl = publicKeyModel.ttl;
    }

    @Data
    @NoArgsConstructor
    @DynamoDbBean
    public static class StatusHistoryItem {
        @Getter(onMethod = @__({@DynamoDbAttribute("changeByDenomination")}))
        private String changeByDenomination;

        @Getter(onMethod = @__({@DynamoDbAttribute("date")}))
        private Instant date;

        @Getter(onMethod = @__({@DynamoDbAttribute("status")}))
        private String status;
    }
}