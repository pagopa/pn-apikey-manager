package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.ApiClient;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataVaultClientConfigurator extends CommonBaseClient {
    @Bean
    public RecipientsApi recipientsApi(PnApikeyManagerConfig config) {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(config.getClientDatavaultBasepath());
        return new RecipientsApi(apiClient);
    }
}
