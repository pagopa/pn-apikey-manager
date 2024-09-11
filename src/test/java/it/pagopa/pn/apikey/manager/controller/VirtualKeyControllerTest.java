package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {PnApikeyManagerConfig.class})
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class VirtualKeyControllerTest {

    private VirtualKeyService virtualKeyService;
    private VirtualKeyController virtualKeyController;
    @MockBean
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        virtualKeyService = mock(VirtualKeyService.class);
        virtualKeyController = new VirtualKeyController(virtualKeyService, new PnAuditLogBuilder());
    }

    @ParameterizedTest
    @CsvSource({
            "ROTATE, 200",
            "BLOCK, 200",
            "ENABLE, 200"
    })
    void changeStatusVirtualKeys_Success(String status, int expectedStatus) {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.valueOf(status));

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectNextMatches(response -> response.getStatusCode().value() == expectedStatus)
                .verifyComplete();
    }

    @Test
    void changeStatusVirtualKeys_Success() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectNext(ResponseEntity.ok().build())
                .verifyComplete();
    }

    @Test
    void changeStatusVirtualKeys_BadRequest() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Bad request", HttpStatus.BAD_REQUEST)));

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void changeStatusVirtualKeys_Conflict() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Conflict", HttpStatus.CONFLICT)));

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void changeStatusVirtualKeys_NotFound() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Not found", HttpStatus.NOT_FOUND)));

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void changeStatusVirtualKeys_Forbidden() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Forbidden", HttpStatus.FORBIDDEN)));

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void deleteVirtualKey_InternalErrorTest() {
        when(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", null,
                "xPagopaPnCxId", null, "xPagopaPnCxRole"))
                .thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyController.deleteVirtualKey("xPagopaPnUid", null,
                        "xPagopaPnCxId", "xPagopaPnCxRole",
                        "id", null, exchange))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal error"))
                .verify();
    }

    @Test
    void deleteVirtualKey_SuccessTest() {
        when(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", null,
                "xPagopaPnCxId", null, "xPagopaPnCxRole"))
                .thenReturn(Mono.just("Successfully deleted"));

        StepVerifier.create(virtualKeyController.deleteVirtualKey("xPagopaPnUid", null,
                        "xPagopaPnCxId", "xPagopaPnCxRole",
                        "id", null, exchange))
                .expectNext(ResponseEntity.ok().build())
                .verifyComplete();
    }

    @Test
    void deleteVirtualKey_BadRequest() {
        when(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", null,
                "xPagopaPnCxId", null, "xPagopaPnCxRole"))
                .thenReturn(Mono.error(new IllegalArgumentException("Bad Request")));

        StepVerifier.create(virtualKeyController.deleteVirtualKey("xPagopaPnUid", null,
                        "xPagopaPnCxId", "xPagopaPnCxRole",
                        "id", null, exchange))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Bad Request"))
                .verify();
    }

    @Test
    void testGetVirtualKeys() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        VirtualKeysResponseDto virtualKeysResponseDto = new VirtualKeysResponseDto();
        List<VirtualKeyDto> virtualKeyDtos = new ArrayList<>();
        virtualKeysResponseDto.setItems(virtualKeyDtos);
        virtualKeysResponseDto.setLastKey(lastKey);
        virtualKeysResponseDto.setLastUpdate(lastUpdate);

        when(virtualKeyService.getVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, "ADMIN", 10, lastKey, lastUpdate, showVirtualKey))
                .thenReturn(Mono.just(virtualKeysResponseDto));

        StepVerifier.create(virtualKeyController.getVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, "ADMIN", xPagopaPnCxGroups, 10, lastKey, lastUpdate, showVirtualKey, exchange))
                .expectNext(ResponseEntity.ok().body(virtualKeysResponseDto))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_Success() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        ResponseNewVirtualKeyDto responseDto = new ResponseNewVirtualKeyDto();
        responseDto.setId("id");

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(responseDto));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", "ADMIN", Mono.just(requestDto), null, exchange))
                .expectNext(ResponseEntity.status(HttpStatus.CREATED).body(responseDto))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_BadRequest() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any(), any(), any())).thenReturn(Mono.error(new IllegalArgumentException("Bad request")));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", "ADMIN", Mono.just(requestDto), null, exchange))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Bad request"))
                .verify();
    }

    @Test
    void createVirtualKey_InternalError() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", "ADMIN", Mono.just(requestDto), null, exchange))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal error"))
                .verify();
    }
}

