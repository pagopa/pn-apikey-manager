package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.AggregationConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private AggregationConverter aggregationConverter;

    @Autowired
    private AggregationService aggregationService;

    @MockBean
    private ApiGatewayService apiGatewayService;

    @Test
    void testGetPaOfAggregate(){
        PaAggregateResponseDto paAggregateResponseDto = new PaAggregateResponseDto();
        paAggregateResponseDto.setTotal(0);
        paAggregateResponseDto.setItems(new ArrayList<>());
        ApiKeyAggregateModel dto = new ApiKeyAggregateModel();
        dto.setAggregateId("id");
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        Page<PaAggregationModel> page = Page.create(List.of(paAggregationModel));
        when(paAggregationRepository.findByAggregateId(any(),any()))
                .thenReturn(Mono.just(page));
        when(aggregateRepository.getApiKeyAggregation("id"))
                .thenReturn(Mono.just(dto));
        when(aggregationConverter.convertToResponseDto(page)).thenReturn(paAggregateResponseDto);
        StepVerifier.create(aggregationService.getPaOfAggregate("id"))
                .expectNext(paAggregateResponseDto).verifyComplete();
    }

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
        when(aggregationConverter.convertToResponseDto((ApiKeyAggregateModel)any(),any())).thenReturn(aggregateResponseDto);
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
        when(aggregateRepository.count()).thenReturn(Mono.just(1));

        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("usagePlanId");
        when(usagePlanService.getUsagePlan("usagePlanId"))
                .thenReturn(Mono.just(usagePlanDetailDto));

        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setUsagePlan("usagePlanName");
        AggregatesListResponseDto result = new AggregatesListResponseDto();
        result.setLastEvaluatedId("id");
        result.addItemsItem(aggregateRowDto);

        when(aggregationConverter.convertToResponseDto(page, List.of(usagePlanDetailDto)))
                .thenReturn(result);

        StepVerifier.create(aggregationService.getAggregation(null, null, null, null))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    void testGetAggregationByName() {
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        Page<ApiKeyAggregateModel> page = Page.create(List.of(apiKeyAggregateModel));
        when(aggregateRepository.findByName(eq("name"), any()))
                .thenReturn(Mono.just(page));
        when(aggregateRepository.countByName("name")).thenReturn(Mono.just(1));

        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        AggregatesListResponseDto result = new AggregatesListResponseDto();
        result.addItemsItem(aggregateRowDto);

        when(aggregationConverter.convertToResponseDto(page, Collections.emptyList()))
                .thenReturn(result);

        StepVerifier.create(aggregationService.getAggregation("name", null, null, null))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    void testDeleteAggregation() {
        when(paAggregationRepository.findByAggregateId(any(), any()))
                .thenReturn(Mono.just(Page.create(Collections.emptyList())));
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        when(aggregateRepository.getApiKeyAggregation("aggregateId"))
                .thenReturn(Mono.just(apiKeyAggregateModel));
        when(aggregateRepository.delete("aggregateId"))
                .thenReturn(Mono.just(apiKeyAggregateModel));
        StepVerifier.create(aggregationService.deleteAggregate("aggregateId"))
                .verifyComplete();
    }

    @Test
    void testDeleteAggregationNotFound() {
        when(paAggregationRepository.findByAggregateId(any(), any()))
                .thenReturn(Mono.just(Page.create(Collections.emptyList())));
        when(aggregateRepository.getApiKeyAggregation("aggregateId"))
                .thenReturn(Mono.empty());
        when(aggregateRepository.delete("aggregateId"))
                .thenReturn(Mono.empty());
        StepVerifier.create(aggregationService.deleteAggregate("aggregateId"))
                .verifyError(ApiKeyManagerException.class);
    }

    @Test
    void testDeleteAggregationFailure() {
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        Page<PaAggregationModel> page = Page.create(List.of(paAggregationModel));
        when(paAggregationRepository.findByAggregateId(eq("aggregateId"), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(aggregationService.deleteAggregate("aggregateId"))
                .verifyError(ApiKeyManagerException.class);
    }

    @Test
    void addAwsApiKeyToAggregateTest() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setAggregateId("id");
        when(aggregateRepository.saveAggregation(any())).thenReturn(Mono.just(apikeyAggregateModel));
        StepVerifier.create(aggregationService.addAwsApiKeyToAggregate(CreateApiKeyResponse.builder().id("id").build(), apikeyAggregateModel)
                        .map(ApiKeyAggregateModel::getAggregateId))
                .expectNextMatches(apiKeyAggregation1 -> apiKeyAggregation1.equalsIgnoreCase("id")).verifyComplete();
    }

    @Test
    void createNewAggregateTest() {
        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setAggregateId("aggregateId");
        apikeyAggregateModel.setName("");
        when(aggregateRepository.saveAggregation(any())).thenReturn(Mono.just(apikeyAggregateModel));
        CreateApiKeyResponse createApiKeyResponse = CreateApiKeyResponse.builder().build();
        when(apiGatewayService.createNewAwsApiKey(any()))
                .thenReturn(Mono.just(createApiKeyResponse));
        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().build();
        when(apiGatewayService.addUsagePlanToApiKey(any(), any()))
                .thenReturn(Mono.just(createUsagePlanKeyResponse));
        when(aggregationConverter.convertToModel(any())).thenReturn(apikeyAggregateModel);
        StepVerifier.create(aggregationService.createNewAggregate(new InternalPaDetailDto()))
                .expectNextMatches(apiKeyAggregation1 -> apiKeyAggregation1.equalsIgnoreCase("aggregateId"))
                .verifyComplete();
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
        when(aggregateRepository.findByName(eq("name"), any())).thenReturn(Mono.just(page));
        when(aggregateRepository.countByName("name")).thenReturn(Mono.just(1));
        AggregatesListResponseDto aggregateListResponse = new AggregatesListResponseDto();
        List<AggregateRowDto> list = new ArrayList<>();
        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setId("id");
        list.add(aggregateRowDto);
        aggregateListResponse.setItems(list);
        when(aggregationConverter.convertToResponseDto(any(), anyList())).thenReturn(aggregateListResponse);
        StepVerifier.create(aggregationService.getAggregation("name", 10, "id", null))
                .expectNext(aggregateListResponse)
                .verifyComplete();
    }

    @Test
    void getAggregationTest2() {
        ApiKeyAggregateModel dto = new ApiKeyAggregateModel();
        dto.setName("name");
        dto.setAggregateId("id");
        Page<ApiKeyAggregateModel> page = Page.create(List.of(dto));
        when(aggregateRepository.findAll(any())).thenReturn(Mono.just(page));
        when(aggregateRepository.count()).thenReturn(Mono.just(1));
        AggregatesListResponseDto aggregateListResponse = new AggregatesListResponseDto();
        List<AggregateRowDto> list = new ArrayList<>();
        AggregateRowDto aggregateRowDto = new AggregateRowDto();
        aggregateRowDto.setId("id");
        list.add(aggregateRowDto);
        aggregateListResponse.setItems(list);
        when(aggregationConverter.convertToResponseDto(any(),anyList())).thenReturn(aggregateListResponse);
        StepVerifier.create(aggregationService.getAggregation(null, 10, "id", null))
                .expectNext(aggregateListResponse)
                .verifyComplete();
    }

    @Test
    void deleteAggregationTest() {
        PaAggregationModel paAggregation = new PaAggregationModel();
        paAggregation.setPaName("name");
        paAggregation.setPaId("paId");
        paAggregation.setAggregateId("id");
        Page<PaAggregationModel> page = Page.create(List.of(paAggregation));
        when(paAggregationRepository.findByAggregateId(eq("id"), any()))
                .thenReturn(Mono.just(page));
        StepVerifier.create(aggregationService.deleteAggregate("id"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testUpdateAggregate(){
        AggregateRequestDto dto = new AggregateRequestDto();
        dto.setName("name");
        dto.setDescription("description");
        dto.setUsagePlanId("usagePlanId");

        SaveAggregateResponseDto saveAggregateResponseDto = new SaveAggregateResponseDto();
        saveAggregateResponseDto.setId("id");

        ApiKeyAggregateModel model = new ApiKeyAggregateModel();
        model.setAggregateId("id");
        model.setUsagePlanId("usagePlanId");

        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().build();
        when(aggregateRepository.saveAggregation(model)).thenReturn(Mono.just(model));
        when(aggregateRepository.findById("id")).thenReturn(Mono.just(model));
        when(apiGatewayService.moveApiKeyToNewUsagePlan(model,dto))
                .thenReturn(Mono.just(createUsagePlanKeyResponse));
        StepVerifier.create(aggregationService.updateAggregate("id",dto))
                .expectNext(saveAggregateResponseDto)
                .verifyComplete();
    }
}
