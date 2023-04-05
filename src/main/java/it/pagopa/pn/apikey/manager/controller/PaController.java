package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.PaApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.GetPaResponseDto;
import it.pagopa.pn.apikey.manager.service.PaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@Slf4j
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
     * servizio di consultazione della lista delle pa
     *
     * @param limit  (optional, default to 10)
     * @param lastKey  (optional)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<GetPaResponseDto>> getPa(Integer limit, String lastKey, ServerWebExchange exchange) {
        return paService.getPaList(limit,lastKey)
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }

}
