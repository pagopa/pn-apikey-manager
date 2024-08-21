package it.pagopa.pn.apikey.manager.entity;

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

    public static final String KID = "kid";
    public static final String NAME = "name";
    public static final String CORRELATION_ID = "correlationId";
    public static final String PUBLIC_KEY = "publickey";
    public static final String EXPIRE_AT = "expireAt";
    public static final String CREATED_AT = "createdAt";
    public static final String STATUS = "status";
    public static final String CX_ID = "cxId";
    public static final String STATUS_HISTORY = "statusHistory";
    public static final String ISSUER = "issuer";
    public static final String TTL = "ttl";
    public static final String CHANGE_BY_DENOMINATION = "changeByDenomination";
    public static final String DATE = "date";

    public static final String GSI_CXID_STATUS = "cxId-status-index";
    public static final String GSI_CXID_CREATEDAT = "cxId-createdAt-index";

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(KID)
    }))
    private String kid;

    @Getter(onMethod = @__({@DynamoDbAttribute(NAME)}))
    private String name;

    @Getter(onMethod = @__({@DynamoDbAttribute(CORRELATION_ID)}))
    private String correlationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(PUBLIC_KEY)}))
    private String publicKey;

    @Getter(onMethod = @__({@DynamoDbAttribute(EXPIRE_AT)}))
    private Instant expireAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(CREATED_AT),
            @DynamoDbSecondarySortKey(indexNames = GSI_CXID_CREATEDAT)}))
    private Instant createdAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(STATUS),
            @DynamoDbSecondarySortKey(indexNames = GSI_CXID_STATUS)
    }))
    private String status;

    @Getter(onMethod = @__({
            @DynamoDbSortKey,
            @DynamoDbAttribute(CX_ID),
            @DynamoDbSecondaryPartitionKey(indexNames = {GSI_CXID_STATUS, GSI_CXID_CREATEDAT}),
    }))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbAttribute(STATUS_HISTORY)}))
    private List<StatusHistoryItem> statusHistory = new ArrayList<>();

    @Getter(onMethod = @__({@DynamoDbAttribute(ISSUER)}))
    private String issuer;

    @Getter(onMethod = @__({@DynamoDbAttribute(TTL)}))
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
        @Getter(onMethod = @__({@DynamoDbAttribute(CHANGE_BY_DENOMINATION)}))
        private String changeByDenomination;

        @Getter(onMethod = @__({@DynamoDbAttribute(DATE)}))
        private Instant date;

        @Getter(onMethod = @__({@DynamoDbAttribute(STATUS)}))
        private String status;
    }
}