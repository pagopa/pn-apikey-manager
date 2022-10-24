package it.pagopa.pn.apikey.manager.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {
    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyConverter apiKeyConverter;

    @InjectMocks
    private ApiKeyService apiKeyService;

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

