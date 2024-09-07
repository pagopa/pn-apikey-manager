package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.VirtualKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;

@Service
@lombok.CustomLog
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final VirtualKeyValidator virtualKeyValidator;

    public Mono<Void> changeStatusVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, List<String> xPagopaPnCxGroups, String id, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
    log.info("Starting changeStatusVirtualKeys - id={}, xPagopaPnUid={}, xPagopaPnCxType={}, xPagopaPnCxId={}, xPagopaPnCxRole={}, status={}",
            id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto.getStatus());
    return virtualKeyValidator.validateCxType(xPagopaPnCxType)
            .then(Mono.defer(() -> switch (requestVirtualKeyStatusDto.getStatus()) {
                case ENABLE, BLOCK -> {
                    log.info("Processing ENABLE or BLOCK status for id={}", id);
                    yield reactivateOrBlockVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto);
                }
                case ROTATE -> {
                    log.info("Processing ROTATE status for id={}", id);
                    yield rotateVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                }
            }));
}

    private Mono<Void> rotateVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        log.info("Starting rotate of virtualKey - id={}, xPagopaPnUid={}", id, xPagopaPnUid);
        return virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString())
                .then(Mono.defer(() -> {
                    log.info("Finding virtualKey by id={}", id);
                    return apiKeyRepository.findById(id);
                }))
                .flatMap(apiKey -> virtualKeyValidator.checkCxIdAndUid(xPagopaPnCxId,xPagopaPnUid,apiKey))
                .flatMap(virtualKeyValidator::validateRotateVirtualKey)
                .flatMap(apiKey -> {
                    log.info("Rotating virtualKey - id={}, xPagopaPnUid={}", apiKey.getId(), xPagopaPnUid);
                    return createAndSaveNewApiKey(apiKey, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(updatedApiKey -> updateExistingApiKey(apiKey, xPagopaPnUid));
                })
                .doOnSuccess(a -> log.info("Successfully changed status of virtualKey - id={}", id))
                .doOnError(throwable -> log.error("Error changing status of virtualKey - id={}, error={}", id, throwable.getMessage()))
                .then();
    }


    private Mono<ApiKeyModel> updateExistingApiKey(ApiKeyModel apiKey, String xPagopaPnUid) {
        log.info("Updating existing ApiKey - id={}", apiKey.getId());
        apiKey.setStatus(ApiKeyStatusDto.ROTATED.toString());
        ArrayList<ApiKeyHistoryModel> history = new ArrayList<>(apiKey.getStatusHistory());
        history.add(createApiKeyHistory(ApiKeyStatusDto.ROTATED.toString(), xPagopaPnUid));
        apiKey.setStatusHistory(history);
        return apiKeyRepository.save(apiKey);
    }

    private Mono<ApiKeyModel> createAndSaveNewApiKey(ApiKeyModel existingApiKey, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        log.info("Creating and saving new ApiKey - correlationId={}", existingApiKey.getId());
        ApiKeyModel newApiKey = createNewApiKey(existingApiKey, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
        return apiKeyRepository.save(newApiKey);
    }

    private String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    private ApiKeyModel createNewApiKey(ApiKeyModel existingApiKey, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        ApiKeyModel newApiKey = new ApiKeyModel();
        newApiKey.setId(generateUUID());
        newApiKey.setCorrelationId(existingApiKey.getId());
        newApiKey.setGroups(existingApiKey.getGroups());
        newApiKey.setLastUpdate(LocalDateTime.now());
        newApiKey.setName(existingApiKey.getName());
        newApiKey.setPdnd(false);
        newApiKey.setStatus(ApiKeyStatusDto.ENABLED.toString());
        newApiKey.setStatusHistory(List.of(createApiKeyHistory(ApiKeyStatusDto.CREATED.toString(), xPagopaPnUid)));
        newApiKey.setVirtualKey(generateUUID());
        newApiKey.setCxId(xPagopaPnCxId);
        newApiKey.setCxType(xPagopaPnCxType.toString());
        newApiKey.setUid(xPagopaPnUid);
        newApiKey.setScope(ApiKeyModel.Scope.CLIENTID);
        return newApiKey;
    }

    private ApiKeyHistoryModel createApiKeyHistory(String status, String xPagopaPnUid) {
        ApiKeyHistoryModel history = new ApiKeyHistoryModel();
        history.setStatus(status);
        history.setDate(LocalDateTime.now());
        history.setChangeByDenomination(xPagopaPnUid);
        return history;
    }

    private Mono<Void> reactivateOrBlockVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        return null;
    }

    public Mono<String> deleteVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {

        if (!VirtualKeyConstant.ALLOWED_CX_TYPE_VIRTUAL_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }

        return apiKeyRepository.findById(id)
                .flatMap(virtualKeyModel -> this.validateRoleForDeletion(virtualKeyModel, xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups))
                .flatMap(this::isDeleteOperationAllowed)
                .map(virtualKeyModel -> this.updateVirtualKeyStatusToDelete(virtualKeyModel, xPagopaPnUid))
                .flatMap(apiKeyRepository::save)
                .thenReturn("VirtualKey deleted");
    }

    private ApiKeyModel updateVirtualKeyStatusToDelete(ApiKeyModel virtualKeyModel, String xPagopaPnUid) {
        virtualKeyModel.setStatus(VirtualKeyStatusDto.DELETED.getValue());
        virtualKeyModel.getStatusHistory().add(createNewHistory(xPagopaPnUid, VirtualKeyStatusDto.DELETED.getValue()));
        return virtualKeyModel;
    }

    @NotNull
    private static ApiKeyHistoryModel createNewHistory(String xPagopaPnUid, String status) {
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setStatus(status);
        apiKeyHistoryModel.setChangeByDenomination(xPagopaPnUid);
        return apiKeyHistoryModel;
    }

    private Mono<ApiKeyModel> validateRoleForDeletion(ApiKeyModel virtualKeyModel, String xPagopaPnUid, String xPagopaCxId,String xPagopaPnCxRole, List<String> xPagopaPnCxGroups) {
        log.debug("validateRoleForDeletion - xPagopaPnUid: {}, xPagopaPnCxRole: {}, xPagopaPnCxGroups: {}", xPagopaPnUid, xPagopaPnCxRole, xPagopaPnCxGroups);
        return VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(isAdmin -> {
                    if((isAdmin && virtualKeyModel.getCxId().equals(xPagopaCxId)) || virtualKeyModel.getUid().equals(xPagopaPnUid)) {
                        return Mono.just(virtualKeyModel);
                    }
                    return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_DELETE, HttpStatus.FORBIDDEN));
                });
    }

    private Mono<ApiKeyModel> isDeleteOperationAllowed(ApiKeyModel virtualKeyModel) {
        VirtualKeyStatusDto status = VirtualKeyStatusDto.fromValue(virtualKeyModel.getStatus());
        if (!status.getValue().equals(VirtualKeyStatusDto.BLOCKED.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, virtualKeyModel.getStatus()), HttpStatus.CONFLICT));
        }
        return Mono.just(virtualKeyModel);
    }
}
