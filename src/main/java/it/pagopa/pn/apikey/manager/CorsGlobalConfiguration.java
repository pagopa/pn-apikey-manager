package it.pagopa.pn.apikey.manager;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

@Configuration
@Slf4j
public class CorsGlobalConfiguration implements WebFluxConfigurer {

    @Value("${cors.allowed.domains:}")
    private List<String> corsAllowedDomains;
    private static final int MAX_AGE = 3600;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        if (log.isInfoEnabled()){
            log.info("allowed domains:" + String.join(", ", corsAllowedDomains));
        }

        corsRegistry.addMapping("/**")
                .allowedOrigins( corsAllowedDomains.toArray( new String[0] ) )
                .allowedMethods("GET", "HEAD", "OPTIONS", "POST", "PUT", "DELETE", "PATCH")
                .maxAge(MAX_AGE);
    }
}
