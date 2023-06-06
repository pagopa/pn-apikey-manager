package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.api.ApiKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.service.CreateApiKeyService;
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

import java.util.List;

import static it.pagopa.pn.apikey.manager.constant.ProcessStatus.*;

@RestController
@lombok.CustomLog
public class ApiKeysController implements ApiKeysApi {

    private final ManageApiKeyService manageApiKeyService;
    private final CreateApiKeyService createApiKeyService;
    private final PnAuditLogBuilder auditLogBuilder;

    @Qualifier("apikeyManagerScheduler")
    private final Scheduler scheduler;

    public ApiKeysController(ManageApiKeyService manageApiKeyService, CreateApiKeyService createApiKeyService, PnAuditLogBuilder auditLogBuilder, Scheduler scheduler) {
        this.manageApiKeyService = manageApiKeyService;
        this.createApiKeyService = createApiKeyService;
        this.auditLogBuilder = auditLogBuilder;
        this.scheduler = scheduler;
    }

    /**
     * PUT /api-key-self/api-keys/{id}/status : Cambia lo stato dell&#39;api key
     * servizio di cambio stato dell&#39;api key
     *
     * @param xPagopaPnUid           User Identifier (required)
     * @param xPagopaPnCxType        Customer/Receiver Type (required)
     * @param xPagopaPnCxId          Customer/Receiver Identifier (required)
     * @param id                     Identificativo univoco dell&#39;api key (required)
     * @param requestApiKeyStatusDto Action per il cambio stato di un&#39;api key (required)
     * @param xPagopaPnCxGroups      Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. enable an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changeStatusApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                         String id, Mono<RequestApiKeyStatusDto> requestApiKeyStatusDto, List<String> xPagopaPnCxGroups,
                                                         final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_API_KEY_CHANGE_STATUS_API_KEY);

        return manageApiKeyService.changeStatus(id, requestApiKeyStatusDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups)
                .publishOn(scheduler)
                .doOnNext(apiKeyModel -> log.logEndingProcess(PROCESS_NAME_API_KEY_CHANGE_STATUS_API_KEY))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_API_KEY_CHANGE_STATUS_API_KEY,false,throwable.getMessage()))
                .map(s -> ResponseEntity.ok().build());
    }

    /**
     * DELETE /api-key-self/api-keys/{id} : Rimozione api key
     * servizio di rimozione dell&#39;api key
     *
     * @param xPagopaPnUid      User Identifier (required)
     * @param xPagopaPnCxType   Customer/Receiver Type (required)
     * @param xPagopaPnCxId     Customer/Receiver Identifier (required)
     * @param id                Identificativo univoco dell&#39;api key (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. delete an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                    String id, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_API_KEY_DELETE_API_KEY);

        String logMessage = String.format("Cancellazione API Key - IdApiKey=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s",
                id,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_DELETE, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.deleteApiKey(id, xPagopaPnCxType)
                .publishOn(scheduler)
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .doOnNext(s -> log.logEndingProcess(PROCESS_NAME_API_KEY_DELETE_API_KEY))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_API_KEY_DELETE_API_KEY,false,throwable.getMessage()))
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().build();
                });
    }


    /**
     * GET /api-key-self/api-keys : Ricerca api keys
     * servizio di consultazione della lista delle api keys
     *
     * @param xPagopaPnUid      User Identifier (required)
     * @param xPagopaPnCxType   Customer/Receiver Type (required)
     * @param xPagopaPnCxId     Customer/Receiver Identifier (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param limit             (optional)
     * @param lastKey           (optional)
     * @param lastUpdate        (optional)
     * @param showVirtualKey    (optional, default to false)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ApiKeysResponseDto>> getApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey, final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_API_KEY_GET_API_KEYS);

        String logMessage = String.format("Visualizzazione di una API Key - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s",
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();

        return manageApiKeyService.getApiKeyList(xPagopaPnCxId, xPagopaPnCxGroups, limit, lastKey, lastUpdate, showVirtualKey, xPagopaPnCxType)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnNext(response -> log.logEndingProcess(PROCESS_NAME_API_KEY_GET_API_KEYS))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_API_KEY_GET_API_KEYS,false,throwable.getMessage()))
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .publishOn(scheduler);
    }

    /**
     * POST /api-key-self/api-keys : Creazione api key
     * servizio di creazione di un&#39;api key
     *
     * @param xPagopaPnUid        User Identifier (required)
     * @param xPagopaPnCxType     Customer/Receiver Type (required)
     * @param xPagopaPnCxId       Customer/Receiver Identifier (required)
     * @param requestNewApiKeyDto (required)
     * @param xPagopaPnCxGroups   Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponseNewApiKeyDto>> newApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<RequestNewApiKeyDto> requestNewApiKeyDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_API_KEY_NEW_API_KEY);

        String logMessage = String.format("Creazione di una API Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s",
                xPagopaPnUid,
                xPagopaPnCxType.getValue(),
                xPagopaPnCxId,
                xPagopaPnCxGroups);

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_CREATE, logMessage)
                .build();

        logEvent.log();

        return createApiKeyService.createApiKey(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, requestNewApiKeyDto, xPagopaPnCxGroups)
                .map(s -> {
                    logEvent.generateSuccess(logMessage).log();
                    return ResponseEntity.ok().body(s);
                })
                .doOnNext(response -> log.logEndingProcess(PROCESS_NAME_API_KEY_NEW_API_KEY))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_API_KEY_NEW_API_KEY,false,throwable.getMessage()))
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent))
                .publishOn(scheduler);
    }

}
