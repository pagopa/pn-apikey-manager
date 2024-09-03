
package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class PublicKeyValidatorTest {

    private PublicKeyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PublicKeyValidator();
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

}
