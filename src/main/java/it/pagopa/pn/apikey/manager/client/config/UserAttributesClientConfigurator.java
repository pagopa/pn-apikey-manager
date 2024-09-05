package it.pagopa.pn.apikey.manager.client.config;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.ApiClient;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.api.ConsentsApi;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAttributesClientConfigurator extends CommonBaseClient {
    @Bean
    public ConsentsApi consentsApi(PnApikeyManagerConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getUserAttributesBaseUrl());
        return new ConsentsApi(apiClient);
    }
}
