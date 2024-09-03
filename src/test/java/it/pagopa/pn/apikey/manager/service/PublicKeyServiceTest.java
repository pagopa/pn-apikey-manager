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
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        PublicKeyValidator validator = new PublicKeyValidator(publicKeyRepository);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder, validator);
    }

    @Test
    void rotatePublicKey_withValidData_returnsPublicKeyResponseDto() {
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
        

        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ROTATED"))).thenReturn(Flux.empty());
        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));

        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModelCopy));

        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(requestDto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", List.of(), "ADMIN"))
                .expectNextMatches(response -> response.getKid() != null && response.getIssuer() != null)
                .verifyComplete();
    }

    @Test
    void rotatePublicKey_withExistingRotatedKey_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setStatus("ROTATED");
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.just(publicKeyModel));
        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");

        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(dto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Public key with status ROTATED already exists."))
                .verify();
    }

    @Test
    void rotatePublicKey_withInvalidRole_throwsApiKeyManagerException() {
        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");
        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(dto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "USER"))
                .expectErrorMatches(throwable -> throwable instanceof PnForbiddenException && throwable.getMessage().contains("Accesso negato!"))
                .verify();
    }
}