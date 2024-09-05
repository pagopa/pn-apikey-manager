package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyServiceTest {

    @Autowired
    private VirtualKeyService virtualKeyService;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private VirtualKeyValidator virtualKeyValidator;

    @Test
    void rotateVirtualKey_withValidData_rotatesKeySuccessfully() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        ApiKeyModel existingApiKey = new ApiKeyModel();
        existingApiKey.setId("existingId");
        existingApiKey.setCxId("cxId");
        existingApiKey.setCxType(CxTypeAuthFleetDto.PG.toString());
        existingApiKey.setUid("uid");
        existingApiKey.setStatus(ApiKeyStatusDto.ENABLED.toString());

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of())));

        when(virtualKeyValidator.validateCxType(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkExistingRotatedKeys(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxId(any(), any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.checkStatus(any()))
                .thenReturn(Mono.just(existingApiKey));

        when(apiKeyRepository.findById("existingId"))
                .thenReturn(Mono.just(existingApiKey));

        when(apiKeyRepository.save(any()))
                .thenReturn(Mono.just(new ApiKeyModel()));

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectComplete()
                .verify();
    }

    @Test
    void rotateVirtualKey_withExistingRotatedKey_throwsConflict() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        ApiKeyModel existingApiKey = new ApiKeyModel();
        existingApiKey.setId("existingId");
        existingApiKey.setCxId("cxId");
        existingApiKey.setStatus(ApiKeyStatusDto.ROTATED.toString());

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of(existingApiKey))));

        when(virtualKeyValidator.validateCxType(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkExistingRotatedKeys(any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("User already has a rotated key", null)));

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void rotateVirtualKey_withMismatchedCxId_throwsBadRequest() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        ApiKeyModel existingApiKey = new ApiKeyModel();
        existingApiKey.setId("existingId");
        existingApiKey.setCxId("differentCxId");
        existingApiKey.setCxType(CxTypeAuthFleetDto.PG.toString());
        existingApiKey.setUid("uid");
        existingApiKey.setStatus(ApiKeyStatusDto.ENABLED.toString());

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of())));

        when(apiKeyRepository.findById("existingId"))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateCxType(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkExistingRotatedKeys(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxId(any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("CxId does not match", HttpStatus.BAD_REQUEST)));

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void rotateVirtualKey_withNonEnabledStatus_throwsBadRequest() {
        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ROTATE);

        ApiKeyModel existingApiKey = new ApiKeyModel();
        existingApiKey.setId("existingId");
        existingApiKey.setCxId("cxId");
        existingApiKey.setCxType(CxTypeAuthFleetDto.PG.toString());
        existingApiKey.setUid("uid");
        existingApiKey.setStatus(ApiKeyStatusDto.BLOCKED.toString()); // Stato diverso da ENABLED

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any()))
                .thenReturn(Mono.just(Page.create(List.of())));

        when(apiKeyRepository.findById("existingId"))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateCxType(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkExistingRotatedKeys(any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxId(any(), any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.checkStatus(any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("virtualKey is not in enabled state", HttpStatus.BAD_REQUEST)));

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }
}