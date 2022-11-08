package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.AggregateApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class AggregationController implements AggregateApi {

    private final Scheduler scheduler;
    private final AggregationService aggregationService;

    public AggregationController(@Qualifier("apikeyManagerScheduler") Scheduler scheduler,
                                 AggregationService aggregationService) {
        this.scheduler = scheduler;
        this.aggregationService = aggregationService;
    }

    @Override
    public Mono<ResponseEntity<AggregateResponseDto>> getAggregate(String id, ServerWebExchange exchange) {
        return AggregateApi.super.getAggregate(id, exchange).publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<AggregatesListResponseDto>> getAggregatesList(String name, Integer limit, String lastEvaluatedId, String lastEvaluatedName, ServerWebExchange exchange) {
        return AggregateApi.super.getAggregatesList(name, limit, lastEvaluatedId, lastEvaluatedName, exchange)
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<AssociablePaResponseDto>> getAssociablePa(ServerWebExchange exchange) {
        return AggregateApi.super.getAssociablePa(exchange).publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<Void>> addPaListToAggregate(String id, AddPaListRequestDto addPaListRequestDto, ServerWebExchange exchange) {
        return AggregateApi.super.addPaListToAggregate(id, addPaListRequestDto, exchange).publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String id, ServerWebExchange exchange) {
        return aggregationService.deleteAggregation(id)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().build());
    }

}
