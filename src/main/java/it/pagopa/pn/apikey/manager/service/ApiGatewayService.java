package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

@Slf4j
@Service
public class ApiGatewayService {

    private final ApiGatewayAsyncClient apiGatewayAsyncClient;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    public ApiGatewayService(ApiGatewayAsyncClient apiGatewayAsyncClient, PnApikeyManagerConfig pnApikeyManagerConfig) {
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
    }

    public Mono<CreateApiKeyResponse> createNewAwsApiKey(String aggregateName) {
        CreateApiKeyRequest createApiKeyRequest = constructApiKeyRequest(aggregateName);
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .doOnNext(createApiKeyResponse -> log.info("Created AWS ApiKey with name: {}", createApiKeyResponse.name()))
                .flatMap(createApiKeyResponse -> createUsagePlanKey(pnApikeyManagerConfig.getDefaultPlan(), createApiKeyResponse.id())
                        .map(createUsagePlanKeyResponse -> createApiKeyResponse));
    }

    public Mono<CreateUsagePlanKeyResponse> createUsagePlanKey(String usagePlanId, String id) {
        CreateUsagePlanKeyRequest createUsagePlanKeyRequest = constructUsagePlanKeyRequest(usagePlanId, id);
        log.debug("CreateUsagePlanKeyRequest with KeyType: {}, usagePlanId: {}",
                createUsagePlanKeyRequest.keyType(), createUsagePlanKeyRequest.usagePlanId());

        return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                .doOnNext(createUsagePlanKeyResponse -> log.info("Created AWS usagePlanKey with id: {}, keyId: {}",
                        createUsagePlanKeyRequest.usagePlanId(), createUsagePlanKeyRequest.keyId()));
    }

    public Mono<CreateUsagePlanKeyResponse> moveApiKeyToNewUsagePlan(ApiKeyAggregateModel apiKeyAggregateModel, AggregateRequestDto aggregateRequestDto) {
        DeleteUsagePlanKeyRequest deleteUsagePlanKeyRequest = DeleteUsagePlanKeyRequest.builder()
                .usagePlanId(apiKeyAggregateModel.getUsagePlanId())
                .keyId(apiKeyAggregateModel.getApiKeyId())
                .build();
        log.debug("DeleteUsagePlanKeyRequest for usagePlanId: {}, ApiKeyId: {}", apiKeyAggregateModel.getUsagePlanId(), apiKeyAggregateModel.getApiKeyId());
        return Mono.fromFuture(apiGatewayAsyncClient.deleteUsagePlanKey(deleteUsagePlanKeyRequest))
                .doOnNext(deleteUsagePlanKeyResponse -> log.info("Deleted usagePlanKey fro usagePlanId: {}",apiKeyAggregateModel.getUsagePlanId()))
                .flatMap(deleteUsagePlanKeyResponse -> createUsagePlanKey(aggregateRequestDto.getUsagePlanId(), apiKeyAggregateModel.getApiKeyId()));
    }

    private CreateApiKeyRequest constructApiKeyRequest(String aggregateName) {
        return CreateApiKeyRequest.builder()
                .name("pn_" + aggregateName + "_apikey")
                .enabled(true)
                .build();
    }

    private CreateUsagePlanKeyRequest constructUsagePlanKeyRequest(String usagePlanId, String id) {
        return CreateUsagePlanKeyRequest.builder()
                .keyId(id)
                .keyType(pnApikeyManagerConfig.getKeyType())
                .usagePlanId(usagePlanId)
                .build();
    }
}
