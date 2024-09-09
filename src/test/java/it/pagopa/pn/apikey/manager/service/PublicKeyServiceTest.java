package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private PublicKeyValidator validator;
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = Mockito.mock(PublicKeyRepository.class);
        validator = new PublicKeyValidator(publicKeyRepository);
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
    void deletePublicKey_Conflict() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setStatusHistory(List.of());
        publicKeyModel.setStatus("DELETED");

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void deletePublicKey_CxTypeNotAllowed() {
        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PA, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @ParameterizedTest
    @CsvSource({
            "BLOCKED, ENABLE",
            "ACTIVE, BLOCK"
    })
    void changeStatus_updatesStatusSuccessfully(String apiKeyStatus, String statusToUpdate) {
        PublicKeyModel publicKeyModel = mockPublicKeyModel(apiKeyStatus);


        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(publicKeyService.changeStatus("kid", statusToUpdate, "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "ADMIN"))
                .verifyComplete();
    }

    @NotNull
    private static PublicKeyModel mockPublicKeyModel(String apiKeyStatus) {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setName("Test Key");
        publicKeyModel.setCorrelationId("correlationId");
        publicKeyModel.setPublicKey("publicKeyData");
        publicKeyModel.setStatus(apiKeyStatus);
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatusHistory(new ArrayList<>());
        return publicKeyModel;
    }

    @Test
    void changeStatus_withInvalidRole_throwsForbiddenException() {
        StepVerifier.create(publicKeyService.changeStatus("kid", "ENABLE", "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "USER"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && Objects.requireNonNull(throwable.getMessage()).contains(ApiKeyManagerExceptionError.ACCESS_DENIED))
                .verify();
    }

    @Test
    void changeStatus_withInvalidStatusTransition_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus("ACTIVE");

        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(publicKeyService.changeStatus("kid", "INACTIVE", "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_STATE_TRANSITION))
                .verify();
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
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_EXISTS_ACTIVE))
                .verify();
    }

    @Test
    void createPublicKey_withInvalidRole_throwsApiKeyManagerException() {
        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(new PublicKeyRequestDto()), List.of(), "invalidRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.ACCESS_DENIED))
                .verify();
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