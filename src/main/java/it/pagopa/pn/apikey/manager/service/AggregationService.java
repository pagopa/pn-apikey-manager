package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.AggregationConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
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
    private final ApiGatewayAsyncClient apiGatewayAsyncClient;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;
    private final UsagePlanService usagePlanService;
    private final AggregationConverter aggregationConverter;

    public AggregationService(AggregateRepository aggregateRepository,
                              PaAggregationRepository paAggregationRepository,
                              ApiGatewayAsyncClient apiGatewayAsyncClient,
                              PnApikeyManagerConfig pnApikeyManagerConfig,
                              UsagePlanService usagePlanService,
                              AggregationConverter aggregationConverter) {
        this.aggregateRepository = aggregateRepository;
        this.paAggregationRepository = paAggregationRepository;
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
        this.usagePlanService = usagePlanService;
        this.aggregationConverter = aggregationConverter;
    }

    public Mono<AggregatesListResponseDto> getAggregation(@Nullable String name, @NonNull AggregatePageable pageable) {
        log.debug("get aggregation - name: {} - pageable: {}", name, pageable);
        if (StringUtils.hasText(name)) {
            return aggregateRepository.findByName(name, pageable)
                    .doOnNext(page -> log.info("filter by name {} - size: {} - lastKey: {}", name, page.items().size(), page.lastEvaluatedKey()))
                    .zipWhen(this::getUsagePlanFromAggregationPage)
                    .map(tuple -> aggregationConverter.convertResponseDto(tuple.getT1(), tuple.getT2()));
        }
        return aggregateRepository.findAll(pageable)
                .doOnNext(page -> log.info("get all - size: {} - lastKey: {}", page.items().size(), page.lastEvaluatedKey()))
                .zipWhen(this::getUsagePlanFromAggregationPage)
                .map(tuple -> aggregationConverter.convertResponseDto(tuple.getT1(), tuple.getT2()));
    }

    public Mono<Void> deleteAggregation(String aggregateId) {
        log.debug("deleting aggregation with id {}", aggregateId);
        return paAggregationRepository.findByAggregateId(aggregateId, null, null)
                .flatMap(page -> CollectionUtils.isEmpty(page.items()) ? Mono.just(page) : Mono.error(new ApiKeyManagerException(AGGREGATE_INVALID_STATUS, HttpStatus.BAD_REQUEST)))
                .flatMap(page -> aggregateRepository.delete(aggregateId))
                .doOnNext(aggregate -> log.info("aggregate {} deleted", aggregate.getAggregateId()))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .then();
    }

    public Mono<CreateApiKeyResponse> createNewAwsApiKey(String aggregateName) {
        CreateApiKeyRequest createApiKeyRequest = constructApiKeyRequest(aggregateName);
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .doOnNext(createApiKeyResponse -> log.info("Created AWS ApiKey with name: {}", createApiKeyResponse.name()))
                .flatMap(createApiKeyResponse -> createUsagePlan(aggregateName, createApiKeyResponse.id())
                        .map(createUsagePlanKeyResponse -> createApiKeyResponse));
    }

    public Mono<CreateUsagePlanKeyResponse> createUsagePlan(String aggregateName, String apiKeyId) {
        CreateUsagePlanRequest createUsagePlanRequest = constructUsagePlanRequest(aggregateName);
        log.debug("CreateUsagePlanRequest with name: {}, quota: {}, throttle: {}, stage: {}",
                createUsagePlanRequest.name(), createUsagePlanRequest.quota(), createUsagePlanRequest.throttle(), createUsagePlanRequest.apiStages());

        return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlan(createUsagePlanRequest))
                .doOnNext(createUsagePlanResponse -> log.info("Created AWS usagePlan with name: {}", createUsagePlanRequest.name()))
                .flatMap(createUsagePlanResponse -> createUsagePlanKey(createUsagePlanResponse,apiKeyId));
    }

    private Mono<CreateUsagePlanKeyResponse> createUsagePlanKey(CreateUsagePlanResponse createUsagePlanResponse, String id) {
        CreateUsagePlanKeyRequest createUsagePlanKeyRequest = constructUsagePlanKeyRequest(createUsagePlanResponse, id);
        log.debug("CreateUsagePlanKeyRequest with KeyType: {}, usagePlanId: {}",
                createUsagePlanKeyRequest.keyType(), createUsagePlanKeyRequest.usagePlanId());

        return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                .doOnNext(createUsagePlanKeyResponse -> log.info("Created AWS usagePlanKey with id: {}, keyId: {}",
                        createUsagePlanKeyRequest.usagePlanId(), createUsagePlanKeyRequest.keyId()));
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
        newApiKeyAggregateModel.setName("AGG_"+paId);
        newApiKeyAggregateModel.setLastUpdate(LocalDateTime.now());
        newApiKeyAggregateModel.setCreatedAt(LocalDateTime.now());
        return aggregateRepository.saveAggregation(newApiKeyAggregateModel);
    }

    private CreateApiKeyRequest constructApiKeyRequest(String aggregateName) {
        return CreateApiKeyRequest.builder()
                .name("pn_" + aggregateName + "_apikey")
                .enabled(true)
                .build();
    }

    private CreateUsagePlanRequest constructUsagePlanRequest(String aggregateName) {
        return CreateUsagePlanRequest.builder()
                .name("pn_" + aggregateName + "_medium")
                .throttle(t -> t.burstLimit(3000).rateLimit(10000.0).build())
                .apiStages(ApiStage.builder().apiId(pnApikeyManagerConfig.getApiId())
                        .stage(pnApikeyManagerConfig.getStage()).build())
                .build();
    }

    private CreateUsagePlanKeyRequest constructUsagePlanKeyRequest(CreateUsagePlanResponse createUsagePlanResponse, String id) {
        return CreateUsagePlanKeyRequest.builder()
                .keyId(id)
                .keyType(pnApikeyManagerConfig.getKeyType())
                .usagePlanId(createUsagePlanResponse.id())
                .build();
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

}
