package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.VirtualKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@lombok.CustomLog
public class VirtualKeyController implements VirtualKeysApi {

    private final VirtualKeyService virtualKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    public VirtualKeyController(VirtualKeyService virtualKeyService, PnAuditLogBuilder auditLogBuilder) {
        this.virtualKeyService = virtualKeyService;
        this.auditLogBuilder = auditLogBuilder;
    }

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
                    String logMessage = String.format("Start cambio stato virtualKey - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxRole=%s, id=%s, status=%s",
                            xPagopaPnUid,
                            xPagopaPnCxType.getValue(),
                            xPagopaPnCxId,
                            xPagopaPnCxRole,
                            id,
                            dto.getStatus());

                    PnAuditLogEvent logEvent = auditLogBuilder
                            .before(selectAuditLogEventType(dto.getStatus().toString()), logMessage)
                            .build();

                    logEvent.log();
                    return virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, id, dto, xPagopaPnCxGroups)
                            .doOnError(throwable -> {
                                CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent);
                                if (throwable instanceof ApiKeyManagerException ex) {
                                    if (ex.getStatus() == HttpStatus.BAD_REQUEST) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
                                    } else if (ex.getStatus() == HttpStatus.CONFLICT) {
                                        throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
                                    } else if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
                                    } else if (ex.getStatus() == HttpStatus.FORBIDDEN) {
                                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
                                    }
                                }
                                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
                            })
                            .then(Mono.defer(() -> {
                                logEvent.generateSuccess(logMessage)
                                        .log();
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
}

