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

    public static final String COL_KID = "kid";
    public static final String COL_NAME = "name";
    public static final String COL_CORRELATION_ID = "correlationId";
    public static final String COL_PUBLIC_KEY = "publickey";
    public static final String COL_EXPONENT = "exponent";
    public static final String COL_KEY_SIZE = "keySize";
    public static final String COL_EXPIRE_AT = "expireAt";
    public static final String COL_CREATED_AT = "createdAt";
    public static final String COL_STATUS = "status";
    public static final String COL_CX_ID = "cxId";
    public static final String COL_STATUS_HISTORY = "statusHistory";
    public static final String COL_ISSUER = "issuer";
    public static final String COL_TTL = "ttl";
    public static final String COL_CHANGE_BY_DENOMINATION = "changeByDenomination";
    public static final String COL_DATE = "date";

    public static final String GSI_CXID_STATUS = "cxId-status-index";
    public static final String GSI_CXID_CREATEDAT = "cxId-createdAt-index";

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(COL_KID)
    }))
    private String kid;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_NAME)}))
    private String name;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CORRELATION_ID)}))
    private String correlationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PUBLIC_KEY)}))
    private String publicKey;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_EXPONENT)}))
    private String exponent;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_KEY_SIZE)}))
    private Integer keySize;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_EXPIRE_AT)}))
    private Instant expireAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(COL_CREATED_AT),
            @DynamoDbSecondarySortKey(indexNames = GSI_CXID_CREATEDAT)}))
    private Instant createdAt;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(COL_STATUS),
            @DynamoDbSecondarySortKey(indexNames = GSI_CXID_STATUS)
    }))
    private String status;

    @Getter(onMethod = @__({
            @DynamoDbSortKey,
            @DynamoDbAttribute(COL_CX_ID),
            @DynamoDbSecondaryPartitionKey(indexNames = {GSI_CXID_STATUS, GSI_CXID_CREATEDAT}),
    }))
    private String cxId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS_HISTORY)}))
    private List<StatusHistoryItem> statusHistory = new ArrayList<>();

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ISSUER)}))
    private String issuer;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TTL)}))
    private Long ttl;

    public PublicKeyModel(PublicKeyModel publicKeyModel) {
        this.kid = publicKeyModel.kid;
        this.name = publicKeyModel.name;
        this.correlationId = publicKeyModel.correlationId;
        this.publicKey = publicKeyModel.publicKey;
        this.exponent = publicKeyModel.exponent;
        this.keySize = publicKeyModel.keySize;
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
        @Getter(onMethod = @__({@DynamoDbAttribute(COL_CHANGE_BY_DENOMINATION)}))
        private String changeByDenomination;

        @Getter(onMethod = @__({@DynamoDbAttribute(COL_DATE)}))
        private Instant date;

        @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS)}))
        private String status;
    }
}