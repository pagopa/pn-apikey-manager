package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
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

    @ParameterizedTest
    @CsvSource({
            "BLOCKED",
            "ACTIVE"
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

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ACTIVE", List.of(), "USER", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void changeStatusPublicKey_NotFound() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Not found", HttpStatus.NOT_FOUND)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ACTIVE", List.of(), "ADMIN", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InternalError() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(publicKeyService.changeStatus(anyString(), anyString(), anyString(), any(), anyString(), anyList(), anyString()))
                .thenReturn(Mono.error(new ApiKeyManagerException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR)));

        StepVerifier.create(publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "ACTIVE", List.of(), "ADMIN", exchange))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verify();
    }

    @Test
    void changeStatusPublicKey_InvalidStatus() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        Assertions.assertThrows(ApiKeyManagerException.class, () -> publicKeysController.changeStatusPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", "INVALID", List.of(), "ADMIN", exchange));
    }
}