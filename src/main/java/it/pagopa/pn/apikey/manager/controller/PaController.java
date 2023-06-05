package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.PaApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.GetPaResponseDto;
import it.pagopa.pn.apikey.manager.service.PaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static it.pagopa.pn.apikey.manager.constant.ProcessStatus.PROCESS_NAME_PA_GET_PA;

@RestController
@lombok.CustomLog
public class PaController implements PaApi {

    private final Scheduler scheduler;
    private final PaService paService;


    public PaController(PaService paService,
                        @Qualifier("apikeyManagerScheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
        this.paService = paService;
    }

    /**
     * GET /api-key-bo/pa : Ricerca pa
     * Servizio di consultazione della lista delle PA
     *
     * @param paName  (optional)
     * @param limit  (optional)
     * @param lastEvaluatedId  (optional)
     * @param lastEvaluatedName  (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<GetPaResponseDto>> getPa(String paName,
                                                        Integer limit,
                                                        String lastEvaluatedId,
                                                        String lastEvaluatedName,
                                                        final ServerWebExchange exchange) {
        log.logStartingProcess(PROCESS_NAME_PA_GET_PA);

        return paService.getPa(paName, limit, lastEvaluatedId, lastEvaluatedName)
                .map(s -> ResponseEntity.ok().body(s))
                .doOnNext(apiKeyModels -> log.logEndingProcess(PROCESS_NAME_PA_GET_PA))
                .doOnError(throwable -> log.logEndingProcess(PROCESS_NAME_PA_GET_PA,false,throwable.getMessage()))
                .publishOn(scheduler);
    }

}
