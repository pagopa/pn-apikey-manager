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

    public Mono<String> createNewAwsApiKey(String pa) {
        CreateApiKeyRequest createApiKeyRequest = constructApiKeyRequest(pa);
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .doOnNext(createApiKeyResponse -> log.info("Created ApiKey with name: {}",createApiKeyRequest.name()))
                .flatMap(createApiKeyResponse -> createUsagePlan(createApiKeyResponse.id())
                        .map(createUsagePlanKeyResponse -> createApiKeyResponse.id()));
    }

    public Mono<CreateUsagePlanKeyResponse> createUsagePlan(String id) {
        CreateUsagePlanRequest createUsagePlanRequest = constructUsagePlanRequest();
        log.debug("CreateUsagePlanRequest with name: {}, quota: {}, throttle: {}, stage: {}",
                createUsagePlanRequest.name(), createUsagePlanRequest.quota(), createUsagePlanRequest.throttle(), createUsagePlanRequest.apiStages());

       return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlan(createUsagePlanRequest))
               .doOnNext(createUsagePlanResponse -> log.info("Created AWS usagePlan with name: {}", createUsagePlanRequest.name()))
               .flatMap(createUsagePlanResponse -> {
            CreateUsagePlanKeyRequest createUsagePlanKeyRequest = constructUsagePlanKeyRequest(createUsagePlanResponse, id);
            return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                    .doOnNext(createUsagePlanKeyResponse -> log.info("Created AWS usagePlanKey with id: {}, keyId: {}",createUsagePlanKeyRequest.usagePlanId(), createUsagePlanKeyRequest.keyId()));
        });
    }

    private CreateApiKeyRequest constructApiKeyRequest(String pa) {
        return CreateApiKeyRequest.builder()
                .name(pa + "-" +UUID.randomUUID())
                .enabled(true)
                .build();
    }

    private CreateUsagePlanKeyRequest constructUsagePlanKeyRequest(CreateUsagePlanResponse createUsagePlanResponse, String id) {
        return CreateUsagePlanKeyRequest.builder()
                .keyId(id)
                .keyType(pnApikeyManagerConfig.getUsageplanKeyType())
                .usagePlanId(createUsagePlanResponse.id())
                .build();
    }

    private CreateUsagePlanRequest constructUsagePlanRequest() {
        return CreateUsagePlanRequest.builder()
                .name("pn-apikey-medium")
                .quota(QuotaSettings.builder().limit(pnApikeyManagerConfig.getUsageplanQuota()).period(QuotaPeriodType.DAY).build())
                .throttle(ThrottleSettings.builder().rateLimit(pnApikeyManagerConfig.getUsageplanThrottle()).build())
                .apiStages(ApiStage.builder().apiId(pnApikeyManagerConfig.getUsageplanApiId())
                        .stage(pnApikeyManagerConfig.getUsageplanStage()).build())
                .build();
    }

    public Mono<ApiKeyAggregation> createNewAggregation(String awsApiKeyId) {
        GetApiKeyRequest getApiKeyRequest = GetApiKeyRequest.builder().apiKey(awsApiKeyId).build();
        return Mono.fromFuture(apiGatewayAsyncClient.getApiKey(getApiKeyRequest))
                .flatMap(getApiKeyResponse -> {
                    ApiKeyAggregation newApiKeyAggregation = new ApiKeyAggregation();
                    newApiKeyAggregation.setAggregateId("aggregationId");
                    newApiKeyAggregation.setAggregationName("");
                    newApiKeyAggregation.setLastUpdate(LocalDateTime.now().toString());
                    newApiKeyAggregation.setCreatedAt(LocalDateTime.now().toString());
                    newApiKeyAggregation.setApiKeyId(awsApiKeyId);
                    newApiKeyAggregation.setApiKey(getApiKeyResponse.value());
                    return aggregationRepository.saveAggregation(newApiKeyAggregation);
                });
    }

    public Mono<ApiKeyAggregation> searchAwsApiKey(String aggregationId){
        return aggregationRepository.searchRealApiKey(aggregationId);
    }

    //aggregateId
    //aggregationName
    //apiKey name
    //usage plan name

}
