package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.api.UsagePlanApi;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.UsagePlanResponseDto;
import it.pagopa.pn.apikey.manager.service.UsagePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@lombok.CustomLog
public class UsagePlanController implements UsagePlanApi {

    private final UsagePlanService usagePlanService;

    public UsagePlanController(UsagePlanService usagePlanService) {
        this.usagePlanService = usagePlanService;
    }

    /**
     * GET /usage-plan : Lista usagePlan
     * servizio che restituisce la lista dei template per gli usage plan
     *
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<UsagePlanResponseDto>> getUsagePlan(final ServerWebExchange exchange) {
        return usagePlanService.getUsagePlanList()
                .map(s -> ResponseEntity.ok().body(s));
    }
}
