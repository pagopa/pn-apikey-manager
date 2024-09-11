package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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

    @Data
    public static class Dao {
        private String publicKeyTableName;
    }
}
