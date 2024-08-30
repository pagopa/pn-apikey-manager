package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
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
    void validatePublicKeyRequest_withValidName_returnsMonoOfRequest() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("validName");

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectNext(requestDto)
                .verifyComplete();
    }

    @Test
    void validatePublicKeyRequest_withNullPublicKey_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName(null);

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("Name is mandatory") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validatePublicKeyRequest_withEmptyPublicKey_throwsApiKeyManagerException() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("");

        StepVerifier.create(validator.validatePublicKeyRequest(requestDto))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("Name is mandatory") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }
}