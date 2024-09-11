package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyValidatorTest {

    @Autowired
    private VirtualKeyValidator validator;
    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @MockBean
    private ApiKeyRepository apiKeyRepository;
    @MockBean
    private PublicKeyRepository publicKeyRepository;
    @MockBean
    private PnUserAttributesClient pnUserAttributesClient;
    @MockBean
    private PnExternalRegistriesClient pnExternalRegistriesClient;

    @BeforeEach
    void setUp() {
        validator = new VirtualKeyValidator(apiKeyRepository, publicKeyRepository, pnUserAttributesClient, pnExternalRegistriesClient);
    }

    @Test
    void validateCxType_shouldReturnError_whenCxTypeIsNotPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA; // Use a non-PG value

        Mono<Void> result = validator.validateCxType(cxType);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateCxType_shouldComplete_whenCxTypeIsPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        Mono<Void> result = validator.validateCxType(cxType);

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
    void checkCxId_shouldReturnApiKey_whenCxIdAndUidMatches() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("testCxId");
        apiKeyModel.setUid("testUid");

        Mono<ApiKeyModel> result = validator.checkCxIdAndUid("testCxId", "testUid", apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void checkStatus_shouldReturnError_whenStatusIsNotEnabled() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus("DISABLED");

        Mono<ApiKeyModel> result = validator.validateRotateVirtualKey(apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void checkStatus_shouldReturnApiKey_whenStatusIsEnabled() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus(ApiKeyStatusDto.ENABLED.toString());

        Mono<ApiKeyModel> result = validator.validateRotateVirtualKey(apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void checkExistingRotatedKeys_shouldReturnError_whenRotatedKeyExists() {
        String xPagopaPnUid = "testUid";
        String xPagopaPnCxId = "testCxId";
        ApiKeyModel rotatedKey = new ApiKeyModel();
        rotatedKey.setCxId(xPagopaPnCxId);
        rotatedKey.setUid(xPagopaPnUid);
        rotatedKey.setStatus(ApiKeyStatusDto.ROTATED.toString());

        Page<ApiKeyModel> rotatedKeysPage = Page.create(List.of(rotatedKey));

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString(), ApiKeyModel.Scope.CLIENTID.toString()))
                .thenReturn(Mono.just(rotatedKeysPage));

        Mono<Void> result = validator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void checkExistingRotatedKeys_shouldComplete_whenNoRotatedKeyExists() {
        String xPagopaPnUid = "testUid";
        String xPagopaPnCxId = "testCxId";

        Page<ApiKeyModel> emptyRotatedKeysPage = Page.create(List.of());

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString(), ApiKeyModel.Scope.CLIENTID.toString()))
                .thenReturn(Mono.just(emptyRotatedKeysPage));

        Mono<Void> result = validator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void validateRoleForDeletion_shouldReturnApiKey_whenRoleIsAdminAndCxIdMatches() {
        ApiKeyModel virtualKeyModel = new ApiKeyModel();
        virtualKeyModel.setCxId("testCxId");
        virtualKeyModel.setUid("testUid");

        Mono<ApiKeyModel> result = validator.validateRoleForDeletion(virtualKeyModel, "testUid", "testCxId", "ADMIN", List.of("group1"));

        StepVerifier.create(result)
                .expectNext(virtualKeyModel)
                .verifyComplete();
    }

    @Test
    void validateRoleForDeletion_shouldReturnApiKey_whenUidMatches() {
        ApiKeyModel virtualKeyModel = new ApiKeyModel();
        virtualKeyModel.setCxId("differentCxId");
        virtualKeyModel.setUid("testUid");

        Mono<ApiKeyModel> result = validator.validateRoleForDeletion(virtualKeyModel, "testUid", "testCxId", "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectNext(virtualKeyModel)
                .verifyComplete();
    }

    @Test
    void validateRoleForDeletion_shouldReturnError_whenRoleIsNotAdminAndCxIdOrUidDoesNotMatch() {
        ApiKeyModel virtualKeyModel = new ApiKeyModel();
        virtualKeyModel.setCxId("differentCxId");
        virtualKeyModel.setUid("differentUid");

        Mono<ApiKeyModel> result = validator.validateRoleForDeletion(virtualKeyModel, "testUid", "testCxId", "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void isDeleteOperationAllowed_shouldReturnApiKey_whenStatusIsBlocked() {
        ApiKeyModel virtualKeyModel = new ApiKeyModel();
        virtualKeyModel.setStatus(VirtualKeyStatusDto.BLOCKED.getValue());

        Mono<ApiKeyModel> result = validator.isDeleteOperationAllowed(virtualKeyModel);

        StepVerifier.create(result)
                .expectNext(virtualKeyModel)
                .verifyComplete();
    }

    @Test
    void isDeleteOperationAllowed_shouldReturnError_whenStatusIsNotBlocked() {
        ApiKeyModel virtualKeyModel = new ApiKeyModel();
        virtualKeyModel.setStatus(VirtualKeyStatusDto.ENABLED.getValue());

        Mono<ApiKeyModel> result = validator.isDeleteOperationAllowed(virtualKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void validateTosAndValidPublicKey_shouldReturnError_whenTosConsentNotFound() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(new ConsentDto().accepted(false)));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of())));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateTosAndValidPublicKey_shouldReturnError_whenPublicKeyNotFound() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(anyString(), anyString()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(anyString(), anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of())));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateTosAndValidPublicKey_shouldComplete_whenTosConsentAndPublicKeyAreValid() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(anyString(), anyString()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(anyString(), anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(Mono.just(new ConsentDto().accepted(true)));
        PublicKeyModel activeKey = new PublicKeyModel();
        activeKey.setStatus("ACTIVE");
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of(activeKey))));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
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
}
