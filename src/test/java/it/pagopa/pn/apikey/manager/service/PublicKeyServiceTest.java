package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private final PublicKeyValidator validator = new PublicKeyValidator();
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder, validator);
    }

    @Test
    void createPublicKey_withValidData_returnsPublicKeyResponseDto() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("Test Key");
        requestDto.setPublicKey("publicKeyData");

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setName("Test Key");
        publicKeyModel.setCorrelationId("correlationId");
        publicKeyModel.setPublicKey("publicKeyData");
        publicKeyModel.setStatus("ACTIVE");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setCxId("cxId");

        PublicKeyModel publicKeyModelCopy = new PublicKeyModel();
        publicKeyModelCopy.setKid("kid_COPY");
        publicKeyModelCopy.setName("Test Key");
        publicKeyModelCopy.setCorrelationId("correlationId");
        publicKeyModelCopy.setPublicKey("publicKeyData");
        publicKeyModelCopy.setStatus("ACTIVE");
        publicKeyModelCopy.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModelCopy.setIssuer("issuer");
        publicKeyModelCopy.setTtl(publicKeyModelCopy.getExpireAt());
        publicKeyModelCopy.setCxId("cxId");
        

        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ACTIVE"))).thenReturn(Flux.empty());
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModelCopy));

        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), List.of(), "ADMIN"))
                .expectNextMatches(response -> response.getKid() != null && response.getIssuer() != null)
                .verifyComplete();
    }

    @Test
    void createPublicKey_withExistingActiveKey_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ACTIVE"))).thenReturn(Flux.just(publicKeyModel));
        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");

        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(dto), List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Public key with status ACTIVE already exists, to create a new public key use the rotate operation."))
                .verify();
    }

    @Test
    void createPublicKey_withInvalidRole_throwsApiKeyManagerException() {
        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(new PublicKeyRequestDto()), List.of(), "invalidRole"))
                .expectErrorMatches(throwable -> throwable instanceof PnForbiddenException && throwable.getMessage().contains("Accesso negato!"))
                .verify();
    }
}