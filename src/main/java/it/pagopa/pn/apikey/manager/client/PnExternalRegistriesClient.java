package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.api.PrivacyNoticeApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Component
@CustomLog
public class PnExternalRegistriesClient extends CommonBaseClient {

    private final PrivacyNoticeApi privacyNoticeApi;
    public Mono<PrivacyNoticeVersionResponseDto> findPrivacyNoticeVersion(String consentsType, String portalType) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_REGISTRIES, "findPrivacyNoticeVersion");
        return privacyNoticeApi.findPrivacyNoticeVersion(consentsType, portalType)
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_REGISTRIES, ex.getMessage()));
    }
}