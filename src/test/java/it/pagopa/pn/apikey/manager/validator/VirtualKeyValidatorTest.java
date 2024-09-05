package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyValidatorTest {

    @Autowired
    private VirtualKeyValidator validator;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @BeforeEach
    void setUp() {
        validator = new VirtualKeyValidator(apiKeyRepository);
    }

    @Test
    void validateCxType_shouldReturnError_whenCxTypeIsNotPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA; // Use a non-PG value

        Mono<Void> result = VirtualKeyValidator.validateCxType(cxType);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateCxType_shouldComplete_whenCxTypeIsPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        Mono<Void> result = VirtualKeyValidator.validateCxType(cxType);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void checkCxIdAndUid_shouldReturnError_whenCxIdAndUidDoesNotMatch() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("differentCxId");
        apiKeyModel.setUid("differentUid");

        Mono<ApiKeyModel> result = validator.checkCxIdAndUid("testCxId", "testUid", apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void checkCxIdAndUid_shouldReturnApiKey_whenCxIdAndUidMatches() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("testCxId");
        apiKeyModel.setUid("testUid");

        Mono<ApiKeyModel> result = validator.checkCxIdAndUid("testCxId", "testUid", apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void checkCxId_shouldReturnError_whenCxIdDoesNotMatch() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("differentCxId");

        Mono<ApiKeyModel> result = validator.checkCxId("testCxId", apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void checkCxId_shouldReturnApiKey_whenCxIdMatches() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("testCxId");

        Mono<ApiKeyModel> result = validator.checkCxId("testCxId", apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void validateStateTransition_shouldReturnError_whenInvalidTransition() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus(ApiKeyStatusDto.BLOCKED.toString());
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        Mono<Void> result = validator.validateStateTransition(apiKeyModel, requestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void validateStateTransition_shouldComplete_whenValidTransition() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus(ApiKeyStatusDto.BLOCKED.toString());
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ENABLE);

        Mono<Void> result = validator.validateStateTransition(apiKeyModel, requestDto);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void validateNoOtherKeyWithSameStatus_shouldReturnError_whenKeyWithSameStatusExists() {
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of(new ApiKeyModel()), null)));

        Mono<Void> result = validator.validateNoOtherKeyWithSameStatus("uid", "cxId", "status");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void validateNoOtherKeyWithSameStatus_shouldComplete_whenNoKeyWithSameStatusExists() {
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of(), null)));

        Mono<Void> result = validator.validateNoOtherKeyWithSameStatus("uid", "cxId", "status");

        StepVerifier.create(result)
                .verifyComplete();
    }
}