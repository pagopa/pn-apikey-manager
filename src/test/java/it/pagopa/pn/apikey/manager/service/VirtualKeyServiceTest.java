package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PgUserDetailDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.constant.RoleConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.TOS_CONSENT_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private PnExternalRegistriesClient pnExternalRegistriesClient;

    @MockBean
    private PnUserAttributesClient pnUserAttributesClient;

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

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(), any(), any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateRotateVirtualKey(any()))
                .thenReturn(Mono.just(existingApiKey));

        when(apiKeyRepository.findById("existingId"))
                .thenReturn(Mono.just(existingApiKey));

        when(apiKeyRepository.save(any()))
                .thenReturn(Mono.just(new ApiKeyModel()));

        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

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

        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(), any(), any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("CxId does not match", HttpStatus.BAD_REQUEST)));

        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

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

        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any()))
                .thenReturn(Mono.empty());

        when(virtualKeyValidator.checkCxIdAndUid(any(), any(), any(), any()))
                .thenReturn(Mono.just(existingApiKey));

        when(virtualKeyValidator.validateRotateVirtualKey(any()))
                .thenReturn(Mono.error(new ApiKeyManagerException("virtualKey is not in enabled state", HttpStatus.CONFLICT)));

        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyService.changeStatusVirtualKeys("uid", CxTypeAuthFleetDto.PG, "cxId", "role", List.of(), "existingId", requestDto))
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testChangeStatusVirtualKeys_Enable() {

        String id = "keyId";
        String xPagopaPnUid = "userUid";
        String xPagopaPnCxId = "cxId";
        String xPagopaPnCxRole = RoleConstant.ADMIN_ROLE;
        List<String> xPagopaPnCxGroups = List.of("group1", "group2");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setUid(xPagopaPnUid);

        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ENABLE);

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.findById(id)).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.checkCxIdAndUid(anyString(), anyString(), any(), any())).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.validateStateTransition(any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any(ApiKeyModel.class))).thenReturn(Mono.just(new ApiKeyModel()));

        Mono<Void> result = virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups, id, requestDto);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyRepository, times(1)).findById(id);
        verify(apiKeyRepository, times(1)).save(any(ApiKeyModel.class));
        verify(virtualKeyValidator, times(1)).checkVirtualKeyAlreadyExistsWithStatus(anyString(), anyString(), anyString());
    }

    @Test
    void testChangeStatusVirtualKeys_Block() {

        String id = "keyId";
        String xPagopaPnUid = "userUid";
        String xPagopaPnCxId = "cxId";
        String xPagopaPnCxRole = RoleConstant.ADMIN_ROLE;
        List<String> xPagopaPnCxGroups = List.of("group1", "group2");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setUid(xPagopaPnUid);

        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.BLOCK);

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.findById(id)).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.checkCxIdAndUid(anyString(), anyString(), any(), any())).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.validateStateTransition(any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any(ApiKeyModel.class))).thenReturn(Mono.just(new ApiKeyModel()));

        Mono<Void> result = virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups, id, requestDto);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyRepository, times(1)).findById(id);
        verify(apiKeyRepository, times(1)).save(any(ApiKeyModel.class));
        verify(virtualKeyValidator, times(1)).checkVirtualKeyAlreadyExistsWithStatus(anyString(), anyString(), anyString());
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
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
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

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.validateRoleForDeletion(any(), any(), any(), any(), any())).thenReturn(Mono.error(new ApiKeyManagerException("Forbidden operation", HttpStatus.FORBIDDEN)));
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", new ArrayList<>(), "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException)
                .verify();
    }

    @Test
    void deleteVirtualKey_CxTypeNotAllowedTest() {
        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, CxTypeAuthFleetDto.PA.getValue()), HttpStatus.FORBIDDEN)));

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

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.validateRoleForDeletion(any(), any(), any(), any(), any())).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.isDeleteOperationAllowed(any())).thenReturn(Mono.just(apiKeyModel));
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());

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
        String xPagopaPnCxRole = RoleConstant.ADMIN_ROLE;
        Integer limit = 10;
        String lastKey = "lastKey";
        String lastUpdate = "lastUpdate";
        Boolean showPublicKey = true;
        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, cxType.getValue()), HttpStatus.FORBIDDEN)));
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

        PgUserDetailDto pgUserDetailDto = new PgUserDetailDto();
        pgUserDetailDto.setId("internalId");
        pgUserDetailDto.setTaxCode("taxId");
        pgUserDetailDto.setName("name");
        pgUserDetailDto.setSurname("surname");

        VirtualKeysResponseDto responseDto = new VirtualKeysResponseDto();
        VirtualKeyDto virtualKeyDto = getVirtualKeyDto();
        responseDto.setItems(List.of(virtualKeyDto));
        responseDto.setLastKey(null);
        responseDto.setLastUpdate(null);
        responseDto.setTotal(1);

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(apiKeyRepository.getVirtualKeys(any(), any(), any(), any(), anyBoolean())).thenReturn(Mono.just(page));
        when(apiKeyRepository.countWithFilters(any(), any(), anyBoolean())).thenReturn(Mono.just(1));
        when(pnExternalRegistriesClient.getPgUsersDetailsPrivate(any(), any())).thenReturn(Mono.just(pgUserDetailDto));

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
        userDto.setDenomination("name surname");
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

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
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

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any())).thenReturn(Mono.empty());
        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(Page.create(List.of(publicKeyModel))));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .expectNextMatches(response -> response.getId().equals("id") && response.getVirtualKey().equals("virtualKey"))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_CxTypeNotAllowed() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        when(virtualKeyValidator.validateCxType(CxTypeAuthFleetDto.PA)).thenReturn(Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, CxTypeAuthFleetDto.PA.getValue()), HttpStatus.FORBIDDEN)));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PA, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("CxTypeAuthFleet PA not allowed"));
    }

    @Test
    void createVirtualKey_TosConsentNotFound() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.error(new ApiKeyManagerException(TOS_CONSENT_NOT_FOUND, HttpStatus.FORBIDDEN)));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(TOS_CONSENT_NOT_FOUND));
    }

    @Test
    void createVirtualKey_InternalError() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof RuntimeException);
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

        when(virtualKeyValidator.validateCxType(any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateTosAndValidPublicKey(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(any(), any(), any())).thenReturn(Mono.error(new ApiKeyManagerException("Virtual key already exists", HttpStatus.CONFLICT)));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT);
    }
}