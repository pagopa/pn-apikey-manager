package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AggregationService {

    private final AggregateRepository aggregateRepository;
    private final ApiGatewayAsyncClient apiGatewayAsyncClient;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    public AggregationService(AggregateRepository aggregateRepository,
                              ApiGatewayAsyncClient apiGatewayAsyncClient,
                              PnApikeyManagerConfig pnApikeyManagerConfig
    ) {
        this.aggregateRepository = aggregateRepository;
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
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

    public Mono<ApiKeyAggregation> getApiKeyAggregation(String aggregationId) {
        return aggregateRepository.getApiKeyAggregation(aggregationId);
    }

    public Mono<String> addAwsApiKeyToAggregate(CreateApiKeyResponse createApiKeyResponse, ApiKeyAggregation aggregate) {
        aggregate.setLastUpdate(LocalDateTime.now());
        aggregate.setApiKeyId(createApiKeyResponse.id());
        aggregate.setApiKey(createApiKeyResponse.value());
        return aggregateRepository.saveAggregation(aggregate).map(ApiKeyAggregation::getAggregateId);
    }

    public Mono<ApiKeyAggregation> createNewAggregate(String paId) {
        ApiKeyAggregation newApiKeyAggregation = new ApiKeyAggregation();
        newApiKeyAggregation.setAggregateId(UUID.randomUUID().toString());
        newApiKeyAggregation.setAggregateName("AGG_"+paId);
        newApiKeyAggregation.setLastUpdate(LocalDateTime.now());
        newApiKeyAggregation.setCreatedAt(LocalDateTime.now());
        return aggregateRepository.saveAggregation(newApiKeyAggregation);
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
                .throttle(ThrottleSettings.builder().burstLimit(pnApikeyManagerConfig.getUsageplanBurstLimit()).rateLimit(pnApikeyManagerConfig.getUsageplanThrottle()).build())
                .apiStages(ApiStage.builder().apiId(pnApikeyManagerConfig.getUsageplanApiId())
                        .stage(pnApikeyManagerConfig.getUsageplanStage()).build())
                .build();
    }

    private CreateUsagePlanKeyRequest constructUsagePlanKeyRequest(CreateUsagePlanResponse createUsagePlanResponse, String id) {
        return CreateUsagePlanKeyRequest.builder()
                .keyId(id)
                .keyType(pnApikeyManagerConfig.getUsageplanKeyType())
                .usagePlanId(createUsagePlanResponse.id())
                .build();
    }

}
