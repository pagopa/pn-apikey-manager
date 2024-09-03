package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.VirtualKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ResponseNewVirtualKeyDto;
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

@RestController
@CustomLog
@AllArgsConstructor
public class VirtualKeyController implements VirtualKeysApi {

    private final VirtualKeyService virtualKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    /**
     * POST /virtual-keys : Censimento virtual key
     * Servizio di creazione di una nuova virtual key.
     *
     * @param xPagopaPnUid User Identifier (required)
     * @param xPagopaPnCxType Customer/Receiver Type (required)
     * @param xPagopaPnCxId Customer/Receiver Identifier (required)
     * @param requestNewVirtualKeyDto  (required)
     * @return Created (status code 201)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    public Mono<ResponseEntity<ResponseNewVirtualKeyDto>> createVirtualKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<RequestNewVirtualKeyDto> requestNewVirtualKeyDto, final ServerWebExchange exchange) {
        String logMessage = String.format("Creazione di una Virtual Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId
        );

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_CREATE, logMessage)
                .build();

        logEvent.log();

        return virtualKeyService.createVirtualKey(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, requestNewVirtualKeyDto)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }


}
