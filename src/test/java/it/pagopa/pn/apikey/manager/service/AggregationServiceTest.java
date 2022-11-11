package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.AggregationConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.repository.AggregatePageable;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AggregationService.class})
@ExtendWith(SpringExtension.class)
class AggregationServiceTest {

    @MockBean
    private AggregateRepository aggregateRepository;

    @MockBean
    private DynamoDbEnhancedAsyncClient asyncClient;

    @MockBean
    private PaAggregationRepository paAggregationRepository;

    @MockBean
    private UsagePlanService usagePlanService;

    @MockBean
    private ApiGatewayAsyncClient apiGatewayAsyncClient;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private AggregationConverter aggregationConverter;

    @Autowired
    private AggregationService aggregationService;

    @Test
    void getAggregate(){
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setAggregateId("id");
        when(aggregateRepository.getApiKeyAggregation(any())).thenReturn(Mono.just(apiKeyAggregateModel));

        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        when(usagePlanService.getUsagePlan(any())).thenReturn(Mono.just(usagePlanDetailDto));

        AggregateResponseDto aggregateResponseDto = new AggregateResponseDto();
        aggregateResponseDto.setId("id");
        when(aggregationConverter.convertResponseDto((ApiKeyAggregateModel)any(),any())).thenReturn(aggregateResponseDto);
        StepVerifier.create(aggregationService.getAggregate("id"))
                .expectNext(aggregateResponseDto).verifyComplete();
    }

    @Test
    void testGetAggregation() {
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setUsagePlanId("usagePlanId");
        Map<String, AttributeValue> lastEvaluatedKey = Map.of("id", AttributeValue.builder().s("id").build());
        Page<ApiKeyAggregateModel> page = Page.create(List.of(apiKeyAggregateModel), lastEvaluatedKey);
        when(aggregateRepository.findAll(any()))
                .thenReturn(Mono.just(page));

        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("usagePlanId");
        when(usagePlanService.getUsagePlan("usagePlanId"))
                .thenReturn(Mono.just(usagePlanDetailDto));

        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setUsagePlan("usagePlanName");
        AggregatesListResponseDto result = new AggregatesListResponseDto();
        result.setLastEvaluatedId("id");
        result.addItemsItem(aggregateRowDto);

        when(aggregationConverter.convertResponseDto(page, List.of(usagePlanDetailDto)))
                .thenReturn(result);

        StepVerifier.create(aggregationService.getAggregation(null, AggregatePageable.builder().build()))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    void testGetAggregationByName() {
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        Page<ApiKeyAggregateModel> page = Page.create(List.of(apiKeyAggregateModel));
        when(aggregateRepository.findByName(eq("name"), any()))
                .thenReturn(Mono.just(page));

        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        AggregatesListResponseDto result = new AggregatesListResponseDto();
        result.addItemsItem(aggregateRowDto);

        when(aggregationConverter.convertResponseDto(page, Collections.emptyList()))
                .thenReturn(result);

        StepVerifier.create(aggregationService.getAggregation("name", AggregatePageable.builder().build()))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    void testDeleteAggregation() {
        when(paAggregationRepository.findByAggregateId(any(), any(), any()))
                .thenReturn(Mono.just(Page.create(Collections.emptyList())));
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        when(aggregateRepository.delete("aggregateId"))
                .thenReturn(Mono.just(apiKeyAggregateModel));
        StepVerifier.create(aggregationService.deleteAggregation("aggregateId"))
                .verifyComplete();
    }

    @Test
    void testDeleteAggregationNotFound() {
        when(paAggregationRepository.findByAggregateId(any(), any(), any()))
                .thenReturn(Mono.just(Page.create(Collections.emptyList())));
        when(aggregateRepository.delete("aggregateId"))
                .thenReturn(Mono.empty());
        StepVerifier.create(aggregationService.deleteAggregation("aggregateId"))
                .verifyError(ApiKeyManagerException.class);
    }

    @Test
    void testDeleteAggregationFailure() {
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        Page<PaAggregationModel> page = Page.create(List.of(paAggregationModel));
        when(paAggregationRepository.findByAggregateId("aggregateId", null, null))
                .thenReturn(Mono.just(page));

        StepVerifier.create(aggregationService.deleteAggregation("aggregateId"))
                .verifyError(ApiKeyManagerException.class);
    }

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
        when(pnApikeyManagerConfig.getUsageplanThrottle()).thenReturn("5000");
        when(pnApikeyManagerConfig.getUsageplanBurstLimit()).thenReturn("1500");
        StepVerifier.create(aggregationService.createNewAwsApiKey("Pa"))
                .expectNext(CreateApiKeyResponse.builder().name("test").id("id").build()).verifyComplete();
    }

    @Test
    void addAwsApiKeyToAggregateTest() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setAggregateId("id");
        when(aggregateRepository.saveAggregation(any())).thenReturn(Mono.just(apikeyAggregateModel));
        StepVerifier.create(aggregationService.addAwsApiKeyToAggregate(CreateApiKeyResponse.builder().id("id").build(), apikeyAggregateModel))
                .expectNextMatches(apiKeyAggregation1 -> apiKeyAggregation1.equalsIgnoreCase("id")).verifyComplete();

    }

