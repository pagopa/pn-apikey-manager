package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                .expectNext(ResponseEntity.ok().build())
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
                "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", status, List.of(), "USER", exchange);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    void changeStatusPublicKey_Unauthorized() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("User is not authorized to perform this action", HttpStatus.FORBIDDEN)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ENABLE", List.of(), "USER", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void changeStatusPublicKey_NotFound() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Not found", HttpStatus.NOT_FOUND)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ENABLE", List.of(), "ADMIN", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InternalError() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ENABLE", List.of(), "ADMIN", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InvalidStatus() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        Assertions.assertThrows(ApiKeyManagerException.class, () -> publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "INVALID", List.of(), "ADMIN", exchange));
    }

}