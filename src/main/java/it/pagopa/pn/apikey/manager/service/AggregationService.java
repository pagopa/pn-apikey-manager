package it.pagopa.pn.apikey.manager.service;

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

    public AggregationService(AggregationRepository aggregationRepository,
                              ApiGatewayAsyncClient apiGatewayAsyncClient) {
        this.aggregationRepository = aggregationRepository;
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
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
               .flatMap(createUsagePlanResponse -> {
            CreateUsagePlanKeyRequest createUsagePlanKeyRequest = constructUsagePlanKeyRequest(createUsagePlanResponse, id);
            return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                    .doOnNext(createUsagePlanKeyResponse -> log.info("Created usagePlanKey with id: {}, keyId: {}",createUsagePlanKeyRequest.usagePlanId(), createUsagePlanKeyRequest.keyId()));
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
                .keyType("API_KEY")
                .usagePlanId(createUsagePlanResponse.id())
                .build();
    }

    private CreateUsagePlanRequest constructUsagePlanRequest() {
        return CreateUsagePlanRequest.builder()
                .name("pn-apikey-medium")
                .quota(QuotaSettings.builder().limit(10000).period(QuotaPeriodType.DAY).build())
                .throttle(ThrottleSettings.builder().rateLimit(10000D).build())
                .apiStages(ApiStage.builder().apiId("vub3na4af4").stage("dev").build())
                .build();
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

    public Mono<ApiKeyAggregation> searchAwsApiKey(String aggregationId){
        return aggregationRepository.searchRealApiKey(aggregationId).doOnNext(apiKeyAggregation -> log.info("resp: {}",apiKeyAggregation));
    }

}
