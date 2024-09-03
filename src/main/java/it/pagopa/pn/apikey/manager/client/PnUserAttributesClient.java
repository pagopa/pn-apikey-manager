package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.api.ConsentsApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Component
@CustomLog
public class PnUserAttributesClient extends CommonBaseClient {

    private final ConsentsApi consentsApi;
    public Mono<ConsentDto> getConsentByType(String xPagopaPnUid, String xPagopaPnCxType, ConsentTypeDto consentType, String version) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_USER_ATTRIBUTES, "getConsentByType");
        return consentsApi.getConsentByType(xPagopaPnUid, CxTypeAuthFleetDto.fromValue(xPagopaPnCxType), consentType, version)
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(PnLogger.EXTERNAL_SERVICES.PN_USER_ATTRIBUTES, ex.getMessage()));
    }
}
