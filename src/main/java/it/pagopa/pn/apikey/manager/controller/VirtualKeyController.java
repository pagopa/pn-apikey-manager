package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.VirtualKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
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
public class VirtualKeyController implements VirtualKeysApi {
    private final VirtualKeyService virtualKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    /**
     * PUT /virtual-keys/{id}/status : Cambia lo stato della virtualKey
     * servizio di cambio stato della virtualKey
     *
     * @param xPagopaPnUid               User Identifier (required)
     * @param xPagopaPnCxType            Customer/Receiver Type (required)
     * @param xPagopaPnCxId              Customer/Receiver Identifier (required)
     * @param xPagopaPnCxRole            User role (required)
     * @param id                         Identificativo univoco della virtual key. (required)
     * @param requestVirtualKeyStatusDto (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. enable an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changeStatusVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String id, Mono<RequestVirtualKeyStatusDto> requestVirtualKeyStatusDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return requestVirtualKeyStatusDto
                .flatMap(dto -> {
                    String logMessage = String.format("Start cambio stato virtualKey - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxRole=%s - xPagopaPnGroups=%s, id=%s, status=%s",
                            xPagopaPnUid,
                            xPagopaPnCxType.getValue(),
                            xPagopaPnCxId,
                            xPagopaPnCxRole,
                            xPagopaPnCxGroups,
                            id,
                            dto.getStatus());

                    PnAuditLogEvent logEvent = auditLogBuilder
                            .before(selectAuditLogEventType(dto.getStatus().toString()), logMessage)
                            .build();

                    logEvent.log();
                    return virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups, id, dto)
                            .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                            .then(Mono.defer(() -> {
                                logEvent.generateSuccess(logMessage).log();
                                return Mono.just(ResponseEntity.ok().build());
                            }));
                });
    }

    private PnAuditLogEventType selectAuditLogEventType(String status) {
        if (RequestVirtualKeyStatusDto.StatusEnum.ROTATE.toString().equalsIgnoreCase(status)) {
            return PnAuditLogEventType.AUD_AK_ROTATE;
        } else if (RequestVirtualKeyStatusDto.StatusEnum.BLOCK.toString().equalsIgnoreCase(status)) {
            return PnAuditLogEventType.AUD_AK_BLOCK;
        } else {
            return PnAuditLogEventType.AUD_AK_REACTIVATE;
        }
    }

    /**
     * DELETE /virtual-keys/{id} : Eliminazione virtual key
     * Servizio di eliminazione di una virtual key.
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param xPagopaPnCxRole User role (required)
     * @param id Identificativo univoco della virtual key. (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Wrong state transition (status code 409)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteVirtualKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType,
                                                       String xPagopaPnCxId, String xPagopaPnCxRole,
                                                       String id, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        String logMessage = String.format("Cancellazione Virtual Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxRole=%s - id=%s",
                xPagopaPnUid,
                xPagopaPnCxType,
                xPagopaPnCxId,
                xPagopaPnCxRole,
                id);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_DELETE, logMessage)
                .build();

        logEvent.log();

        return virtualKeyService.deleteVirtualKey(id,xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().build();
                });
    }

    @Override
    public Mono<ResponseEntity<VirtualKeysResponseDto>> getVirtualKeys(
            String xPagopaPnUid,
            CxTypeAuthFleetDto xPagopaPnCxType,
            String xPagopaPnCxId,
            String xPagopaPnCxRole,
            List<String> xPagopaPnCxGroups,
            Integer limit,
            String lastKey,
            String lastUpdate,
            Boolean showVirtualKey,
            ServerWebExchange exchange) {
        String logMessage = String.format("getVirtualKeys - xPagopaPnUid: %s, xPagopaPnCxType: %s, xPagopaPnCxId: %s, xPagopaPnCxRole: %s, xPagopaPnCxGroups: %s, limit: %s, lastKey: %s, lastUpdate: %s, showVirtualKey: %s",
                xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups, limit, lastKey, lastUpdate, showVirtualKey);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return virtualKeyService.getVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showVirtualKey)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }
}
