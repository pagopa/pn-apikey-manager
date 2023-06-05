package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.prvt.api.ApiKeysPrvtApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.prvt.dto.RequestBodyApiKeyPkDto;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static it.pagopa.pn.apikey.manager.constant.ProcessStatus.PROCESS_NAME_API_KEY_PRVT_CHANGE_VIRTUAL_KEY;

@RestController
@lombok.CustomLog
public class ApiKeysPrvtController implements ApiKeysPrvtApi {

    private final ManageApiKeyService manageApiKeyService;

    @Qualifier("apikeyManagerScheduler")
    private final Scheduler scheduler;

    public ApiKeysPrvtController(ManageApiKeyService manageApiKeyService, Scheduler scheduler) {
        this.manageApiKeyService = manageApiKeyService;
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
    public Mono<ResponseEntity<Void>> changeVirtualKeyApiKey(Mono<RequestBodyApiKeyPkDto> requestBodyApiKeyPkDto,
                                                             final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_API_KEY_PRVT_CHANGE_VIRTUAL_KEY);

        return manageApiKeyService.changeVirtualKey(requestBodyApiKeyPkDto)
                .publishOn(scheduler)
                .doOnNext(apiKeyModels -> log.logEndingProcess(PROCESS_NAME_API_KEY_PRVT_CHANGE_VIRTUAL_KEY))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_API_KEY_PRVT_CHANGE_VIRTUAL_KEY,false,throwable.getMessage()))
                .map(s -> ResponseEntity.ok().build());
    }


}
