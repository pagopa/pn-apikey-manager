package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponsePdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.model.PaGroup;
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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "pn.apikey.manager.pn-external-registries.base-path=path",
        "aws.region=eu-south-1",
        "pn.apikey.manager.flag.pdnd=true"
})
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

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private ExternalRegistriesClient externalRegistriesClient;

    @Test
    void changePdnd(){
        List<ApiPdndDto> apiPdndDtos = new ArrayList<>();
        ApiPdndDto apiPdndDto = new ApiPdndDto();
        apiPdndDto.setPdnd(true);
        apiPdndDto.setId("id");
        apiPdndDtos.add(apiPdndDto);

        ResponsePdndDto apiKeyResponsePdndDto = new ResponsePdndDto();
        apiKeyResponsePdndDto.setApikeyNonModificate(apiPdndDtos.stream().map(ApiPdndDto::getId).toList());

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");

        when(apiKeyRepository.changePdnd("id",true)).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyConverter.convertToResponsePdnd(any(),any())).thenReturn(apiKeyResponsePdndDto);

        StepVerifier.create(apiKeyService.changePdnd(apiPdndDtos))
                .expectNext(apiKeyResponsePdndDto)
                .verifyComplete();
    }

    @Test
    void testChangeVirtualKey() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("cxId");

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(apiKeyModel);

        when(apiKeyRepository.findByCxId("cxId")).thenReturn(Mono.just(apiKeyModels));
        when(apiKeyRepository.setNewVirtualKey(anyList(),any())).thenReturn(Mono.just(apiKeyModels));

        StepVerifier.create(apiKeyService.changeVirtualKey("cxId", "virtualKey"))
                .expectNext(apiKeyModels)
                .verifyComplete();
    }

    @Test
    void testChangeVirtualKeyEmpty() {
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();

        when(apiKeyRepository.findByCxId("cxId")).thenReturn(Mono.just(apiKeyModels));

        StepVerifier.create(apiKeyService.changeVirtualKey("cxId", "virtualKey"))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testChangeVirtualKeyMoreOneElement() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        ApiKeyModel apiKeyModel1 = new ApiKeyModel();
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(apiKeyModel);
        apiKeyModels.add(apiKeyModel1);

        when(apiKeyRepository.findByCxId("cxId")).thenReturn(Mono.just(apiKeyModels));

        StepVerifier.create(apiKeyService.changeVirtualKey("cxId", "virtualKey"))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    /**
     * Method under test: {@link ManageApiKeyService#changeStatus(String, RequestApiKeyStatusDto, String, CxTypeAuthFleetDto)}
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

        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.ENABLE);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PA))
                .expectNext(apiKeyModel1)
                .verifyComplete();
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

        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.ROTATE);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PA))
                .expectError(ApiKeyManagerException.class)
                .verify();
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

        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.BLOCK);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PA))
                .expectNext(apiKeyModel1)
                .verifyComplete();
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

        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.ENABLE);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PA))
                .expectError(ApiKeyManagerException.class)
                .verify();
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

        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.ROTATE);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PA))
                .expectNext(apiKeyModel1)
                .verifyComplete();
    }

    @Test
    void testChangeStatus6() {
        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(RequestApiKeyStatusDto.StatusEnum.ROTATE);

        StepVerifier.create(apiKeyService.changeStatus("42", requestApiKeyStatusDto, "1234", CxTypeAuthFleetDto.PF))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testDelete1() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("ROTATED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(apiKeyService.deleteApiKey("42", CxTypeAuthFleetDto.PA))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testDelete2() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("42");
        apiKeyModel.setStatus("BLOCKED");

        when(apiKeyRepository.findById("42")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.delete(any())).thenReturn(Mono.just("42"));
        StepVerifier.create(apiKeyService.deleteApiKey("42", CxTypeAuthFleetDto.PA))
                .expectNext("42")
                .verifyComplete();
    }

    @Test
    void testDelete3() {
        StepVerifier.create(apiKeyService.deleteApiKey("42", CxTypeAuthFleetDto.PF))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testGetApiKeyList() {
        String xPagopaPnUid = "cxId";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("0001");
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        ApiKeyModel apiKey = new ApiKeyModel();
        apiKey.setVirtualKey("virtualKey");
        apiKey.setStatus("ENABLED");
        apiKey.setId("id");
        apiKey.setCxId("cxId");
        apiKey.setName("name");
        apiKey.setStatusHistory(new ArrayList<>());
        apiKey.setCxType("PA");
        apiKey.setUid("uid");
        apiKey.setGroups(List.of("0001"));
        apiKeyModels.add(apiKey);

        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        ApiKeyRowDto apiKeyDto = new ApiKeyRowDto();
        apiKeyDto.setValue("virtualKey");
        apiKeyDto.setStatus(ApiKeyStatusDto.ENABLED);
        apiKeyDto.setId("id");
        apiKeyDto.setName("name");
        apiKeyDto.setStatusHistory(new ArrayList<>());
        apiKeyDto.setGroups(List.of("0001"));
        apiKeysResponseDto.setItems(List.of(apiKeyDto));
        apiKeysResponseDto.setLastKey(lastKey);
        apiKeysResponseDto.setLastUpdate(lastUpdate);

        List<PaGroup> paGroups = new ArrayList<>();
        PaGroup paGroup = new PaGroup();
        paGroup.setId("0001");
        paGroup.setName("Tributi");
        paGroups.add(paGroup);

        when(apiKeyRepository.getAllWithFilter(anyString(), anyList(), any()))
                .thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(anyString(), anyList()))
                .thenReturn(Mono.just(1));
        when(apiKeyConverter.convertResponsetoDto(any(),anyBoolean())).thenReturn(apiKeysResponseDto);
        when(externalRegistriesClient.getPaGroupsById(any(), any())).thenReturn(Mono.just(paGroups));
        StepVerifier.create(apiKeyService.getApiKeyList(xPagopaPnUid, xPagopaPnCxGroups, 10, lastKey, lastUpdate, showVirtualKey, CxTypeAuthFleetDto.PA))
                .expectNext(apiKeysResponseDto)
                .verifyComplete();
    }

}
