package it.pagopa.pn.apikey.manager.service;

import io.swagger.annotations.Api;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AggregationService {

    private final AggregationRepository aggregationRepository;
    private final ApiGatewayAsyncClient apiGatewayAsyncClient;

    public AggregationService(AggregationRepository aggregationRepository,
                              ApiGatewayAsyncClient apiGatewayAsyncClient) {
        this.aggregationRepository = aggregationRepository;
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
    }

    public Mono<String> createNewAwsApiKey(String pa) {
        CreateApiKeyRequest createApiKeyRequest = CreateApiKeyRequest.builder()
                .name(pa + UUID.randomUUID())
                .enabled(true)
                .build();
        log.debug("CreateApiKeyRequest with name: {}",createApiKeyRequest.name());
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .flatMap(createApiKeyResponse -> createUsagePlan(createApiKeyResponse.id())
                        .map(createUsagePlanKeyResponse -> createApiKeyResponse.id()));
    }

    public Mono<CreateUsagePlanKeyResponse> createUsagePlan(String id) {
        CreateUsagePlanRequest createUsagePlanRequest = CreateUsagePlanRequest.builder()
                .name("pn-apikey-medium")
                .quota(QuotaSettings.builder().limit(10000).period(QuotaPeriodType.DAY).build())
                .throttle(ThrottleSettings.builder().rateLimit(10000D).build())
                .apiStages(ApiStage.builder().apiId("vub3na4af4").stage("dev").build())
                .build();

        log.debug("CreateUsagePlanRequest with name: {}, quota: {}, throttle: {}, stage: {}",
                createUsagePlanRequest.name(), createUsagePlanRequest.quota(), createUsagePlanRequest.throttle(), createUsagePlanRequest.apiStages());

       return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlan(createUsagePlanRequest)).flatMap(createUsagePlanResponse -> {
            CreateUsagePlanKeyRequest createUsagePlanKeyRequest = CreateUsagePlanKeyRequest.builder()
                    .keyId(id)
                    .usagePlanId(createUsagePlanResponse.id())
                    .build();
            return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                    .doOnNext(createUsagePlanKeyResponse -> log.info("Created usagePlanKey with id: {}, keyId: {}",createUsagePlanKeyRequest.usagePlanId(), createUsagePlanKeyRequest.keyId()));
        });
    }

    public Mono<ApiKeyAggregation> createNewAggregation(String awsApiKey) {
        ApiKeyAggregation newApiKeyAggregation = new ApiKeyAggregation();
        newApiKeyAggregation.setAggregateId("aggregationId");
        newApiKeyAggregation.setAggregationName("");
        newApiKeyAggregation.setLastUpdate(LocalDateTime.now().toString());
        newApiKeyAggregation.setCreatedAt(LocalDateTime.now().toString());
        newApiKeyAggregation.setApiKey(awsApiKey);
        return aggregationRepository.saveAggregation(newApiKeyAggregation);
    }

    public Mono<ApiKeyAggregation> deleteAggregation(String aggregationId){
        //TODO: REMOVE AGGREGATION, DELETE API KEY, DELETE PA ASSOCIATION, DELETE APY KEY REAL FROM VIRTUAL
        return Mono.empty();
    }

    public Mono<ApiKeyAggregation> searchAwsApiKey(String aggregationId){
        return aggregationRepository.searchRealApiKey(aggregationId);
    }

}
