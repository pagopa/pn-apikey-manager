package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerUsagePlanConfig;
import it.pagopa.pn.apikey.manager.converter.AggregationConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.PaAggregationPageable;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.model.CreateApiKeyResponse;
import software.amazon.awssdk.services.apigateway.model.CreateUsagePlanKeyResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.AGGREGATE_INVALID_STATUS;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.AGGREGATE_NOT_FOUND;

@Service
@Slf4j
public class AggregationService {

    private final AggregateRepository aggregateRepository;
    private final PaAggregationRepository paAggregationRepository;
    private final PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfig;
    private final UsagePlanService usagePlanService;
    private final AggregationConverter aggregationConverter;
    private final ApiGatewayService apiGatewayService;

    public AggregationService(AggregateRepository aggregateRepository,
                              PaAggregationRepository paAggregationRepository,
                              PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfig,
                              UsagePlanService usagePlanService,
                              AggregationConverter aggregationConverter,
                              ApiGatewayService apiGatewayService) {
        this.aggregateRepository = aggregateRepository;
        this.paAggregationRepository = paAggregationRepository;
        this.pnApikeyManagerUsagePlanConfig = pnApikeyManagerUsagePlanConfig;
        this.usagePlanService = usagePlanService;
        this.aggregationConverter = aggregationConverter;
        this.apiGatewayService = apiGatewayService;
    }

    /**
     * Ottiene la lista degli aggregati, disponibile la paginazione e il filtro per name.
     * @param name filtro per nome (opzionale)
     * @param limit parametro per la paginazione
     * @param lastEvaluatedId parametro per la paginazione
     * @param lastEvaluatedName parametro per la paginazione
     * @return Lista degli aggregati
     */
    public Mono<AggregatesListResponseDto> getAggregation(@Nullable String name,
                                                          @Nullable Integer limit,
                                                          @Nullable String lastEvaluatedId,
                                                          @Nullable String lastEvaluatedName) {
        AggregatePageable pageable = toAggregatePageable(limit, lastEvaluatedId, lastEvaluatedName);
        log.debug("get aggregation - name: {} - pageable: {}", name, pageable);
        if (StringUtils.hasText(name)) {
            return aggregateRepository.findByName(name, pageable)
                    .doOnNext(page -> log.info("filter by name {} - size: {} - lastKey: {}", name, page.items().size(), page.lastEvaluatedKey()))
                    .zipWhen(this::getUsagePlanFromAggregationPage)
                    .map(tuple -> aggregationConverter.convertToResponseDto(tuple.getT1(), tuple.getT2()))
                    .zipWhen(dto -> aggregateRepository.countByName(name))
                    .doOnNext(tuple -> tuple.getT1().setTotal(tuple.getT2()))
                    .map(Tuple2::getT1);
        }
        return aggregateRepository.findAll(pageable)
                .doOnNext(page -> log.info("get all - size: {} - lastKey: {}", page.items().size(), page.lastEvaluatedKey()))
                .zipWhen(this::getUsagePlanFromAggregationPage)
                .map(tuple -> aggregationConverter.convertToResponseDto(tuple.getT1(), tuple.getT2()))
                .zipWhen(dto -> aggregateRepository.count())
                .doOnNext(tuple -> tuple.getT1().setTotal(tuple.getT2()))
                .map(Tuple2::getT1);
    }

