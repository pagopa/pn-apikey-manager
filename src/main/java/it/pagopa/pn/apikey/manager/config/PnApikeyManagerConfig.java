package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "pn.apikey.manager")
@Slf4j
@Data
@Import({SharedAutoConfiguration.class})
public class PnApikeyManagerConfig {

    private String userAttributesBaseUrl;
    private String externalRegistriesBaseUrl;
    private String clientDatavaultBasepath;
    private Dao dao;

    private int jwksCacheMaxDurationSec;
    private int jwksCacheRenewSec;
    private String lambdaName;
    private Sqs sqs;

    private AttributeResolverDatabase attributeResolversCfgsDatabase;
    private AttributeResolverPgCustom attributeResolversCfgsPgCustom;

    private Boolean enableJwksCreation;

    @Data
    public static class Dao {
        private String publicKeyTableName;
    }

    @Data
    public static class Sqs {
        private String internalQueueName;
    }

    @Data
    public static class AttributeResolver{
        private String name;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AttributeResolverDatabase extends AttributeResolver{
        private Map<String, String> cfg;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AttributeResolverPgCustom extends AttributeResolver{
        private Map<String, List<String>> cfg;
    }

    public List<AttributeResolver> retrieveAttributeResolvers(){
        return List.of(attributeResolversCfgsDatabase, attributeResolversCfgsPgCustom);
    }
}
