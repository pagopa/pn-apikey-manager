package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.model.PaGroup;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.apigateway.model.CreateApiKeyResponse;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "pn.apikey.manager.pn-external-registries.base-path=path",
        "aws.region=eu-south-1"
})
@ExtendWith(SpringExtension.class)
class CreateApiKeyServiceTest {

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private AggregateRepository aggregateRepository;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private AggregationService aggregationService;

    @Autowired
    private CreateApiKeyService apiKeyService;

    @MockBean
    private PaAggregationsService paAggregationsService;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private ExternalRegistriesClient externalRegistriesClient;

    @MockBean
    private ApiGatewayService apiGatewayService;

    @Test
    void testCreateApiKey1() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups1");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Groups1");

        when(paAggregationsService.searchAggregationId("42")).thenReturn(Mono.empty());

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setApiKey("test");
        apikeyAggregateModel.setApiKeyId("id");
        apikeyAggregateModel.setAggregateId("1");
        when(aggregationService.createNewAggregate(any())).thenReturn(Mono.just("1"));

        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("1");
        when(paAggregationsService.createNewPaAggregation(any())).thenReturn(Mono.just(paAggregationModel));

        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(apikeyAggregateModel));
        when(apiGatewayService.createNewAwsApiKey("1")).thenReturn(Mono.just(CreateApiKeyResponse.builder().name("name").id("id").build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just(apikeyAggregateModel));
        when(aggregationService.createNewAggregate(any())).thenReturn(Mono.just("1"));

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        when(externalRegistriesClient.getPaById("42"))
                .thenReturn(Mono.just(new InternalPaDetailDto()));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectNext(responseNewApiKeyDto)
                .verifyComplete();
    }

    @Test
    void testCreateApiKey2() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups1");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Groups1");

        when(paAggregationsService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setApiKey("1");
        apikeyAggregateModel.setApiKeyId("1");
        apikeyAggregateModel.setAggregateId("1");
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(apikeyAggregateModel));
        when(apiGatewayService.createNewAwsApiKey("1")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just(apikeyAggregateModel));
        when(aggregationService.createNewAggregate(any())).thenReturn(Mono.just("1"));
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

        when(paAggregationsService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setApiKey("test");
        apikeyAggregateModel.setAggregateId("1");
        apikeyAggregateModel.setApiKeyId("id");
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(apikeyAggregateModel));
        when(apiGatewayService.createNewAwsApiKey("1")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just(apikeyAggregateModel));
        when(aggregationService.createNewAggregate(any())).thenReturn(Mono.just("1"));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        PaGroup group = new PaGroup();
        group.setId("Groups1");
        List<PaGroup> paGroups = new ArrayList<>();
        paGroups.add(group);

        when(externalRegistriesClient.getPaGroupsById(any())).thenReturn(Mono.just(paGroups));

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, new ArrayList<>()))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }

    @Test
    void testCreateApiKey4() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("Groups1");
        when(paAggregationsService.searchAggregationId("42")).thenReturn(Mono.just("1"));

        ApiKeyAggregateModel apikeyAggregateModel = new ApiKeyAggregateModel();
        apikeyAggregateModel.setApiKey("test");
        apikeyAggregateModel.setAggregateId("1");
        apikeyAggregateModel.setApiKeyId("id");
        when(aggregationService.getApiKeyAggregation("1")).thenReturn(Mono.just(apikeyAggregateModel));
        when(apiGatewayService.createNewAwsApiKey("1")).thenReturn(Mono.just(CreateApiKeyResponse.builder().build()));
        when(aggregationService.addAwsApiKeyToAggregate(any(), any())).thenReturn(Mono.just(apikeyAggregateModel));
        when(aggregationService.createNewAggregate(any())).thenReturn(Mono.just("1"));
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("idtest");
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setId("idtest");

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, stringList))
                .expectNext(responseNewApiKeyDto).verifyComplete();
    }

    @Test
    void testCreateApiKey5() {
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        requestNewApiKeyDto.addGroupsItem("Groups1");

        PaGroup group = new PaGroup();
        group.setId("Groups2");
        List<PaGroup> paGroups = new ArrayList<>();
        paGroups.add(group);

        when(externalRegistriesClient.getPaGroupsById(any())).thenReturn(Mono.just(paGroups));

        StepVerifier.create(apiKeyService.createApiKey("1234", CxTypeAuthFleetDto.PA, "42", requestNewApiKeyDto, new ArrayList<>()))
                .expectError(ApiKeyManagerException.class).verify();
    }
}
