package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PnApikeyManagerConfig.class})
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class VirtualKeyControllerTest {

    private VirtualKeyService virtualKeyService;
    private VirtualKeyController virtualKeyController;

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
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.valueOf(status));

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectNextMatches(response -> response.getStatusCode().value() == expectedStatus)
                .verifyComplete();
    }

    @Test
    void changeStatusVirtualKeys_Success() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectNext(ResponseEntity.ok().build())
                .verifyComplete();
    }

    @Test
    void changeStatusVirtualKeys_BadRequest() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
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
        ServerWebExchange exchange = mock(ServerWebExchange.class);
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
        ServerWebExchange exchange = mock(ServerWebExchange.class);
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
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        when(virtualKeyService.changeStatusVirtualKeys(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Forbidden", HttpStatus.FORBIDDEN)));

        StepVerifier.create(virtualKeyController.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "cxRole", "id", Mono.just(requestDto), List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }
}

