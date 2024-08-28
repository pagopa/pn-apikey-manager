package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.PublicKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeysController implements PublicKeysApi {

    private final PublicKeyService publicKeyService;
    private final PnAuditLogBuilder auditLogBuilder;


    /**
     * POST /pg-self/public-key/{kid}/rotate : Rotazione public key
     * servizio di rotazione della public key identificata tramite Kid
     *
     * @param xPagopaPnUid        User Identifier (required)
     * @param xPagopaPnCxType     Customer/Receiver Type (required)
     * @param xPagopaPnCxId       Customer/Receiver Identifier (required)
     * @param kid                 Identificativo univoco della public key (required)
     * @param publicKeyRequestDto (required)
     * @param xPagopaPnCxGroups   Customer Groups (optional)
     * @param xPagopaPnCxRole     User role (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. enable an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PublicKeyResponseDto>> rotatePublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole, final ServerWebExchange exchange) {
        String logMessage = String.format("Rotazione di una Public Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s - kid=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups,
                kid);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_ROTATE, logMessage)
                .build();

        logEvent.log();

        return publicKeyService.rotatePublicKey(publicKeyRequestDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }
}