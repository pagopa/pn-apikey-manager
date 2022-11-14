package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.AggregationConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.PaAggregationPageable;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;

@Service
@Slf4j
public class AggregationService {

    private final AggregateRepository aggregateRepository;
    private final PaAggregationRepository paAggregationRepository;
    private final UsagePlanService usagePlanService;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;
    private final AggregationConverter aggregationConverter;
    private final ApiGatewayService apiGatewayService;

    public AggregationService(AggregateRepository aggregateRepository,
                              PaAggregationRepository paAggregationRepository,
                              UsagePlanService usagePlanService,
                              PnApikeyManagerConfig pnApikeyManagerConfig, AggregationConverter aggregationConverter, ApiGatewayService apiGatewayService) {
        this.aggregateRepository = aggregateRepository;
        this.paAggregationRepository = paAggregationRepository;
        this.usagePlanService = usagePlanService;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
        this.aggregationConverter = aggregationConverter;
        this.apiGatewayService = apiGatewayService;
    }

    public Mono<AggregatesListResponseDto> getAggregation(@Nullable String name, @NonNull AggregatePageable pageable) {
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

    public Mono<PaAggregateResponseDto> getPaOfAggregate(String aggregateId) {
        log.debug("get pa of aggregate with id: {}", aggregateId);
        return aggregateRepository.getApiKeyAggregation(aggregateId)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(aggregate -> paAggregationRepository.findByAggregateId(aggregateId, PaAggregationPageable.createEmpty()))
                .map(aggregationConverter::convertToResponseDto);
    }

    public Mono<Void> deleteAggregate(String aggregateId) {
        log.debug("deleting aggregation with id {}", aggregateId);
        return paAggregationRepository.findByAggregateId(aggregateId, PaAggregationPageable.createWithLimit(1))
                .flatMap(page -> CollectionUtils.isEmpty(page.items()) ? Mono.just(page) : Mono.error(new ApiKeyManagerException(AGGREGATE_INVALID_STATUS, HttpStatus.BAD_REQUEST)))
                .flatMap(page -> aggregateRepository.delete(aggregateId))
                .doOnNext(aggregate -> log.info("aggregate {} deleted", aggregate.getAggregateId()))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .then();
    }

    public Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregationId) {
        return aggregateRepository.getApiKeyAggregation(aggregationId);
    }

    public Mono<String> addAwsApiKeyToAggregate(CreateApiKeyResponse createApiKeyResponse, ApiKeyAggregateModel aggregate) {
        aggregate.setLastUpdate(LocalDateTime.now());
        aggregate.setApiKeyId(createApiKeyResponse.id());
        aggregate.setApiKey(createApiKeyResponse.value());
        return aggregateRepository.saveAggregation(aggregate).map(ApiKeyAggregateModel::getAggregateId);
    }

    public Mono<ApiKeyAggregateModel> createNewAggregate(String paId) {
        ApiKeyAggregateModel newApiKeyAggregateModel = new ApiKeyAggregateModel();
        newApiKeyAggregateModel.setAggregateId(UUID.randomUUID().toString());
        newApiKeyAggregateModel.setName("AGG_" + paId);
        newApiKeyAggregateModel.setUsagePlanId(pnApikeyManagerConfig.getDefaultPlan());
        newApiKeyAggregateModel.setLastUpdate(LocalDateTime.now());
        newApiKeyAggregateModel.setCreatedAt(LocalDateTime.now());
        return aggregateRepository.saveAggregation(newApiKeyAggregateModel);
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

    public Mono<SaveAggregateResponseDto> updateAggregate(String id, AggregateRequestDto aggregateRequestDto) {
        return aggregateRepository.findById(id)
                .flatMap(apiKeyAggregateModel -> updateAggregateModel(apiKeyAggregateModel, aggregateRequestDto));
    }

    private Mono<SaveAggregateResponseDto> updateAggregateModel(ApiKeyAggregateModel apiKeyAggregateModel, AggregateRequestDto aggregateRequestDto) {
        apiKeyAggregateModel.setName(aggregateRequestDto.getName());
        apiKeyAggregateModel.setDescription(aggregateRequestDto.getDescription());
        return aggregateRepository.saveAggregation(apiKeyAggregateModel)
                .doOnNext(apiKeyAggregateModel1 -> log.info("save ApiKeyAggregateModel for aggregateId: {}", apiKeyAggregateModel.getAggregateId()))
                .flatMap(apiKeyAggregateModel1 -> {
                    if (StringUtils.hasText(aggregateRequestDto.getUsagePlanId())) {
                        return apiGatewayService.moveApiKeyToNewUsagePlan(apiKeyAggregateModel, aggregateRequestDto)
                                .flatMap(createUsagePlanKeyResponse -> convertToAggregateResponseDto(apiKeyAggregateModel.getAggregateId()));
                    }
                    return convertToAggregateResponseDto(apiKeyAggregateModel.getAggregateId());
                });
    }

    private Mono<SaveAggregateResponseDto> convertToAggregateResponseDto(String aggregateId) {
        SaveAggregateResponseDto saveAggregateResponseDto = new SaveAggregateResponseDto();
        saveAggregateResponseDto.setId(aggregateId);
        return Mono.just(saveAggregateResponseDto);
    }

}
