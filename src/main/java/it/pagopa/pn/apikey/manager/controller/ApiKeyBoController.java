package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.api.ApiKeysBoApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.RequestPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.ResponseApiKeysDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.ResponsePdndDto;
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
@lombok.CustomLog
public class ApiKeyBoController implements ApiKeysBoApi {

    private final Scheduler scheduler;
    private final ManageApiKeyService manageApiKeyService;
    private final PnAuditLogBuilder auditLogBuilder;


    public ApiKeyBoController(ManageApiKeyService manageApiKeyService,
                              PnAuditLogBuilder auditLogBuilder,
                              @Qualifier("apikeyManagerScheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
        this.manageApiKeyService = manageApiKeyService;
        this.auditLogBuilder = auditLogBuilder;
    }

    /**
     * GET /api-key-bo/api-keys : Ricerca api keys
     * Servizio di consultazione della lista delle virtual API Keys dato l&#39;id della PA
     *
     * @param paId  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponseApiKeysDto>> getBoApiKeys(String paId, ServerWebExchange exchange) {
        String logMessage = String.format("Visualizzazione di una API Key - xPagopaPnCxId=%s", paId);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.getBoApiKeyList(paId)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .publishOn(scheduler);
    }

    /**
     * PUT /api-key-bo/api-keys/interop : Cambio valore pdnd
     * Servizio che cambia il valore del flag pdnd di una o più virtual API Key
     *
     * @param requestPdndDto  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponsePdndDto>> interop(Mono<RequestPdndDto> requestPdndDto, ServerWebExchange exchange) {
        return manageApiKeyService.changePdnd(requestPdndDto)
                .publishOn(scheduler)
                .map(s -> ResponseEntity.ok().body(s));
    }
}
