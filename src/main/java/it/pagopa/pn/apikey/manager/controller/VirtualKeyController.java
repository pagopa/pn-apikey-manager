package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.VirtualKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@CustomLog
@AllArgsConstructor
public class VirtualKeyController implements VirtualKeysApi {

    private final VirtualKeyService virtualKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

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
}
