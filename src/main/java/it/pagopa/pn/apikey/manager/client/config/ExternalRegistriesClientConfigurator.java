package it.pagopa.pn.apikey.manager.client.config;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.ApiClient;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.api.InternalOnlyApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.api.PrivacyNoticeApi;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalRegistriesClientConfigurator extends CommonBaseClient {
    @Bean
    public PrivacyNoticeApi privacyNoticeApi(PnApikeyManagerConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getExternalRegistriesBaseUrl());
        return new PrivacyNoticeApi(apiClient);
    }

    @Bean
    public InternalOnlyApi internalOnlyApi(PnApikeyManagerConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getExternalRegistriesBaseUrl());
        return new InternalOnlyApi(apiClient);
    }
}
