package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

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
    void validatePublicKeyRequest_withValidNameAndPublicKey_returnsMonoOfRequest() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("validName");
        requestDto.setPublicKey("validPublicKey");

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectNext(requestDto)
                .verifyComplete();
    }

    @Test
    void validatePublicKeyRequest_withNullName_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName(null);

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.PUBLIC_KEY_NAME_MANDATORY) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePublicKeyRequest_withEmptyName_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("");

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.PUBLIC_KEY_NAME_MANDATORY) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePublicKeyRequest_withNullPublicKey_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("validName");
        requestDto.setPublicKey(null);

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.PUBLIC_KEY_MANDATORY) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePublicKeyRequest_withEmptyPublicKey_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("validName");
        requestDto.setPublicKey("");

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.PUBLIC_KEY_MANDATORY) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

   @Test
    void validateChangeStatus_withBlockedToActive_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.BLOCKED.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "ENABLE"))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void validateChangeStatus_withBlockedToInvalid_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.BLOCKED.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "INVALID"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_STATE_TRANSITION))
                .verify();
    }

    @Test
    void validateChangeStatus_withActiveToBlocked_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "BLOCK"))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void validateChangeStatus_withActiveToInvalid_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validateChangeStatus(publicKeyModel, "INVALID"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_STATE_TRANSITION))
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
                        throwable.getMessage().contains(String.format(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_EXISTS, "ACTIVE")))
                .verify();
    }

    @Test
    void validateDeletePublicKey_withBlockedStatus_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.BLOCKED.getValue());

        StepVerifier.create(validator.validateDeletePublicKey(publicKeyModel))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void validateDeletePublicKey_withNonBlockedStatus_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validateDeletePublicKey(publicKeyModel))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.PUBLIC_KEY_CAN_NOT_DELETE) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void validatePayload_withValidPayload_returnsPayload() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .kid("validKid")
                .cxId("validCxId")
                .action("DELETE")
                .build();

        StepVerifier.create(validator.validatePayload(payload))
                .expectNext(payload)
                .verifyComplete();
    }

    @Test
    void validatePayload_withEmptyKid_throwsApiKeyManagerException() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .kid("")
                .cxId("validCxId")
                .action("DELETE")
                .build();

        StepVerifier.create(validator.validatePayload(payload))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.TTL_PAYLOAD_INVALID_KID_CXID) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePayload_withEmptyCxId_throwsApiKeyManagerException() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .kid("validKid")
                .cxId("")
                .action("DELETE")
                .build();

        StepVerifier.create(validator.validatePayload(payload))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.TTL_PAYLOAD_INVALID_KID_CXID) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePayload_withInvalidAction_throwsApiKeyManagerException() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action("INVALID")
                .cxId("validCxId")
                .kid("validKid")
                .build();

        StepVerifier.create(validator.validatePayload(payload))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals(ApiKeyManagerExceptionError.TTL_PAYLOAD_INVALID_ACTION) &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void checkItemExpiration_withExpiredItem_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().minusSeconds(60));

        StepVerifier.create(validator.checkItemExpiration(publicKeyModel))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void checkItemExpiration_withNotExpiredItem_returnsEmpty() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().plusSeconds(60));

        StepVerifier.create(validator.checkItemExpiration(publicKeyModel))
                .verifyComplete();
    }

    @Test
    void checkIfItemIsNotAlreadyDeleted_withDeletedItem_returnsEmpty() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.DELETED.getValue());

        StepVerifier.create(validator.checkIfItemIsNotAlreadyDeleted(publicKeyModel))
                .verifyComplete();
    }

    @Test
    void checkIfItemIsNotAlreadyDeleted_withNotDeletedItem_returnsPublicKeyModel() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.checkIfItemIsNotAlreadyDeleted(publicKeyModel))
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
        "ACTIVE, true",
        "BLOCKED, false",
        "DELETED, false"
    })
    void validatePublicKeyRotation_handlesVariousStatuses(String status, boolean shouldPass) {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setPublicKey("oldPublicKey");
        publicKeyModel.setStatus(status);

        if (shouldPass) {
            StepVerifier.create(validator.validatePublicKeyRotation(publicKeyModel, "newPublicKey"))
                    .expectNext(publicKeyModel)
                    .verifyComplete();
        } else {
            StepVerifier.create(validator.validatePublicKeyRotation(publicKeyModel, "newPublicKey"))
                    .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                            ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                    .verify();
        }
    }

    @Test
    void validatePublicKeyRotation_withSamePublicKey_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setPublicKey("oldPublicKey");
        publicKeyModel.setStatus(PublicKeyStatusDto.ACTIVE.getValue());

        StepVerifier.create(validator.validatePublicKeyRotation(publicKeyModel, "oldPublicKey"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_USED))
                .verify();
    }
}