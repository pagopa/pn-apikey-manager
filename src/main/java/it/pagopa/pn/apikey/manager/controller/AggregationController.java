package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.api.AggregateApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import it.pagopa.pn.apikey.manager.service.PaService;
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

    /**
     * GET /aggregate/{id} : Dettaglio Aggregato
     * servizio per il dettaglio dell&#39;Aggregato
     *
     * @param id Identificativo univoco dell&#39;aggregato (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AggregateResponseDto>> getAggregate(String id, ServerWebExchange exchange) {
        return aggregationService.getAggregate(id)
                .map(aggregateDto -> ResponseEntity.ok().body(aggregateDto))
                .publishOn(scheduler);
    }

    /**
     * GET /aggregate : Ricerca aggregati
     * servizio di consultazione della lista degli aggregati
     *
     * @param name              (optional)
     * @param limit             (optional)
     * @param lastEvaluatedId   (optional)
     * @param lastEvaluatedName (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AggregatesListResponseDto>> getAggregatesList(String name, Integer limit, String lastEvaluatedId, String lastEvaluatedName, ServerWebExchange exchange) {
        return aggregationService.getAggregation(name, limit, lastEvaluatedId, lastEvaluatedName)
                .map(dto -> ResponseEntity.ok().body(dto))
                .publishOn(scheduler);
    }

    /**
     * GET /aggregate/{id}/associated-pa : Lista PA associate
     * servizio per la lista delle PA associate all&#39;aggregato
     *
     * @param id Identificativo univoco dell&#39;aggregato (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<PaAggregateResponseDto>> getPaAggregation(String id, ServerWebExchange exchange) {
        return aggregationService.getPaOfAggregate(id)
                .map(dto -> ResponseEntity.ok().body(dto))
                .publishOn(scheduler);
    }

    /**
     * POST /aggregate/{id}/move-pa : Spostamento PA
     * servizio che si occupa dello spostamento di una PA da un aggregato a un altro
     *
     * @param id                  Identificativo univoco dell&#39;aggregato (required)
     * @param movePaListRequestDto (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<MovePaResponseDto>> movePa(String id, MovePaListRequestDto movePaListRequestDto, final ServerWebExchange exchange) {
        return paService.movePa(id, movePaListRequestDto)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().body(a));
    }
    /**
     * POST /aggregate/{id}/add-pa : Associazione PA - Aggregato
     * servizio che associa una lista di PA a un determinato aggregato
     *
     * @param id                  Identificativo univoco dell&#39;aggregato (required)
     * @param addPaListRequestDto (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
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

    /**
     * DELETE /aggregate/{id} : Rimozione aggregato
     * servizio di rimozione dell&#39;aggregato
     *
     * @param id Identificativo univoco dell&#39;aggregato (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String id, ServerWebExchange exchange) {
        return aggregationService.deleteAggregate(id)
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().build());
    }

    /**
     * GET /aggregate/associable-pa : Lista PA associabili
     * servizio che restituisce la lista della PA associabili all&#39;aggregato
     *
     * @param name (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
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

    /**
     * PUT /aggregate/{id} : Modifica Aggregato
     * Servizio per la modifica di un aggregato
     *
     * @param id                  Identificativo univoco dell&#39;aggregato (required)
     * @param aggregateRequestDto (required)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<SaveAggregateResponseDto>> updateAggregate(String id, AggregateRequestDto aggregateRequestDto, final ServerWebExchange exchange) {
        return aggregationService.updateAggregate(id, aggregateRequestDto)
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }

    /**
     * POST /api-key-bo/aggregate/changePdnd : Cambio valore pdnd
     * servizio che cambia il valore del flag pdnd di un aggregato
     *
     * @param pdndPaDto  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Not found (status code 404)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changePdndPa(PdndPaDto pdndPaDto, ServerWebExchange exchange) {
        return aggregationService.changePdnd(pdndPaDto.getPdnd())
                .publishOn(scheduler)
                .map(a -> ResponseEntity.ok().build());
    }
}
