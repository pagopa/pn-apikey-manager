package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
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

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AggregationService.class})
@ExtendWith(SpringExtension.class)
class AggregationServiceTest {

    @MockBean
    private AggregateRepository aggregateRepository;
    @MockBean
    private PaAggregationRepository paAggregationRepository;

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

        StepVerifier.create(aggregationService.createNewAwsApiKey("Pa"))
                .expectNext(CreateApiKeyResponse.builder().name("test").id("id").build()).verifyComplete();
    }


    @Test
    void addAwsApiKeyToAggregateTest(){
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setAggregateId("id");
        when(aggregateRepository.saveAggregation(any())).thenReturn(Mono.just(apikeyAggregateModel));
        StepVerifier.create(aggregationService.addAwsApiKeyToAggregate(CreateApiKeyResponse.builder().id("id").build(), apikeyAggregateModel))
                .expectNextMatches(apiKeyAggregation1 -> apiKeyAggregation1.equalsIgnoreCase("id")).verifyComplete();

    }

    @Test
    void createNewAggregateTest(){
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setName("");
        when(aggregateRepository.saveAggregation(any())).thenReturn(Mono.just(apikeyAggregateModel));
        StepVerifier.create(aggregationService.createNewAggregate("paID"))
                .expectNextMatches(apiKeyAggregation1 -> apiKeyAggregation1.getName().equalsIgnoreCase("")).verifyComplete();

    }

    @Test
    void testCreateUsagePlan() {
        CreateUsagePlanResponse createUsagePlanResponse = CreateUsagePlanResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> createUsagePlanResponse);

        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(() -> createUsagePlanKeyResponse);

        when(apiGatewayAsyncClient.createUsagePlan((CreateUsagePlanRequest) any())).thenReturn(completableFuture);
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any())).thenReturn(completableFuture2);
        StepVerifier.create(aggregationService.createUsagePlan("42","id"))
                .expectNext(createUsagePlanKeyResponse).verifyComplete();
    }

    /**
     * Method under test: {@link AggregationService#getApiKeyAggregation(String)}
     */
    @Test
    void testSearchAwsApiKey() {
        when(aggregateRepository.getApiKeyAggregation(any())).thenReturn(Mono.empty());
        StepVerifier.create(aggregationService.getApiKeyAggregation("42")).verifyComplete();
    }
}

