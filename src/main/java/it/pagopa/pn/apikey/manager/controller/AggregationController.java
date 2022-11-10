package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.AggregateApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
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
        return aggregationService.getAggregate(id)
                .map(aggregateDto -> ResponseEntity.ok().body(aggregateDto))
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<AggregatesListResponseDto>> getAggregatesList(String name, Integer limit, String lastEvaluatedId, String lastEvaluatedName, ServerWebExchange exchange) {
        AggregatePageable pageable = AggregatePageable.builder()
                .limit(limit)
                .lastEvaluatedId(lastEvaluatedId)
                .build();
        return aggregationService.getAggregation(name, pageable)
                .map(dto -> ResponseEntity.ok().body(dto))
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<AssociablePaResponseDto>> getAssociablePa(String name, ServerWebExchange exchange) {
        return AggregateApi.super.getAssociablePa(name, exchange).publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<MovePaResponseDto>> addPaListToAggregate(String id, AddPaListRequestDto addPaListRequestDto, ServerWebExchange exchange) {
        return AggregateApi.super.addPaListToAggregate(id, addPaListRequestDto, exchange).publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String id, ServerWebExchange exchange) {
        return aggregationService.deleteAggregation(id)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().build());
    }

}
