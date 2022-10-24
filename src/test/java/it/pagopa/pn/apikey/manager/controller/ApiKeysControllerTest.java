package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.service.ApiKeyService;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
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

    private static Integer limit;
    private static String xPagopaPnUid;
    private static String lastKey;
    private static String xPagopaPnCxId;
    private static List<String> xPagopaPnCxGroups;
    private static CxTypeAuthFleetDto xPagopaPnCxType;
    private static ApiKeysResponseDto apiKeysResponseDto;

    @BeforeAll
    static void setup(){
        xPagopaPnUid = "PA-test-1";
        xPagopaPnCxType = CxTypeAuthFleetDto.PA;
        xPagopaPnCxId = "user1";
        xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        limit = 10;
        lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";

        apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);
    }

    @Test
    void testGetApiKeys() {
        when(apiKeyService.getApiKeyList(anyString(),any(),anyInt(),anyString())).thenReturn(Mono.just(apiKeysResponseDto));
        StepVerifier.create(apiKeysController.getApiKeys(xPagopaPnUid,xPagopaPnCxType,xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,serverWebExchange))
                .expectNext(ResponseEntity.ok().body(apiKeysResponseDto));
    }
}

