package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

@ContextConfiguration(classes = {AggregationService.class})
@ExtendWith(SpringExtension.class)
class AggregationServiceTest {
    @MockBean
    private AggregationRepository aggregationRepository;

    @Autowired
    private AggregationService aggregationService;

    @MockBean
    private ApiGatewayAsyncClient apiGatewayAsyncClient;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    /**
     * Method under test: {@link AggregationService#createNewAwsApiKey(String)}
     */
    @Test
    void testCreateNewAwsApiKey() {
        CreateApiKeyResponse createApiKeyResponse = CreateApiKeyResponse.builder().name("test").id("id").build();
        CompletableFuture<CreateApiKeyResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> createApiKeyResponse);
        when(apiGatewayAsyncClient.createApiKey((CreateApiKeyRequest) any())).thenReturn(completableFuture);

        CreateUsagePlanResponse createUsagePlanResponse = CreateUsagePlanResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanResponse> completableFuture1 = new CompletableFuture<>();
        completableFuture1.completeAsync(() -> createUsagePlanResponse);

        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(() -> createUsagePlanKeyResponse);

        when(apiGatewayAsyncClient.createUsagePlan((CreateUsagePlanRequest) any())).thenReturn(completableFuture1);
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any())).thenReturn(completableFuture2);

        StepVerifier.create(aggregationService.createNewAwsApiKey("Pa")).expectNext("id").verifyComplete();
    }

    /**
     * Method under test: {@link AggregationService#createUsagePlan(String)}
     */
    @Test
    void testCreateUsagePlan(){
        CreateUsagePlanResponse createUsagePlanResponse = CreateUsagePlanResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> createUsagePlanResponse);

        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(() -> createUsagePlanKeyResponse);

        when(apiGatewayAsyncClient.createUsagePlan((CreateUsagePlanRequest) any())).thenReturn(completableFuture);
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any())).thenReturn(completableFuture2);
        StepVerifier.create(aggregationService.createUsagePlan("42"))
                .expectNext(createUsagePlanKeyResponse).verifyComplete();
    }

    @Test
    void testCreateNewAggregation() {
        GetApiKeyResponse getApiKeyResponse = GetApiKeyResponse.builder().id("id").build();
        CompletableFuture<GetApiKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(() -> getApiKeyResponse);
        when(apiGatewayAsyncClient.getApiKey((GetApiKeyRequest) any())).thenReturn(completableFuture2);

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        when(aggregationRepository.saveAggregation(any())).thenReturn(Mono.just(apiKeyAggregation));

        StepVerifier.create(aggregationService.createNewAggregation("id")).expectNext(apiKeyAggregation).verifyComplete();
    }

    /**
     * Method under test: {@link AggregationService#searchAwsApiKey(String)}
     */
    @Test
    void testSearchAwsApiKey() {
        when(aggregationRepository.searchRealApiKey(any())).thenReturn(Mono.empty());
        StepVerifier.create(aggregationService.searchAwsApiKey("42")).verifyComplete();
    }
}

