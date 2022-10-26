package it.pagopa.pn.apikey.manager.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;

import java.util.ArrayList;

import java.util.List;

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
class ManageApiKeyServiceTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private ApiGatewayAsyncClient apiGatewayAsyncClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ManageApiKeyService apiKeyService;

    @MockBean
    private ApiKeyConverter apiKeyConverter;

    /**
     * Method under test: {@link ManageApiKeyService#changeStatus(String, String, String)}
     */
    @Test
    void testChangeStatus2() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("BLOCKED");

        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        apiKeyModel1.setId("42");
        apiKeyModel1.setStatus("ENABLED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
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

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
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

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
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

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
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

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel1));
        StepVerifier.create(apiKeyService.changeStatus("42", "ROTATE", "1234"))
                .expectNext(apiKeyModel1).verifyComplete();
    }

    @Test
    void testDelete1() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ROTATED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(apiKeyService.deleteApiKey("42"))
                .expectError(ApiKeyManagerException.class).verify();
    }

    @Test
    void testDelete2() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ENABLED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.delete(any())).thenReturn(Mono.just("id"));
        StepVerifier.create(apiKeyService.deleteApiKey("42")).expectNext("id").verifyComplete();
    }

    @Test
    void testGetApiKeyList() {

        String xPagopaPnUid = "PA-test-1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        int limit = 10;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(apiKeyModel);

        when(apiKeyRepository.getAllWithFilter(anyString(), anyList(), anyInt(), anyString()))
                .thenReturn(Mono.just(apiKeyModels));
        when(apiKeyConverter.convertResponsetoDto(anyList())).thenReturn(apiKeysResponseDto);
        StepVerifier.create(apiKeyService.getApiKeyList(xPagopaPnUid, xPagopaPnCxGroups, limit, lastKey)).expectNext(apiKeysResponseDto).verifyComplete();
    }

}

