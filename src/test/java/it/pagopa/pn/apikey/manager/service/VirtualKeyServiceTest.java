package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.PnDataVaultClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.TOS_CONSENT_NOT_FOUND;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.VALID_PUBLIC_KEY_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class VirtualKeyServiceTest {

    @Autowired
    private VirtualKeyService virtualKeyService;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private PublicKeyRepository publicKeyRepository;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @MockBean
    private VirtualKeyValidator virtualKeyValidator;

    @MockBean
    private PnDataVaultClient pnDataVaultClient;

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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(), any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateRotateVirtualKey(any()))
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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any()))
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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(),any(),any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(),any()))
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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(),any(),any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(),any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateRotateVirtualKey(any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("virtualKey is not in enabled state", HttpStatus.CONFLICT)));

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void deleteVirtualKey_InternalErrorTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setCxGroup(new ArrayList<>());
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("xPagopaPnUid");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);
        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", new ArrayList<>(), "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Internal error"));
    }

    @Test
    void deleteVirtualKey_validateRoleErrorTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setGroups(new ArrayList<>());
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("otherUser");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);

        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.validateRoleForDeletion(any(), any(), any(), any(), any())).thenReturn(Mono.error(new ApiKeyManagerException("Forbidden operation", HttpStatus.FORBIDDEN)));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", new ArrayList<>(), "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException)
                .verify();
    }

    @Test
    void deleteVirtualKey_CxTypeNotAllowedTest() {
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PA, "xPagopaPnCxId", new ArrayList<>(), "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException);
    }

    @Test
    void deleteVirtualKey_SuccessTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setCxGroup(new ArrayList<>());
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("xPagopaPnUid");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);

        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.validateRoleForDeletion(any(), any(), any(), any(), any())).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.isDeleteOperationAllowed(any())).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", new ArrayList<>(), "xPagopaPnCxRole"))
                .expectNextMatches(response -> response.equals("VirtualKey deleted"))
                .verifyComplete();
    }

    @Test
    void getVirtualKeys_whenCxTypeNotAllowed_shouldReturnError() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of("group1");
        String xPagopaPnCxRole = "ADMIN";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "lastUpdate";
        Boolean showPublicKey = true;

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void getVirtualKeys_whenValidInput_adminTrue() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "ADMIN";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "2024-09-04T16:25:58.334862500";
        Boolean showPublicKey = true;

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(getApiKeyModel());
        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        BaseRecipientDtoDto baseRecipientDto = new BaseRecipientDtoDto();
        baseRecipientDto.setInternalId("internalId");
        baseRecipientDto.setTaxId("taxId");
        baseRecipientDto.setDenomination("denomination");

        VirtualKeysResponseDto responseDto = new VirtualKeysResponseDto();
        VirtualKeyDto virtualKeyDto = getVirtualKeyDto();
        responseDto.setItems(List.of(virtualKeyDto));
        responseDto.setLastKey(null);
        responseDto.setLastUpdate(null);
        responseDto.setTotal(1);

        when(apiKeyRepository.getVirtualKeys(any(), any(), any(), any(), anyBoolean())).thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(any(), any(), anyBoolean())).thenReturn(Mono.just(1));
        when(pnDataVaultClient.getRecipientDenominationByInternalId(any())).thenReturn(Flux.just(baseRecipientDto));

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }

    private ApiKeyModel getApiKeyModel() {
        ApiKeyModel apiKey = new ApiKeyModel();
        apiKey.setVirtualKey("virtualKey");
        apiKey.setStatus("ENABLED");
        apiKey.setId("id");
        apiKey.setCxId("cxId");
        apiKey.setName("name");
        apiKey.setStatusHistory(new ArrayList<>());
        apiKey.setCxType("PG");
        apiKey.setUid("internalId");
        return apiKey;
    }

    private VirtualKeyDto getVirtualKeyDto() {
        VirtualKeyDto virtualKeyDto = new VirtualKeyDto();
        UserDtoDto userDto = new UserDtoDto();
        userDto.setFiscalCode("taxId");
        userDto.setDenomination("denomination");
        virtualKeyDto.setValue("virtualKey");
        virtualKeyDto.setStatus(VirtualKeyStatusDto.ENABLED);
        virtualKeyDto.setId("id");
        virtualKeyDto.setName("name");
        virtualKeyDto.setUser(userDto);
        return virtualKeyDto;
    }

    @Test
    void getVirtualKeys_whenValidInput_adminFalse() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "operator";
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "2024-09-04T16:25:58.334862500";
        Boolean showPublicKey = true;

        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        apiKeyModels.add(getApiKeyModel());
        Page<ApiKeyModel> page = Page.create(apiKeyModels);

        VirtualKeysResponseDto responseDto = new VirtualKeysResponseDto();
        VirtualKeyDto virtualKeyDto = getVirtualKeyDto();
        virtualKeyDto.setUser(null);
        responseDto.setItems(List.of(virtualKeyDto));
        responseDto.setLastKey(null);
        responseDto.setLastUpdate(null);
        responseDto.setTotal(1);

        when(apiKeyRepository.getVirtualKeys(any(), any(), any(), any(), anyBoolean())).thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(any(), any(), anyBoolean())).thenReturn(Mono.just(1));

        Mono<VirtualKeysResponseDto> result = virtualKeyService.getVirtualKeys(xPagopaPnUid, cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, lastUpdate, showPublicKey);

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }

    @Test
    void createVirtualKey_Success() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        requestDto.setName("Test Key");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("virtualKey");
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus("ACTIVE");

        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(Page.create(List.of(publicKeyModel))));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .expectNextMatches(response -> response.getId().equals("id") && response.getVirtualKey().equals("virtualKey"))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_CxTypeNotAllowed() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PA, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("CxTypeAuthFleet PA not allowed"));
    }

    @Test
    void createVirtualKey_TosConsentNotFound() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(Page.create(List.of())));
        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(false)));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(TOS_CONSENT_NOT_FOUND));
    }

    @Test
    void createVirtualKey_InternalError() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(Page.create(List.of())));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().equals(VALID_PUBLIC_KEY_NOT_FOUND));
    }

    @Test
    void createVirtualKey_virtualKeyAlreadyExists() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setUid("uid");
        apiKeyModel.setCxId("cxId");
        apiKeyModel.setScope(ApiKeyModel.Scope.CLIENTID);
        apiKeyModel.setStatus(VirtualKeyStatusDto.ENABLED.getValue());

        Page<ApiKeyModel> page = Page.create(List.of(apiKeyModel));

        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.just(page));
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(Page.create(List.of())));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().equals(VALID_PUBLIC_KEY_NOT_FOUND));
    }
}