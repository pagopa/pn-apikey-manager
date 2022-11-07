package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties(prefix = "pn.apikey.manager.usageplan")
@Slf4j
@Data
@ToString
@Import(SharedAutoConfiguration.class)
public class PnApikeyManagerConfig {
    private String apiId;
    private String keyType;
    private String stage;
}
