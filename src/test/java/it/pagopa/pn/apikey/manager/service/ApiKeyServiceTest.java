package it.pagopa.pn.apikey.manager.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import java.util.ArrayList;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ApiKeyServiceTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private AggregationRepository aggregationRepository;

    @MockBean
    private ApiGatewayAsyncClient apiGatewayAsyncClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private AggregationService aggregationService;

    @Autowired
    private ApiKeyService apiKeyService;

    @MockBean
    private PaService paService;

    /**
     * Method under test: {@link ApiKeyService#createApiKey(String, CxTypeAuthFleetDto, String, RequestNewApiKeyDto, List)}
     */
    @Test
    void testCreateApiKey() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups Item");
        requestNewApiKeyDto.addGroupsItem("list groupsToAdd size: {}");
        requestNewApiKeyDto.addGroupsItem("list groupsToAdd size: {}");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("list groupsToAdd size: {}");
        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testCreateApiKey2() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups1");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Groups1");

        when(paService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        apiKeyAggregation.setApiKey("test");
        apiKeyAggregation.setApiKeyId("id");
        apiKeyAggregation.setAggregateId("1");
        when(aggregationService.searchAwsApiKey("1")).thenReturn(Mono.just(apiKeyAggregation));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just("id"));
        when(aggregationService.createNewAggregation(any())).thenReturn(Mono.just(apiKeyAggregation));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }

    @Test
    void testCreateApiKey3() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups1");

        when(paService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        apiKeyAggregation.setApiKey("test");
        apiKeyAggregation.setAggregateId("1");
        apiKeyAggregation.setApiKeyId("id");
        when(aggregationService.searchAwsApiKey("1")).thenReturn(Mono.just(apiKeyAggregation));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just("id"));
        when(aggregationService.createNewAggregation(any())).thenReturn(Mono.just(apiKeyAggregation));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto,new ArrayList<>()))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }

    @Test
    void testCreateApiKey4() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Groups1");
        when(paService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregation apiKeyAggregation = new ApiKeyAggregation();
        apiKeyAggregation.setApiKey("test");
        apiKeyAggregation.setAggregateId("1");
        apiKeyAggregation.setApiKeyId("id");
        when(aggregationService.searchAwsApiKey("1")).thenReturn(Mono.just(apiKeyAggregation));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just("id"));
        when(aggregationService.createNewAggregation(any())).thenReturn(Mono.just(apiKeyAggregation));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }

    /**
     * Method under test: {@link ApiKeyService#changeStatus(String, String, String)}
     */
    @Test
    void testChangeStatus2() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("BLOCKED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("ENABLED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "ENABLE", "1234"))
                .expectNext(apiKeyModel1).verifyComplete();
    }

    @Test
    void testChangeStatus2bis() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("BLOCKED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("ENABLED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "test", "1234"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testChangeStatus3() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "BLOCK", "1234"))
                .expectNext(apiKeyModel1).verifyComplete();
    }

    @Test
    void testChangeStatus4() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "ENABLE", "1234"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testChangeStatus5() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "ROTATE", "1234"))
                .expectNext(apiKeyModel1).verifyComplete();
    }

    @Test
    void testChangeStatus6() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel,apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "ROTATE", "1234"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testDelete1() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ROTATED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        StepVerifier.create(apiKeyService.deleteApiKey("42"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testDelete2() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel)));
        when(apiKeyRepository.delete(any())).thenReturn(Mono.just("id"));
        StepVerifier.create(apiKeyService.deleteApiKey("42")).expectNext("id").verifyComplete();
    }

    @Test
    void testDelete3() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(List.of(apiKeyModel,apiKeyModel)));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.deleteApiKey("42"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    private static Integer limit;
    private static String xPagopaPnUid;
    private static String lastKey;
    private static List<String> xPagopaPnCxGroups;
    private static ApiKeysResponseDto apiKeysResponseDto;
    private static List<ApiKeyModel> apiKeyModels;

    @BeforeAll
    static void setup(){
        xPagopaPnUid = "PA-test-1";
        xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        limit = 10;
        lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";

        apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModels = new ArrayList<>();
        apiKeyModels.add(apiKeyModel);
    }

    @Test
    void testGetApiKeyList() {
        when(apiKeyRepository.getAllWithFilter(anyString(), anyList(), anyInt(), anyString()))
                .thenReturn(Mono.just(apiKeyModels));
        when(apiKeyConverter.convertResponsetoDto(anyList())).thenReturn(apiKeysResponseDto);
        StepVerifier.create(apiKeyService.getApiKeyList(xPagopaPnUid, xPagopaPnCxGroups, limit, lastKey)).expectNext(apiKeysResponseDto).verifyComplete();
    }

}

