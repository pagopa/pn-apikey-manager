package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
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
    void deletePublicKeysSuccessfully() {
        String xPagopaPnUid = "user123";
        String xPagopaPnCxId = "cxId123";
        String kid = "kid123";
        List<String> xPagopaPnCxGroups = List.of("group1");
        String xPagopaPnCxRole = "role1";
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(publicKeyService.deletePublicKey(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole))
                .thenReturn(Mono.just("Public key deleted"));

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, kid, xPagopaPnCxGroups, exchange);

        StepVerifier.create(response)
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();
    }

    @Test
    void deletePublicKeysNotFound() {
        String xPagopaPnUid = "user123";
        String xPagopaPnCxId = "cxId123";
        String kid = "kid123";
        List<String> xPagopaPnCxGroups = List.of("group1");
        String xPagopaPnCxRole = "role1";
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(publicKeyService.deletePublicKey(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole))
                .thenReturn(Mono.error(new RuntimeException("Not Found")));

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, kid, xPagopaPnCxGroups, exchange);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Not Found"))
                .verify();
    }

    @Test
    void deletePublicKeysInternalError() {
        String xPagopaPnUid = "user123";
        String xPagopaPnCxId = "cxId123";
        String kid = "kid123";
        List<String> xPagopaPnCxGroups = List.of("group1");
        String xPagopaPnCxRole = "role1";
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(publicKeyService.deletePublicKey(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole))
                .thenReturn(Mono.error(new RuntimeException("Internal Error")));

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, kid, xPagopaPnCxGroups, exchange);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal Error"))
                .verify();
    }

    @Test
    void getIssuerStatusSuccessfully() {
        String xPagopaPnUid = "user123";
        String xPagopaPnCxId = "cxId123";
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        PublicKeysIssuerResponseDto responseDto = new PublicKeysIssuerResponseDto();
        responseDto.setIsPresent(true);
        responseDto.setIssuerStatus(PublicKeysIssuerResponseDto.IssuerStatusEnum.ACTIVE);

        when(publicKeyService.getIssuer(xPagopaPnCxId, CxTypeAuthFleetDto.PG))
                .thenReturn(Mono.just(responseDto));

        Mono<ResponseEntity<PublicKeysIssuerResponseDto>> response = publicKeysController.getIssuerStatus(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, exchange);

        StepVerifier.create(response)
                .expectNext(ResponseEntity.ok().body(responseDto))
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "BLOCK",
            "ENABLE"
    })
    void changeStatusPublicKey_Success(String status) {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = publicKeysController.changeStatusPublicKey(
                "uid", CxTypeAuthFleetDto.PG, "cxId", "USER","kid", status, List.of(), exchange);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    void changeStatusPublicKey_Unauthorized() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("User is not authorized to perform this action", HttpStatus.FORBIDDEN)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId","USER", "kid", "ENABLE", List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void changeStatusPublicKey_NotFound() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Not found", HttpStatus.NOT_FOUND)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ADMIN","ENABLE", List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InternalError() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "ADMIN", "kid", "ENABLE", List.of(), exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InvalidStatus() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        Assertions.assertThrows(ApiKeyManagerException.class, () -> publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "ADMIN", "kid", "INVALID", List.of(), exchange));
    }

    @Test
    void testNewPublicKey() {
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        when(publicKeyService.createPublicKey(any(), any(), anyString(), any(), any(), any())).thenReturn(Mono.just(publicKeyResponseDto));

        StepVerifier.create(publicKeysController.newPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "role", Mono.just(new PublicKeyRequestDto()), List.of(),
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().body(publicKeyResponseDto))
                .verifyComplete();
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

        StepVerifier.create(publicKeysController.rotatePublicKey("xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", "ADMIN", "kid", Mono.just(new PublicKeyRequestDto()), List.of("group"), null))
                .expectNext(ResponseEntity.ok().body(publicKeyResponseDto))
                .verifyComplete();
    }

    @Test
    void testGetPublicKeys() {
        String xPagopaPnUid = "uidTest";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        String xPagopaPnCxRole = "ADMIN";
        Boolean showPublicKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String createdAt = "2024-10-25T16:25:58.334862500";

        PublicKeysResponseDto publicKeysResponseDto = new PublicKeysResponseDto();
        List<PublicKeyRowDto> publicKeyRowDtos = new ArrayList<>();
        publicKeysResponseDto.setItems(publicKeyRowDtos);
        publicKeysResponseDto.setLastKey(lastKey);
        publicKeysResponseDto.setCreatedAt(createdAt);

        when(publicKeyService.getPublicKeys(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(publicKeysResponseDto));
        StepVerifier.create(publicKeysController.getPublicKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups, 10, lastKey, createdAt, showPublicKey, null))
                .expectNext(ResponseEntity.ok().body(publicKeysResponseDto))
                .verifyComplete();
    }
}