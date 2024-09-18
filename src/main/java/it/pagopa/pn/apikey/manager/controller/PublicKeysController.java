package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.PublicKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.BLOCK_OPERATION;
import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;

@RestController
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeysController implements PublicKeysApi {

    private final PublicKeyService publicKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    /**
     * DELETE /pg-self/public-keys/{kid} : Rimozione public key
     * servizio di rimozione della public key identificata tramite Kid
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param kid Identificativo univoco della public key (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param xPagopaPnCxRole User role (optional)
     * @return No content (status code 204)
     *         or Bad request (status code 400)
     *         or Wrong state transition (i.e. delete an enabled key) (status code 409)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
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

    /**
     * PUT /pg-self/public-keys/{kid}/status : Blocco/Riattivazione public key
     * servizio di blocco/riattivazione della public key identificata tramite Kid
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param xPagopaPnCxRole User role (required)
     * @param kid Identificativo univoco della public key (required)
     * @param status Action per il cambio stato di una public key (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @return No content (status code 204)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     *         or Wrong state transition (i.e. enable an enabled key) (status code 409)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changeStatusPublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String kid, String status, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {

        String logMessage = String.format("Start cambio stato chiave pubblica - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s, kid=%s, status=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups,
                kid,
                status);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(selectAuditLogEventType(status), logMessage)
                .build();

        logEvent.log();

        return publicKeyService.changeStatus(kid, status, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .then(Mono.defer(() -> {
                    logEvent.generateSuccess(logMessage).log();
                    return Mono.just(ResponseEntity.noContent().build());
                }));
    }

    private PnAuditLogEventType selectAuditLogEventType(String status) {
        if (BLOCK_OPERATION.equalsIgnoreCase(status)) {
            return PnAuditLogEventType.AUD_AK_BLOCK;
        } else if (ENABLE_OPERATION.equalsIgnoreCase(status)) {
            return PnAuditLogEventType.AUD_AK_REACTIVATE;
        } else {
            throw new ApiKeyManagerException("Invalid state", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * POST /pg-self/public-keys : Censimento public key
     * servizio di censimento di una public key
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param publicKeyRequestDto  (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param xPagopaPnCxRole User role (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PublicKeyResponseDto>> newPublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        String logMessage = String.format("Creazione di una Public Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_CREATE, logMessage)
                .build();

        logEvent.log();

        return publicKeyService.createPublicKey(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, publicKeyRequestDto, xPagopaPnCxGroups, xPagopaPnCxRole)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }

    /**
     * POST /pg-self/public-keys/{kid}/rotate : Rotazione public key
     * servizio di rotazione della public key identificata tramite Kid
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param xPagopaPnCxRole User role (required)
     * @param kid Identificativo univoco della public key (required)
     * @param publicKeyRequestDto  (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     *         or Wrong state transition (i.e. enable an enabled key) (status code 409)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PublicKeyResponseDto>> rotatePublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String kid, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
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

    /**
     * GET /pg-self/public-keys : Ricerca public keys
     * servizio di consultazione della lista delle public keys
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param xPagopaPnCxRole User role (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param limit  (optional, default to 10)
     * @param lastKey  (optional)
     * @param createdAt  (optional)
     * @param showPublicKey  (optional, default to false)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PublicKeysResponseDto>> getPublicKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, List<String> xPagopaPnCxGroups, Integer limit, String lastKey, String createdAt, Boolean showPublicKey, final ServerWebExchange exchange) {
        String logMessage = String.format("Recupero delle Public Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s - xPagopaPnCxRole=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups,
                xPagopaPnCxRole);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return publicKeyService.getPublicKeys(xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, createdAt, showPublicKey)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }

    /**
     * GET /pg-self/public-keys/issuer/status : Verifica esistenza issuer
     * servizio di verifica esistenza issuer
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Forbidden (status code 403)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PublicKeysIssuerResponseDto>> getIssuerStatus(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, final ServerWebExchange exchange) {
        log.info("getIssuerStatus - xPagopaPnUid={}, xPagopaPnCxType={}, xPagopaPnCxId={}", xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);

        return publicKeyService.getIssuer(xPagopaPnCxId, xPagopaPnCxType)
                .doOnError(throwable -> log.error("Error in getIssuerStatus", throwable))
                .map(responseDto -> ResponseEntity.ok().body(responseDto));
    }
}