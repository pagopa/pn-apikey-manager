package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;
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

    private final AggregationRepository aggregationRepository;
    private final ApiGatewayAsyncClient apiGatewayAsyncClient;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    public AggregationService(AggregationRepository aggregationRepository,
                              ApiGatewayAsyncClient apiGatewayAsyncClient,
                              PnApikeyManagerConfig pnApikeyManagerConfig
    ) {
        this.aggregationRepository = aggregationRepository;
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
    }

    public Mono<CreateApiKeyResponse> createNewAwsApiKey(String pa) {
        CreateApiKeyRequest createApiKeyRequest = constructApiKeyRequest(pa);
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .doOnNext(createApiKeyResponse -> log.info("Created AWS ApiKey with name: {}", createApiKeyResponse.name()))
                .flatMap(createApiKeyResponse -> createUsagePlan(createApiKeyResponse.id())
                        .map(createUsagePlanKeyResponse -> createApiKeyResponse));
    }

    public Mono<CreateUsagePlanKeyResponse> createUsagePlan(String id) {
        CreateUsagePlanRequest createUsagePlanRequest = constructUsagePlanRequest();
        log.debug("CreateUsagePlanRequest with name: {}, quota: {}, throttle: {}, stage: {}",
                createUsagePlanRequest.name(), createUsagePlanRequest.quota(), createUsagePlanRequest.throttle(), createUsagePlanRequest.apiStages());

        return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlan(createUsagePlanRequest))
                .doOnNext(createUsagePlanResponse -> log.info("Created AWS usagePlan with name: {}", createUsagePlanRequest.name()))
                .flatMap(createUsagePlanResponse -> createUsagePlanKey(createUsagePlanResponse,id));
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
        return aggregationRepository.getApiKeyAggregation(aggregationId);
    }

    public Mono<String> addAwsApiKeyToAggregate(CreateApiKeyResponse createApiKeyResponse, ApiKeyAggregation aggregate) {
        aggregate.setLastUpdate(LocalDateTime.now().toString());
        aggregate.setApiKeyId(createApiKeyResponse.id());
        aggregate.setApiKey(createApiKeyResponse.value());
        return aggregationRepository.saveAggregation(aggregate).map(ApiKeyAggregation::getAggregateId);
    }

    public Mono<ApiKeyAggregation> createNewAggregate(CreateApiKeyResponse createApiKeyResponse) {
        ApiKeyAggregation newApiKeyAggregation = new ApiKeyAggregation();
        newApiKeyAggregation.setAggregateId(UUID.randomUUID().toString());
        newApiKeyAggregation.setAggregationName("");
        newApiKeyAggregation.setLastUpdate(LocalDateTime.now().toString());
        newApiKeyAggregation.setCreatedAt(LocalDateTime.now().toString());
        newApiKeyAggregation.setApiKeyId(createApiKeyResponse.id());
        newApiKeyAggregation.setApiKey(createApiKeyResponse.value());
        return aggregationRepository.saveAggregation(newApiKeyAggregation);
    }

    private CreateApiKeyRequest constructApiKeyRequest(String pa) {
        return CreateApiKeyRequest.builder()
                .name(pa + "-" + UUID.randomUUID())
                .enabled(true)
                .build();
    }

    private CreateUsagePlanRequest constructUsagePlanRequest() {
        return CreateUsagePlanRequest.builder()
                .name("pn-apikey-medium" + UUID.randomUUID())
                .quota(QuotaSettings.builder().limit(pnApikeyManagerConfig.getUsageplanQuota()).period(QuotaPeriodType.DAY).build())
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


    //aggregateId
    //aggregationName
    //apiKey name
    //usage plan name

}
