package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.prvt.api.ApiKeysPrvtApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.prvt.dto.RequestBodyApiKeyPkDto;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class ApiKeysPrvtController implements ApiKeysPrvtApi {

    private final ManageApiKeyService manageApiKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    @Qualifier("apikeyManagerScheduler")
    private final Scheduler scheduler;

    public ApiKeysPrvtController(ManageApiKeyService manageApiKeyService, PnAuditLogBuilder auditLogBuilder, Scheduler scheduler) {
        this.manageApiKeyService = manageApiKeyService;
        this.auditLogBuilder = auditLogBuilder;
        this.scheduler = scheduler;
    }

    /**
     * POST /api-key-prvt/api-keys/associate-api-key : Cambia la virtual key di un api key dato un cxId
     * servizio di cambio virtual key di un api key dato un cxId
     *
     * @param requestBodyApiKeyPkDto  (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Wrong state transition (i.e. enable an enabled key) (status code 409)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changeVirtualKeyApiKey(RequestBodyApiKeyPkDto requestBodyApiKeyPkDto,
                                                             final ServerWebExchange exchange) {
        String logMessage = String.format("Cambio virtual Key API Key - xPagopaPnCxId=%s - VirtualKey=%s",
                requestBodyApiKeyPkDto.getxPagopaPnCxId(),
                requestBodyApiKeyPkDto.getVirtualKey());

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_CREATE, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.changeVirtualKey(requestBodyApiKeyPkDto.getxPagopaPnCxId(), requestBodyApiKeyPkDto.getVirtualKey())
                .publishOn(scheduler)
                .doOnError(throwable -> {
                    if(CheckExceptionUtils.checkExceptionStatusForLogLevel(throwable))
                        logEvent.generateWarning(throwable.getMessage()).log();
                    else
                        logEvent.generateFailure(throwable.getMessage()).log();
                })
                .map(s -> {
                    logEvent.generateSuccess().log();
                    return ResponseEntity.ok().build();
                });
    }


}
