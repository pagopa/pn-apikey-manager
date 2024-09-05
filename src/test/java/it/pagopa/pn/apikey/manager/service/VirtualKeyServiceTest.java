package it.pagopa.pn.apikey.manager.service;


import it.pagopa.pn.apikey.manager.client.PnDataVaultClient;
import it.pagopa.pn.apikey.manager.converter.VirtualKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class VirtualKeyServiceTest {

    private VirtualKeyService virtualKeyService;
    private ApiKeyRepository apiKeyRepository;
    private PnDataVaultClient pnDataVaultClient;

    @BeforeEach
    void setUp() {
        apiKeyRepository = mock(ApiKeyRepository.class);
        pnDataVaultClient = mock(PnDataVaultClient.class);
        virtualKeyService = new VirtualKeyService(apiKeyRepository, new VirtualKeyConverter(), pnDataVaultClient);
    }

    @Test
    void getVirtualKeys_whenCxTypeNotAllowed_shouldReturnError() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of("group1");
        String xPagopaPnCxRole = "ADMIN";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "lastUpdate";
        Boolean showPublicKey = true;

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void getVirtualKeys_whenValidInput_adminTrue() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "ADMIN";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "2024-09-04T16:25:58.334862500";
        Boolean showPublicKey = true;

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(getApiKeyModel());
        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        BaseRecipientDtoDto baseRecipientDto = new BaseRecipientDtoDto();
        baseRecipientDto.setInternalId("internalId");
        baseRecipientDto.setTaxId("taxId");
        baseRecipientDto.setDenomination("denomination");

        VirtualKeysResponseDto responseDto = new VirtualKeysResponseDto();
        VirtualKeyDto virtualKeyDto = getVirtualKeyDto();
        responseDto.setItems(List.of(virtualKeyDto));
        responseDto.setLastKey(null);
        responseDto.setLastUpdate(null);
        responseDto.setTotal(1);

        when(apiKeyRepository.getVirtualKeys(any(), any(), any(), any(), anyBoolean())).thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(any(), any(), anyBoolean())).thenReturn(Mono.just(1));
        when(pnDataVaultClient.getRecipientDenominationByInternalId(any())).thenReturn(Flux.just(baseRecipientDto));

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }

    private ApiKeyModel getApiKeyModel() {
        ApiKeyModel apiKey = new ApiKeyModel();
        apiKey.setVirtualKey("virtualKey");
        apiKey.setStatus("ENABLED");
        apiKey.setId("id");
        apiKey.setCxId("cxId");
        apiKey.setName("name");
        apiKey.setStatusHistory(new ArrayList<>());
        apiKey.setCxType("PG");
        apiKey.setUid("internalId");
        return apiKey;
    }

    private VirtualKeyDto getVirtualKeyDto() {
        VirtualKeyDto virtualKeyDto = new VirtualKeyDto();
        UserDtoDto userDto = new UserDtoDto();
        userDto.setFiscalCode("taxId");
        userDto.setDenomination("denomination");
        virtualKeyDto.setValue("virtualKey");
        virtualKeyDto.setStatus(VirtualKeyStatusDto.ENABLED);
        virtualKeyDto.setId("id");
        virtualKeyDto.setName("name");
        virtualKeyDto.setUser(userDto);
        return virtualKeyDto;
    }

    @Test
    void getVirtualKeys_whenValidInput_adminFalse() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "operator";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "2024-09-04T16:25:58.334862500";
        Boolean showPublicKey = true;

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(getApiKeyModel());
        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        VirtualKeysResponseDto responseDto = new VirtualKeysResponseDto();
        VirtualKeyDto virtualKeyDto = getVirtualKeyDto();
        virtualKeyDto.setUser(null);
        responseDto.setItems(List.of(virtualKeyDto));
        responseDto.setLastKey(null);
        responseDto.setLastUpdate(null);
        responseDto.setTotal(1);

        when(apiKeyRepository.getVirtualKeys(any(), any(), any(), any(), anyBoolean())).thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(any(), any(), anyBoolean())).thenReturn(Mono.just(1));

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }


}