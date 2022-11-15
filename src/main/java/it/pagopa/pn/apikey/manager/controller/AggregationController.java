package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.AggregateApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.service.PaService;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.INVALID_NAME_LENGTH;

@RestController
@Slf4j
public class AggregationController implements AggregateApi {

    private final Scheduler scheduler;
    private final AggregationService aggregationService;
    private final PaService paService;

    public AggregationController(@Qualifier("apikeyManagerScheduler") Scheduler scheduler,
                                 AggregationService aggregationService,
                                 PaService paService) {
        this.scheduler = scheduler;
        this.aggregationService = aggregationService;
        this.paService = paService;
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
                .lastEvaluatedName(lastEvaluatedName)
                .build();
        return aggregationService.getAggregation(name, pageable)
                .map(dto -> ResponseEntity.ok().body(dto))
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<PaAggregateResponseDto>> getPaAggregation(String id, ServerWebExchange exchange) {
        return aggregationService.getPaOfAggregate(id)
                .map(dto -> ResponseEntity.ok().body(dto))
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<MovePaResponseDto>> movePa(String id, AddPaListRequestDto addPaListRequestDto, final ServerWebExchange exchange) {
        return paService.movePa(id, addPaListRequestDto)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().body(a));
    }

    @Override
    public Mono<ResponseEntity<MovePaResponseDto>> addPaListToAggregate(String id, AddPaListRequestDto addPaListRequestDto, ServerWebExchange exchange) {
        return paService.createNewPaAggregation(id, addPaListRequestDto)
                .publishOn(scheduler)
                .map(dto -> ResponseEntity.ok().body(dto));
    }

    @Override
    public Mono<ResponseEntity<SaveAggregateResponseDto>> createAggregate(AggregateRequestDto aggregateRequestDto, ServerWebExchange exchange) {
        return aggregationService.createAggregate(aggregateRequestDto)
                .publishOn(scheduler)
                .map(dto -> ResponseEntity.ok().body(dto));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String id, ServerWebExchange exchange) {
        return aggregationService.deleteAggregate(id)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<AssociablePaResponseDto>> getAssociablePa(String name, final ServerWebExchange exchange) {
        if (name != null && name.length() < 3) {
            throw new ApiKeyManagerException(INVALID_NAME_LENGTH, HttpStatus.BAD_REQUEST);
        }
        return paService.getAssociablePa(name)
                .doOnNext(associablePaResponseDto -> log.info("getAssociablePA return list with size: {}", associablePaResponseDto.getItems().size()))
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }

    @Override
    public Mono<ResponseEntity<SaveAggregateResponseDto>> updateAggregate(String id, AggregateRequestDto aggregateRequestDto, final ServerWebExchange exchange) {
        return aggregationService.updateAggregate(id, aggregateRequestDto)
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }
}
