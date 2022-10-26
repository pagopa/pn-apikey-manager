package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.service.ApiKeyService;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ApiKeysControllerTest {

    @InjectMocks
    ApiKeysController apiKeysController;

    @Mock
    ServerWebExchange serverWebExchange;

    @Mock
    ApiKeyService apiKeyService;

    @Test
    void testGetApiKeys() {
        String xPagopaPnUid = "PA-test-1";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PA;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        Integer limit = 10;
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);
        apiKeysResponseDto.setLastKey(lastKey);
        apiKeysResponseDto.setLastUpdate(lastUpdate);
        when(apiKeyService.getApiKeyList(anyString(),any(),anyInt(),anyString(),anyString(),anyBoolean())).thenReturn(Mono.just(apiKeysResponseDto));
        StepVerifier.create(apiKeysController.getApiKeys(xPagopaPnUid,xPagopaPnCxType,xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,lastUpdate,showVirtualKey,serverWebExchange))
                .expectNext(ResponseEntity.ok().body(apiKeysResponseDto));
    }

}

