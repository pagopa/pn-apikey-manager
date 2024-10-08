package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties(prefix = "pn.apikey.manager.usageplan")
@Slf4j
@Data
@Import({SharedAutoConfiguration.class, PnAuditLogBuilder.class})
public class PnApikeyManagerUsagePlanConfig {
    private String keyType;
    private String defaultPlan;
    private String scope;
    private String tag;
}
