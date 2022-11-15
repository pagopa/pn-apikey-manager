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

    /**
     * Creazione di una AWS API Key fisica.
     * @param aggregateName nome dell'aggregato, necessario per la creazione del nome dell'API Key
     * @return La risposta del SDK di AWS della creazione dell'API Key
     */
    public Mono<CreateApiKeyResponse> createNewAwsApiKey(String aggregateName) {
        CreateApiKeyRequest createApiKeyRequest = constructApiKeyRequest(aggregateName);
        log.debug("create AWS Api Key request: {}", createApiKeyRequest);
        return Mono.fromFuture(apiGatewayAsyncClient.createApiKey(createApiKeyRequest))
                .doOnNext(response -> log.info("Create AWS Api Key response: {}", response));
    }

    /**
     * Eliminazione di una AWS API Key fisica.
     * @param apiKeyId id della API Key da eliminare
     * @return La risposta del SDK di AWS della cancellazione dell'API Key
     */
    public Mono<DeleteApiKeyResponse> deleteAwsApiKey(String apiKeyId) {
        DeleteApiKeyRequest deleteApiKeyRequest = DeleteApiKeyRequest.builder().apiKey(apiKeyId).build();
        log.debug("delete AWS Api Key {} request: {}", apiKeyId, deleteApiKeyRequest);
        return Mono.fromFuture(apiGatewayAsyncClient.deleteApiKey(deleteApiKeyRequest))
                .doOnNext(response -> log.info("Delete AWS Api Key {} response: {}", apiKeyId, response));
    }

    /**
     * Associazione di una AWS API Key fisica a uno Usage Plan.
     * @param usagePlanId id dello usage plan
     * @param apiKeyId id della chiave fisica
     * @return La risposta del SDK di AWS dell'associazione dello usage plan
     */
    public Mono<CreateUsagePlanKeyResponse> addUsagePlanToApiKey(String usagePlanId, String apiKeyId) {
        CreateUsagePlanKeyRequest createUsagePlanKeyRequest = constructUsagePlanKeyRequest(usagePlanId, apiKeyId);
        log.debug("create AWS UsagePlan-ApiKey request: {}", createUsagePlanKeyRequest);
        return Mono.fromFuture(apiGatewayAsyncClient.createUsagePlanKey(createUsagePlanKeyRequest))
                .doOnNext(response -> log.info("Create AWS UsagePlan-ApiKey response: {}", response));
    }

    public Mono<CreateUsagePlanKeyResponse> moveApiKeyToNewUsagePlan(ApiKeyAggregateModel apiKeyAggregateModel, AggregateRequestDto aggregateRequestDto) {
        DeleteUsagePlanKeyRequest deleteUsagePlanKeyRequest = DeleteUsagePlanKeyRequest.builder()
                .usagePlanId(apiKeyAggregateModel.getUsagePlanId())
                .keyId(apiKeyAggregateModel.getApiKeyId())
                .build();
        log.debug("DeleteUsagePlanKeyRequest for usagePlanId: {}, ApiKeyId: {}", apiKeyAggregateModel.getUsagePlanId(), apiKeyAggregateModel.getApiKeyId());
        return Mono.fromFuture(apiGatewayAsyncClient.deleteUsagePlanKey(deleteUsagePlanKeyRequest))
                .doOnNext(response -> log.info("Delete AWS UsagePlan-ApiKey response: {}", response))
                .flatMap(response -> addUsagePlanToApiKey(aggregateRequestDto.getUsagePlanId(), apiKeyAggregateModel.getApiKeyId()));
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
