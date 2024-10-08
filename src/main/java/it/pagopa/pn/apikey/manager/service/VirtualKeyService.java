package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PgUserDetailDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.converter.VirtualKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils.decodeToEntityStatus;

@Service
@lombok.CustomLog
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final VirtualKeyValidator virtualKeyValidator;
    private final VirtualKeyConverter virtualKeyConverter;
    private final PnExternalRegistriesClient pnExternalRegistriesClient;

    public Mono<Void> changeStatusVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, List<String> xPagopaPnCxGroups, String id, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        log.info("Starting changeStatusVirtualKeys - id={}, xPagopaPnUid={}, xPagopaPnCxType={}, xPagopaPnCxId={}, xPagopaPnCxRole={}, status={}",
                id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto.getStatus());
        return virtualKeyValidator.validateCxType(xPagopaPnCxType)
                .then(virtualKeyValidator.validateTosAndValidPublicKey(xPagopaPnCxId, xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups))
                .then(Mono.defer(() -> switch (requestVirtualKeyStatusDto.getStatus()) {
                    case ENABLE, BLOCK -> {
                        log.info("Processing ENABLE or BLOCK status for id={}", id);
                        yield reactivateOrBlockVirtualKey(id, xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto, xPagopaPnCxGroups);
                    }
                    case ROTATE -> {
                        log.info("Processing ROTATE status for id={}", id);
                        yield rotateVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                    }
                }));
    }

    private Mono<Void> reactivateOrBlockVirtualKey(String id, String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto, List<String> xPagopaPnCxGroups) {
        return virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, decodeToEntityStatus(requestVirtualKeyStatusDto.getStatus()))
                .then(Mono.defer(() -> apiKeyRepository.findById(id))
                        .flatMap(apiKeyModel -> VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                                .flatMap(isAdmin -> checkUserPermission(xPagopaPnUid, xPagopaPnCxId, apiKeyModel, isAdmin, requestVirtualKeyStatusDto.getStatus()))
                                .flatMap(apiKey -> virtualKeyValidator.validateStateTransition(apiKey, requestVirtualKeyStatusDto)
                                        .then(Mono.defer(() -> updateApiKeyStatus(apiKey, xPagopaPnUid, decodeToEntityStatus(requestVirtualKeyStatusDto.getStatus()))))
                                )
                        )
                ).then();
    }

    private Mono<ApiKeyModel> checkUserPermission(String xPagopaPnUid, String xPagopaPnCxId, ApiKeyModel apiKeyModel, Boolean isAdmin, RequestVirtualKeyStatusDto.StatusEnum statusEnum) {
        if (Boolean.TRUE.equals(isAdmin)) {
            return virtualKeyValidator.checkCxId(xPagopaPnCxId, apiKeyModel);
        } else {
            return virtualKeyValidator.checkCxIdAndUid(xPagopaPnCxId, xPagopaPnUid, apiKeyModel, statusEnum);
        }
    }

    private Mono<Void> rotateVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        log.info("Starting rotate of virtualKey - id={}, xPagopaPnUid={}", id, xPagopaPnUid);
        return virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, VirtualKeyStatusDto.ROTATED.toString())
                .then(Mono.defer(() -> {
                    log.info("Finding virtualKey by id={}", id);
                    return apiKeyRepository.findById(id);
                }))
                .flatMap(apiKey -> virtualKeyValidator.checkCxIdAndUid(xPagopaPnCxId, xPagopaPnUid, apiKey, RequestVirtualKeyStatusDto.StatusEnum.ROTATE))
                .flatMap(virtualKeyValidator::validateRotateVirtualKey)
                .flatMap(apiKey -> {
                    log.info("Rotating virtualKey - id={}, xPagopaPnUid={}", apiKey.getId(), xPagopaPnUid);
                    return createAndSaveNewApiKey(apiKey, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId)
                            .flatMap(newActiveKey -> updateApiKeyStatus(apiKey, xPagopaPnUid, ApiKeyStatusDto.ROTATED.toString()));
                })
                .doOnSuccess(a -> log.info("Successfully changed status of virtualKey - id={}", id))
                .doOnError(throwable -> log.error("Error changing status of virtualKey - id={}, error={}", id, throwable.getMessage()))
                .then();
    }

    private Mono<ApiKeyModel> updateApiKeyStatus(ApiKeyModel apiKey, String xPagopaPnUid, String status) {
        log.info("updateApiKeyStatus - id={} xPagoPaPnUid={} status={}", apiKey.getId(), xPagopaPnUid, status);
        apiKey.setStatus(status);
        apiKey.getStatusHistory().add(createApiKeyHistory(status, xPagopaPnUid));
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

    public Mono<String> deleteVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        return virtualKeyValidator.validateCxType(xPagopaPnCxType)
                .then(Mono.defer(() -> virtualKeyValidator.validateTosAndValidPublicKey(xPagopaPnCxId, xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)))
                .then(Mono.defer(() -> apiKeyRepository.findById(id)))
                .flatMap(virtualKeyModel -> virtualKeyValidator.validateRoleForDeletion(virtualKeyModel, xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups))
                .flatMap(virtualKeyValidator::isDeleteOperationAllowed)
                .flatMap(virtualKeyModel -> this.updateApiKeyStatus(virtualKeyModel, xPagopaPnUid, VirtualKeyStatusDto.DELETED.getValue()))
                .flatMap(apiKeyRepository::save)
                .thenReturn("VirtualKey deleted");
    }

    public Mono<VirtualKeysResponseDto> getVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole,
                                                       Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey) {
        ApiKeyPageable pageable = toApiKeyPageable(limit, lastKey, lastUpdate);
        return virtualKeyValidator.validateCxType(xPagopaPnCxType)
                .then(VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups))
                .flatMap(admin -> {
                    log.debug("admin: {}", admin);
                    Mono<Page<ApiKeyModel>> page = apiKeyRepository.getVirtualKeys(xPagopaPnUid, xPagopaPnCxId, new ArrayList<>(), pageable, admin);
                    if (admin) {
                        return page.flatMap(apiKeyModelPage -> {
                            List<String> internalIds = apiKeyModelPage.items().stream()
                                    .map(ApiKeyModel::getUid)
                                    .toList();
                            String institutionId = internalIdWithoutPrefix(xPagopaPnCxId);
                            return Flux.fromIterable(internalIds).flatMap(uid -> pnExternalRegistriesClient.getPgUsersDetailsPrivate(uid, institutionId))
                                    .collectMap(PgUserDetailDto::getId, pgUserDetailDto -> pgUserDetailDto)
                                    .flatMap(mapPgUserDetail -> convertToDtoAndSetTotal(xPagopaPnUid, xPagopaPnCxId, showVirtualKey, apiKeyModelPage, mapPgUserDetail, admin));
                        });
                    } else {
                        return page.flatMap(apiKeyModelPage -> convertToDtoAndSetTotal(xPagopaPnUid, xPagopaPnCxId, showVirtualKey, apiKeyModelPage, null, admin));
                    }
                });
    }

    private String internalIdWithoutPrefix(String internalId) {
        internalId  = internalId.replace("PF-","").replace("PG-","");
        return internalId;
    }

    private Mono<VirtualKeysResponseDto> convertToDtoAndSetTotal(String xPagopaPnUid, String xPagopaPnCxId, Boolean showVirtualKey, Page<ApiKeyModel> apiKeyModelPage,
                                                                 Map<String, PgUserDetailDto> mapPgUserDetail, Boolean admin) {
        VirtualKeysResponseDto virtualKeysResponseDto = virtualKeyConverter.convertResponseToDto(apiKeyModelPage, mapPgUserDetail, showVirtualKey);
        return apiKeyRepository.countWithFilters(xPagopaPnUid, xPagopaPnCxId, admin).map(integer -> {
            virtualKeysResponseDto.setTotal(integer);
            return virtualKeysResponseDto;
        });
    }

    private ApiKeyPageable toApiKeyPageable(Integer limit, String lastKey, String lastUpdate) {
        return ApiKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastKey)
                .lastEvaluatedLastUpdate(lastUpdate)
                .build();
    }

    public Mono<ResponseNewVirtualKeyDto> createVirtualKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<RequestNewVirtualKeyDto> requestNewVirtualKeyDto, String role, List<String> groups) {
        return virtualKeyValidator.validateCxType(xPagopaPnCxType)
                .then(Mono.defer(() -> virtualKeyValidator.validateTosAndValidPublicKey(xPagopaPnCxId, xPagopaPnCxType, role, groups)))
                .then(Mono.defer(() -> virtualKeyValidator.checkVirtualKeyAlreadyExistsWithStatus(xPagopaPnUid, xPagopaPnCxId, VirtualKeyStatusDto.ENABLED.getValue())))
                .then(requestNewVirtualKeyDto)
                .flatMap(dto -> createVirtualKeyModel(dto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId))
                .flatMap(apiKeyRepository::save)
                .flatMap(this::createVirtualKeyDto);
    }

    private Mono<ApiKeyModel> createVirtualKeyModel(RequestNewVirtualKeyDto dto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        ApiKeyModel model = new ApiKeyModel();
        model.setId(UUID.randomUUID().toString());
        model.setCorrelationId("");
        model.setGroups(new ArrayList<>());
        model.setLastUpdate(LocalDateTime.now());
        model.setName(dto.getName());
        model.setPdnd(false);
        model.setStatus(VirtualKeyStatusDto.ENABLED.getValue());
        ApiKeyHistoryModel historyModel = new ApiKeyHistoryModel();
        historyModel.setChangeByDenomination(xPagopaPnUid);
        historyModel.setDate(LocalDateTime.now());
        historyModel.setStatus(VirtualKeyStatusDto.CREATED.getValue());
        model.setStatusHistory(List.of(historyModel));
        model.setVirtualKey(UUID.randomUUID().toString());
        model.setCxId(xPagopaPnCxId);
        model.setCxType(xPagopaPnCxType.getValue());
        model.setUid(xPagopaPnUid);
        model.setScope(ApiKeyModel.Scope.CLIENTID);
        return Mono.just(model);
    }

    private Mono<ResponseNewVirtualKeyDto> createVirtualKeyDto(ApiKeyModel apiKeyModel) {
        ResponseNewVirtualKeyDto response = new ResponseNewVirtualKeyDto();
        response.setId(apiKeyModel.getId());
        response.setVirtualKey(apiKeyModel.getVirtualKey());
        return Mono.just(response);
    }

}
