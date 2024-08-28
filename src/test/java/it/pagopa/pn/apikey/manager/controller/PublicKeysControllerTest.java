package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PublicKeysControllerTest {

    private PublicKeyService publicKeyService;
    private PublicKeysController publicKeysController;

    @BeforeEach
    void setUp() {
        publicKeyService = mock(PublicKeyService.class);
        publicKeysController = new PublicKeysController(publicKeyService, new PnAuditLogBuilder());
    }

    @Test
    void testRotatePublicKey() {
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));

        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        when(publicKeyService.rotatePublicKey(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(publicKeyResponseDto));

        StepVerifier.create(publicKeysController.rotatePublicKey("xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", "kid", Mono.just(new PublicKeyRequestDto()), List.of("group"), "ADMIN", null))
                .expectNext(ResponseEntity.ok().body(publicKeyResponseDto))
                .verifyComplete();
    }
}