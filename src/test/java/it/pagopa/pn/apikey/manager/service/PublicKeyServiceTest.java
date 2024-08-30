package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private final PublicKeyValidator validator = new PublicKeyValidator();
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = Mockito.mock(PublicKeyRepository.class);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder, validator);
    }

    @Test
    void deletePublicKey_Success() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setStatusHistory(List.of());
        publicKeyModel.setStatus("BLOCKED");

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectNext("Public key deleted")
                .verifyComplete();
    }

    @Test
    void deletePublicKey_CxTypeNotAllowed() {
        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PA, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void deletePublicKey_RoleNotAllowed() {
        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PA, "cxId", "kid", null, "USER");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void deletePublicKey_KeyNotFound() {
        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.empty());

        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.NOT_FOUND)
                .verify();
    }
}