    /**
     * Ottiene il dettaglio di un aggregato. Include le informazioni dello Usage Plan associato alla sua AWS API Key.
     * @param aggregateId id dell'aggregato
     * @return Il dettaglio dell'aggregato
     */
    public Mono<AggregateResponseDto> getAggregate(String aggregateId) {
        log.debug("get aggregate with id: {}", aggregateId);
        return aggregateRepository.getApiKeyAggregation(aggregateId)
                .zipWhen(aggregate -> {
                    if (StringUtils.hasText(aggregate.getUsagePlanId())) {
                        return usagePlanService.getUsagePlan(aggregate.getUsagePlanId())
                                .map(Optional::of)
                                .defaultIfEmpty(Optional.empty());
                    }
                    return Mono.<UsagePlanDetailDto>empty().map(Optional::of).defaultIfEmpty(Optional.empty());
                })
                .map(t -> aggregationConverter.convertToResponseDto(t.getT1(), t.getT2().orElse(null)))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)));
    }

    /**
     * Ottiene la lista delle PA associate a un aggregato.
     * @param aggregateId id dell'aggregato
     * @return La lista delle PA associate
     */
    public Mono<PaAggregateResponseDto> getPaOfAggregate(String aggregateId) {
        log.debug("get pa of aggregate with id: {}", aggregateId);
        return aggregateRepository.getApiKeyAggregation(aggregateId)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(aggregate -> paAggregationRepository.findByAggregateId(aggregateId, PaAggregationPageable.createEmpty()))
                .map(aggregationConverter::convertToResponseDto);
    }

    /**
     * Creazione di un nuovo aggregato, che comprende la generazione di una API Key fisica e associazione allo usage plan.
     * @param requestDto modello della richiesta
     * @return La risposta contiene l'id del nuovo aggregato creato
     */
    public Mono<SaveAggregateResponseDto> createAggregate(Mono<AggregateRequestDto> requestDto) {
        log.debug("creating aggregate request: {}", requestDto);
        return requestDto.map(aggregationConverter::convertToModel)
                .flatMap(model -> aggregateRepository.saveAggregation(model)
                        .zipWhen(this::createAwsApiKey)
                        .flatMap(tuple -> addAwsApiKeyToAggregate(tuple.getT2(), tuple.getT1()))
                        .flatMap(aggregate -> addUsagePlanToKey(aggregate).map(response -> aggregate))
                        .doOnNext(aggregate -> log.info("Create aggregate: {}", aggregate))
                        .onErrorResume(e -> deleteAggregate(model.getAggregateId())
                                .doOnSuccess(a -> log.info("rollback aggregate {} done", model.getAggregateId()))
                                .doOnError(re -> log.error("can not execute rollback of aggregate {}", model.getAggregateId(), re))
                                .then(Mono.error(e)))
                        .map(aggregate -> {
                            SaveAggregateResponseDto dto = new SaveAggregateResponseDto();
                            dto.setId(aggregate.getAggregateId());
                            return dto;
                        }));
    }

    /**
     * Cancellazione di un aggregato, che comprende la cancellazione dell'eventuale API Key fisica associata.
     * Per cancellare un aggregato, questo non deve avere PA associate.
     * @param aggregateId id dell'aggregato da eliminare
     * @return Un Mono
     */
    public Mono<Void> deleteAggregate(String aggregateId) {
        log.debug("deleting aggregate with id {}", aggregateId);
        return paAggregationRepository.findByAggregateId(aggregateId, PaAggregationPageable.createWithLimit(1))
                .flatMap(page -> CollectionUtils.isEmpty(page.items()) ? Mono.just(page) : Mono.error(new ApiKeyManagerException(AGGREGATE_INVALID_STATUS, HttpStatus.BAD_REQUEST)))
                .flatMap(page -> aggregateRepository.getApiKeyAggregation(aggregateId))
                .flatMap(aggregate -> {
                    if (StringUtils.hasText(aggregate.getApiKeyId())) {
                        return apiGatewayService.deleteAwsApiKey(aggregate.getApiKeyId()).map(response -> aggregate);
                    }
                    log.warn("deleting aggregate {} without AWS Api Key", aggregateId);
                    return Mono.just(aggregate);
                })
                .flatMap(aggregate -> aggregateRepository.delete(aggregateId))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .then();
    }

    public Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregationId) {
        return aggregateRepository.getApiKeyAggregation(aggregationId);
    }

    /**
     * Associa una AWS API Key all'aggregato, salvando l'aggregato aggiornato sul DB.
     * @param createApiKeyResponse informazioni sull'API Key
     * @param aggregate aggregato
     * @return L'aggregato salvato
     */
    public Mono<ApiKeyAggregateModel> addAwsApiKeyToAggregate(CreateApiKeyResponse createApiKeyResponse, ApiKeyAggregateModel aggregate) {
        aggregate.setLastUpdate(LocalDateTime.now());
        aggregate.setApiKeyId(createApiKeyResponse.id());
        aggregate.setApiKey(createApiKeyResponse.value());
        return aggregateRepository.saveAggregation(aggregate);
    }

    /**
     * Creazione di un aggregato dato l'id della PA. Questo metodo provvede solo alla creazione dell'aggregato e non
     * alla creazione dell'API Key con relativa associazione dello Usage Plan.
     * @param internalPaDetailDto DTO della PA da associare all'aggregato
     * @return L'aggregato salvato
     */
    public Mono<String> createNewAggregate(InternalPaDetailDto internalPaDetailDto) {
        AggregateRequestDto dto = new AggregateRequestDto();
        dto.setName("AGG_" + internalPaDetailDto.getName());
        dto.setUsagePlanId(pnApikeyManagerUsagePlanConfig.getDefaultPlan());
        return createAggregate(Mono.just(dto)).map(SaveAggregateResponseDto::getId);
    }

    private Mono<CreateApiKeyResponse> createAwsApiKey(ApiKeyAggregateModel aggregate) {
        return apiGatewayService.createNewAwsApiKey(aggregate.getName())
                .doOnNext(response -> log.info("Created AWS Api Key {} with name {} for aggregate {}", response.id(), response.name(), aggregate.getAggregateId()));
    }

    private Mono<CreateUsagePlanKeyResponse> addUsagePlanToKey(ApiKeyAggregateModel aggregate) {
        return apiGatewayService.addUsagePlanToApiKey(aggregate.getUsagePlanId(), aggregate.getApiKeyId())
                .doOnNext(signal -> log.info("Added AWS Usage Plan {} to Api Key {} for aggregate {}", aggregate.getUsagePlanId(), aggregate.getApiKeyId(), aggregate.getAggregateId()));
    }

    private Mono<List<UsagePlanDetailDto>> getUsagePlanFromAggregationPage(Page<ApiKeyAggregateModel> page) {
        return Flux.fromStream(page.items().stream()
                        .map(ApiKeyAggregateModel::getUsagePlanId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(usagePlanService::getUsagePlan))
                .flatMap(Function.identity())
                .collectList();
    }


    public Mono<SaveAggregateResponseDto> updateAggregate(String id, Mono<AggregateRequestDto> aggregateRequest) {
        return aggregateRequest.flatMap(aggregateRequestDto -> aggregateRepository.findById(id)
                .flatMap(apiKeyAggregateModel -> updateAggregateModel(apiKeyAggregateModel, aggregateRequestDto)));
    }

    private Mono<SaveAggregateResponseDto> updateAggregateModel(ApiKeyAggregateModel aggregate, AggregateRequestDto aggregateRequestDto) {
        ApiKeyAggregateModel oldApiKeyAggregate = new ApiKeyAggregateModel(aggregate);
        if (StringUtils.hasText(aggregateRequestDto.getName())) {
            aggregate.setName(aggregateRequestDto.getName());
        }
        if (StringUtils.hasText(aggregateRequestDto.getDescription())) {
            aggregate.setDescription(aggregateRequestDto.getDescription());
        }
        boolean isUsagePlanChanged = StringUtils.hasText(aggregateRequestDto.getUsagePlanId())
                && !aggregateRequestDto.getUsagePlanId().equalsIgnoreCase(aggregate.getUsagePlanId());
        if (isUsagePlanChanged) {
            log.info("aggregate {} usage plan changed from {} to {}", aggregate.getAggregateId(), aggregate.getUsagePlanId(), aggregateRequestDto.getUsagePlanId());
            aggregate.setUsagePlanId(aggregateRequestDto.getUsagePlanId());
        }
        return aggregateRepository.saveAggregation(aggregate)
                .flatMap(model -> {
                    if (isUsagePlanChanged) {
                        return apiGatewayService.moveApiKeyToNewUsagePlan(oldApiKeyAggregate, aggregate)
                                .onErrorResume(e -> aggregateRepository.saveAggregation(oldApiKeyAggregate)
                                        .doOnNext(a -> log.info("rollback aggregate {} done", a.getAggregateId()))
                                        .then(Mono.error(e)))
                                .flatMap(createUsagePlanKeyResponse -> convertToAggregateResponseDto(aggregate.getAggregateId()));
                    }
                    return convertToAggregateResponseDto(aggregate.getAggregateId());
                });
    }

    private Mono<SaveAggregateResponseDto> convertToAggregateResponseDto(String aggregateId) {
        SaveAggregateResponseDto saveAggregateResponseDto = new SaveAggregateResponseDto();
        saveAggregateResponseDto.setId(aggregateId);
        return Mono.just(saveAggregateResponseDto);
    }

    private AggregatePageable toAggregatePageable(Integer limit, String lastEvaluatedId, String lastEvaluatedName) {
        return AggregatePageable.builder()
                .limit(limit)
                .lastEvaluatedId(lastEvaluatedId)
                .lastEvaluatedName(lastEvaluatedName)
                .build();
    }

}
