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

import java.util.List;


@AllArgsConstructor
@Component
@CustomLog
public class PnUserAttributesClient extends CommonBaseClient {

    private final ConsentsApi consentsApi;
    public Mono<ConsentDto> getPgConsentByType(String xPagopaPnCxId, String xPagopaPnCxType, String xPagopaPnCxRole, ConsentTypeDto consentType, List<String> xPagopaPnCxGroups, String version) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_USER_ATTRIBUTES, "getConsentByType");
        return consentsApi.getPgConsentByType(xPagopaPnCxId, CxTypeAuthFleetDto.fromValue(xPagopaPnCxType), xPagopaPnCxRole, consentType, xPagopaPnCxGroups, version)
                .doOnError(ex -> log.logInvokationResultDownstreamFailed(PnLogger.EXTERNAL_SERVICES.PN_USER_ATTRIBUTES, ex.getMessage()));
    }
}
