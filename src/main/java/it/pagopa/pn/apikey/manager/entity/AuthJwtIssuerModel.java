package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@DynamoDbBean
public class AuthJwtIssuerModel {

    public static final String COL_HASH_KEY = "hashKey";
    public static final String COL_SORT_KEY = "sortKey";
    public static final String COL_ATTRIBUTE_RESOLVERS_CFGS = "attributeResolversCfgs";
    public static final String COL_ISS = "iss";
    public static final String COL_JWKS_CACHE_EXPIRE_SLOT = "jwksCacheExpireSlot";
    public static final String COL_JWKS_CACHE_MAX_DURATION_SEC = "JWKSCacheMaxDurationSec";
    public static final String COL_JWKS_CACHE_RENEW_SEC = "JWKSCacheRenewSec";
    public static final String COL_JWKS_URL = "JWKSUrl";
    public static final String COL_MODIFICATION_TIME_EPOCH_MS = "modificationTimeEpochMs";
    public static final String COL_JWKS_CACHE_ORIGINAL_EXPIRE_EPOCH_SECONDS = "jwksCacheOriginalExpireEpochSeconds";
    public static final String COL_CX_ID = "cxId";

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(COL_HASH_KEY)
    }))
    private String hashKey;

    @Getter(onMethod=@__({
            @DynamoDbSortKey,
            @DynamoDbAttribute(COL_SORT_KEY)
    }))
    private String sortKey;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ATTRIBUTE_RESOLVERS_CFGS)}))
    private List<AttributeResolverCfg> attributeResolversCfgs;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ISS)}))
    private String iss;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_JWKS_CACHE_EXPIRE_SLOT)}))
    private Instant jwksCacheExpireSlot;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_JWKS_CACHE_MAX_DURATION_SEC)}))
    private int jwksCacheMaxDurationSec;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_JWKS_CACHE_RENEW_SEC)}))
    private int jwksCacheRenewSec;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_JWKS_URL)}))
    private String jwksUrl;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_MODIFICATION_TIME_EPOCH_MS)}))
    private long modificationTimeEpochMs;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_JWKS_CACHE_ORIGINAL_EXPIRE_EPOCH_SECONDS)}))
    private long jwksCacheOriginalExpireEpochSeconds;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CX_ID)}))
    private String cxId;

    @Data
    @NoArgsConstructor
    @DynamoDbBean
    public static class AttributeResolverCfg {
        public static final String COL_CFG = "cfg";
        public static final String COL_NAME = "name";

        @Getter(onMethod=@__({@DynamoDbAttribute(COL_CFG)}))
        private Cfg cfg;

        @Getter(onMethod=@__({@DynamoDbAttribute(COL_NAME)}))
        private String name;

        @Data
        @NoArgsConstructor
        @DynamoDbBean
        public static class Cfg {
            public static final String COL_KEY_ATTRIBUTE_NAME = "keyAttributeName";
            public static final String COL_PURPOSES = "purposes";

            @Getter(onMethod=@__({@DynamoDbAttribute(COL_KEY_ATTRIBUTE_NAME)}))
            private String keyAttributeName;

            @Getter(onMethod=@__({@DynamoDbAttribute(COL_PURPOSES)}))
            private List<Purpose> purposes;

            public enum Purpose {
                REFINEMENT,
                BASE,
                MANDATE
            }
        }
    }
}