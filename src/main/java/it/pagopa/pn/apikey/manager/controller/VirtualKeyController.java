package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.VirtualKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@CustomLog
@RequiredArgsConstructor
public class VirtualKeyController implements VirtualKeysApi {

    private final VirtualKeyService virtualKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

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
