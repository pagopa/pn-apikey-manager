package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ResponseNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @BeforeEach
    void setUp() {
        virtualKeyService = mock(VirtualKeyService.class);
        virtualKeyController = new VirtualKeyController(virtualKeyService, new PnAuditLogBuilder());
    }

    /*@Test
    void createVirtualKey_Success() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        ResponseNewVirtualKeyDto responseDto = new ResponseNewVirtualKeyDto();
        responseDto.setId("id");

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any())).thenReturn(Mono.just(responseDto));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), exchange))
                .expectNext(ResponseEntity.ok().body(responseDto))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_BadRequest() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any())).thenReturn(Mono.error(new IllegalArgumentException("Bad request")));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), exchange))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Bad request"))
                .verify();
    }

    @Test
    void createVirtualKey_InternalError() {

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(virtualKeyService.createVirtualKey(any(), any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyController.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), exchange))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal error"))
                .verify();
    }*/

    @Test
    void deleteVirtualKey_InternalErrorTest() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);

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
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", null,
                "xPagopaPnCxId", null, "xPagopaPnCxRole"))
                .thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyController.deleteVirtualKey("xPagopaPnUid", null,
                        "xPagopaPnCxId", "xPagopaPnCxRole",
                        "id", null, exchange))
                .expectNext(ResponseEntity.ok().build());
    }

    @Test
    void deleteVirtualKey_BadRequest() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", null,
                "xPagopaPnCxId", null, "xPagopaPnCxRole"))
                .thenReturn(Mono.error(new IllegalArgumentException("Bad Request")));

        StepVerifier.create(virtualKeyController.deleteVirtualKey("xPagopaPnUid", null,
                        "xPagopaPnCxId", "xPagopaPnCxRole",
                        "id", null, exchange))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Bad Request"))
                .verify();
    }

}