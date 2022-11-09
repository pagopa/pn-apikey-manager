package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.UsagePlanApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanResponseDto;
import it.pagopa.pn.apikey.manager.service.UsagePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class UsagePlanController implements UsagePlanApi {

    private final UsagePlanService usagePlanService;

    public UsagePlanController(UsagePlanService usagePlanService) {
        this.usagePlanService = usagePlanService;
    }

    @Override
    public Mono<ResponseEntity<UsagePlanResponseDto>> getUsagePlan(final ServerWebExchange exchange) {
        return usagePlanService.getUsagePlanList().map(s -> ResponseEntity.ok().body(s));
    }
}
