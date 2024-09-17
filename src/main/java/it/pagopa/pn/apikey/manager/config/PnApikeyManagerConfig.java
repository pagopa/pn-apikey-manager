package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
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

    private List<AttributeResolver> attributeResolversCfgs;
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
    public static class Dao {
        private String publicKeyTableName;
    }

    @Data
    public static class AttributeResolver {
        private Map<String, Object> cfg;  // Still generic for other fields
        private String name;

        public void setCfg(Map<String, Object> cfg) {
            // If the purposes field exists and is a string, split it into a list
            if (cfg.containsKey("purposes") && cfg.get("purposes") instanceof String) {
                String purposesStr = (String) cfg.get("purposes");
                // Convert the comma-separated string into a list
                List<String> purposesList = Arrays.asList(purposesStr.split(","));
                cfg.put("purposes", purposesList);
            }
            this.cfg = cfg;
        }
    }
}
