
package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private PublicKeyValidator validator;
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        validator = new PublicKeyValidator(publicKeyRepository);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder, validator);
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
                .expectErrorMatches(throwable -> throwable instanceof PnForbiddenException && Objects.requireNonNull(throwable.getMessage()).contains("Accesso negato!"))
                .verify();
    }

    @Test
    void changeStatus_withInvalidStatusTransition_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus("ACTIVE");

        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(publicKeyService.changeStatus("kid", "INACTIVE", "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Invalid state transition"))
                .verify();
    }
}
