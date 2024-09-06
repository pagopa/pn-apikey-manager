package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.PublicKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysIssuerResponseDto;
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
     * PUT /pg-self/public-key/{kid}/delete : Rimozione public key
     * servizio di rimozione della public key identificata tramite Kid
     *
     * @param xPagopaPnUid      User Identifier (required)
     * @param xPagopaPnCxType   Customer/Receiver Type (required)
     * @param xPagopaPnCxId     Customer/Receiver Identifier (required)
     * @param kid               Identificativo univoco della public key (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param xPagopaPnCxRole   User role (optional)
     * @return No content (status code 204)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. delete an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deletePublicKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String kid, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        String logMessage = String.format("Cancellazione di una Public Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s - kid=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups,
                kid);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_DELETE, logMessage)
                .build();

        logEvent.log();

        return publicKeyService.deletePublicKey(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().build();
                });
    }

    @Override
    public Mono<ResponseEntity<PublicKeysIssuerResponseDto>> getIssuerStatus(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, final ServerWebExchange exchange) {
        String logMessage = String.format("Verifica esistenza issuer - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        if (!CxTypeAuthFleetDto.PG.name().equals(xPagopaPnCxType.getValue())) {
            log.logCheckingOutcome("validating access", false, "only a PG can access this resource");
            return Mono.error(new PnForbiddenException());
        }

        return publicKeyService.getIssuer(xPagopaPnCxId)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .map(responseDto -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(responseDto);
                });
    }
}