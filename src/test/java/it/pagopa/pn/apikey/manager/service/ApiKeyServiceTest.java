package it.pagopa.pn.apikey.manager.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {
    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyConverter apiKeyConverter;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @Test
    void testGetApiKeyList() {
        String xPagopaPnUid = "cxId";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        int limit = 10;
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(new ApiKeyModel());

        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        apiKeysResponseDto.setItems(new ArrayList<>());
        apiKeysResponseDto.setLastKey(lastKey);
        apiKeysResponseDto.setLastUpdate(lastUpdate);

        when(apiKeyRepository.getAllWithFilter(anyString(), anyList(), anyInt(), anyString(), anyString()))
                .thenReturn(Mono.just(page));
        when(apiKeyConverter.convertResponsetoDto(any(),anyBoolean())).thenReturn(apiKeysResponseDto);
        StepVerifier.create(apiKeyService.getApiKeyList(xPagopaPnUid, xPagopaPnCxGroups, limit, lastKey, lastUpdate, showVirtualKey)).expectNext(apiKeysResponseDto).verifyComplete();

    }



}

