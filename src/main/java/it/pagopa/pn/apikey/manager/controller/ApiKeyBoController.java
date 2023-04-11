package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.ApiKeysBoApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.RequestPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponseApiKeysDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponsePdndDto;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.stream.Collectors;

@RestController
@Slf4j
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
     * GET /api-key-bo/api-keys/{id} : Ricerca api keys
     * servizio di consultazione della lista delle api keys dato un id della PA
     *
     * @param id (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponseApiKeysDto>> getBoApiKeys(String id, ServerWebExchange exchange) {
        String logMessage = String.format("Visualizzazione di una API Key - xPagopaPnCxId=%s", id);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.getBoApiKeyList(id)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .publishOn(scheduler);
    }

    /**
     * POST /api-key-bo/changePdnd : Cambio valore pdnd
     * servizio che cambia il valore del flag pdnd di una api key
     *
     * @param requestPdndDto  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponsePdndDto>> changePdnd(RequestPdndDto requestPdndDto, ServerWebExchange exchange) {

        String logMessage = String.format("Cambio valore Pdnd di una o più API Key - Ids=%s - Pdnd=%s",
                requestPdndDto.getItems().stream().map(ApiPdndDto::getId).collect(Collectors.toList()),
                requestPdndDto.getItems().stream().map(ApiPdndDto::getPdnd).collect(Collectors.toList()));

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.changePdnd(requestPdndDto.getItems())
                .publishOn(scheduler)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .map(s -> {
                    logEvent.generateSuccess().log();
                    return ResponseEntity.ok().body(s);
                });
    }
}
