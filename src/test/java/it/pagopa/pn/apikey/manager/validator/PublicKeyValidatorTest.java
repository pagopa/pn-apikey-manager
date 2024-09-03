
package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyValidatorTest {

    private PublicKeyValidator validator;

    private PublicKeyRepository publicKeyRepository;

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        validator = new PublicKeyValidator(publicKeyRepository);
    }

   @Test
    void validateChangeStatus_withBlockedToActive_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.BLOCKED.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, PublicKeyStatusDto.ACTIVE.getValue()))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void validateChangeStatus_withBlockedToInvalid_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.BLOCKED.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "INVALID"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains("Invalid state transition"))
                .verify();
    }

    @Test
    void validateChangeStatus_withActiveToBlocked_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, PublicKeyStatusDto.BLOCKED.getValue()))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void validateChangeStatus_withActiveToInvalid_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "INVALID"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains("Invalid state transition"))
                .verify();
    }

    @Test
    void checkPublicKeyAlreadyExistsWithStatus_returnsEmptyWhenKeyDoesNotExist() {
        when(publicKeyRepository.findByCxIdAndStatus("cxId", "ACTIVE")).thenReturn(Flux.empty());

        StepVerifier.create(validator.checkPublicKeyAlreadyExistsWithStatus("cxId", "ACTIVE"))
                .verifyComplete();
    }

    @Test
    void checkPublicKeyAlreadyExistsWithStatus_returnsErrorWhenKeyExists() {
        when(publicKeyRepository.findByCxIdAndStatus("cxId", "ACTIVE")).thenReturn(Mono.just(new PublicKeyModel()).flux());

        StepVerifier.create(validator.checkPublicKeyAlreadyExistsWithStatus("cxId", "ACTIVE"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains("Public key with status ACTIVE already exists."))
                .verify();
    }
}
