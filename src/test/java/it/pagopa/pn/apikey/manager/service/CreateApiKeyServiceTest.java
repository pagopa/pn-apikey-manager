package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.repository.AggregationRepository;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
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
import software.amazon.awssdk.services.apigateway.model.CreateApiKeyResponse;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class CreateApiKeyServiceTest {

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
    private CreateApiKeyService apiKeyService;

    @MockBean
    private PaService paService;

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
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(new ApiKeyAggregation()));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just("apiKey"));
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
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(new ApiKeyAggregation()));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just("apiKey"));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, new ArrayList<>()))
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
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(new ApiKeyAggregation()));
        when(aggregationService.createNewAwsApiKey("42")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just("apiKey"));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }
}