    @Test
    void createNewAggregateTest() {
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
        when(pnApikeyManagerConfig.getUsageplanThrottle()).thenReturn("5000");
        when(pnApikeyManagerConfig.getUsageplanBurstLimit()).thenReturn("1500");
        when(apiGatewayAsyncClient.createUsagePlan((CreateUsagePlanRequest) any())).thenReturn(completableFuture);
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any())).thenReturn(completableFuture2);
        StepVerifier.create(aggregationService.createUsagePlan("42", "id"))
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

    @Test
    void getAggregationTest() {
        ApiKeyAggregateModel dto = new ApiKeyAggregateModel();
        dto.setName("name");
        dto.setAggregateId("id");
        Page<ApiKeyAggregateModel> page = Page.create(List.of(dto));
        AggregatePageable aggregatePageable = AggregatePageable.builder()
                .lastEvaluatedId("id")
                .limit(10)
                .build();
        when(aggregateRepository.findByName("name", aggregatePageable)).thenReturn(Mono.just(page));
        AggregatesListResponseDto aggregateListResponse = new AggregatesListResponseDto();
        List<AggregateRowDto> list = new ArrayList<>();
        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setId("id");
        list.add(aggregateRowDto);
        aggregateListResponse.setItems(list);
        when(aggregationConverter.convertResponseDto(any(),anyList())).thenReturn(aggregateListResponse);
        StepVerifier.create(aggregationService.getAggregation("name", aggregatePageable))
                .expectNext(aggregateListResponse).verifyComplete();
    }

    @Test
    void getAggregationTest2() {
        ApiKeyAggregateModel dto = new ApiKeyAggregateModel();
        dto.setName("name");
        dto.setAggregateId("id");
        Page<ApiKeyAggregateModel> page = Page.create(List.of(dto));
        AggregatePageable aggregatePageable = AggregatePageable.builder()
                .lastEvaluatedId("id")
                .limit(10)
                .build();
        when(aggregateRepository.findAll(aggregatePageable)).thenReturn(Mono.just(page));
        AggregatesListResponseDto aggregateListResponse = new AggregatesListResponseDto();
        List<AggregateRowDto> list = new ArrayList<>();
        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setId("id");
        list.add(aggregateRowDto);
        aggregateListResponse.setItems(list);
        when(aggregationConverter.convertResponseDto(any(),anyList())).thenReturn(aggregateListResponse);
        StepVerifier.create(aggregationService.getAggregation(null, aggregatePageable))
                .expectNext(aggregateListResponse).verifyComplete();
    }

    @Test
    void deleteAggregationTest() {
        PaAggregationModel paAggregation = new PaAggregationModel();
        paAggregation.setPaName("name");
        paAggregation.setPaId("paId");
        paAggregation.setAggregateId("id");
        Page<PaAggregationModel> page = Page.create(List.of(paAggregation));
        when(paAggregationRepository.findByAggregateId("id", null, null))
                .thenReturn(Mono.just(page));
        StepVerifier.create(aggregationService.deleteAggregation("id"))
                .expectError(ApiKeyManagerException.class).verify();
    }
}
