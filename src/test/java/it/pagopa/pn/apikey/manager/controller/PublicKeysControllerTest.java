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

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole, exchange);

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

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole, exchange);

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

        Mono<ResponseEntity<Void>> response = publicKeysController.deletePublicKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, kid, xPagopaPnCxGroups, xPagopaPnCxRole, exchange);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal Error"))
                .verify();
    }

}