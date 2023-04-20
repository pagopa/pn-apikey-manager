package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.converter.ApiKeyBoConverter;
import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponseApiKeysDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponsePdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.model.PaGroup;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;
import static it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto.*;

@Service
@Slf4j
public class ManageApiKeyService {

    private static final String BLOCK = "BLOCK";
    private static final String ENABLE = "ENABLE";
    private static final String ROTATE = "ROTATE";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";

    private static final String CX_TYPE_NOT_ALLOWED = "CxTypeAuthFleet {} not allowed";

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyConverter apiKeyConverter;
    private final ApiKeyBoConverter apiKeyBoConverter;
    private final ExternalRegistriesClient externalRegistriesClient;


    public ManageApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyConverter apiKeyConverter, ApiKeyBoConverter apiKeyBoConverter, ExternalRegistriesClient externalRegistriesClient) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyConverter = apiKeyConverter;
        this.apiKeyBoConverter = apiKeyBoConverter;
        this.externalRegistriesClient = externalRegistriesClient;
    }

    public Mono<ResponsePdndDto> changePdnd(List<ApiPdndDto> apiPdndDtos) {
        return Flux.fromIterable(apiPdndDtos)
                .flatMap(apiPdndDto -> apiKeyRepository.changePdnd(apiPdndDto.getId(), apiPdndDto.getPdnd())
                        .onErrorResume(throwable -> {
                            log.warn("can not update pdnd flag of key {} to {}", apiPdndDto.getId(), apiPdndDto.getPdnd(), throwable);
                            return Mono.empty();
                        })
                        .map(apiKeyModel -> apiPdndDto))
                .collectList()
                .map(apiPdndChanged -> apiKeyBoConverter.convertToResponsePdnd(apiPdndDtos, apiPdndChanged));
    }

    public Mono<List<ApiKeyModel>> changeVirtualKey(String xPagopaPnCxId, String virtualKey) {
        return apiKeyRepository.findByCxId(xPagopaPnCxId)
                .map(apiKeyModels -> {
                    if (apiKeyModels.isEmpty()) {
                        throw new ApiKeyManagerException("ApiKey does not exist", HttpStatus.NOT_FOUND);
                    }
                    if (apiKeyModels.size() > 1) {
                        throw new ApiKeyManagerException("Already exist a virtual key associated for this cxId", HttpStatus.BAD_REQUEST);
                    }
                    return apiKeyModels;
                })
                .flatMap(apiKeyModels -> apiKeyRepository.setNewVirtualKey(apiKeyModels, virtualKey))
                .doOnNext(apiKeyModels -> log.info("Setted new virtual key:{} at api key with xPagopaPnCxId: {}", virtualKey, xPagopaPnCxId));
    }

    public Mono<ApiKeyModel> changeStatus(String id, RequestApiKeyStatusDto requestApiKeyStatusDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType) {
        String status = requestApiKeyStatusDto.getStatus().getValue();

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.warn(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel, status)) {
                        ApiKeyModel oldApiKey = new ApiKeyModel(apiKeyModel);
                        apiKeyModel.setStatus(decodeStatus(status, false).getValue());
                        apiKeyModel.setLastUpdate(LocalDateTime.now());
                        apiKeyModel.getStatusHistory().add(createNewApiKeyHistory(status, xPagopaPnUid));
                        return saveAndCheckIfRotate(apiKeyModel, oldApiKey, status, xPagopaPnUid)
                                .doOnNext(apiKeyModel1 -> log.info("Updated Apikey with id: {} and status: {}", id, status));
                    } else {
                        return Mono.error(new ApiKeyManagerException(String.format(APIKEY_INVALID_STATUS, apiKeyModel.getStatus(), status), HttpStatus.CONFLICT));
                    }
                });
    }

    public Mono<String> deleteApiKey(String id, CxTypeAuthFleetDto xPagopaPnCxType) {

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.warn(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel, DELETE)) {
                        return apiKeyRepository.delete(id);
                    } else {
                        return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, apiKeyModel.getStatus()), HttpStatus.CONFLICT));
                    }
                });
    }

    public Mono<ResponseApiKeysDto> getBoApiKeyList(@NonNull String xPagopaPnCxId) {
        return apiKeyRepository.findByCxIdAndStatusRotateAndEnabled(xPagopaPnCxId)
                .zipWith(externalRegistriesClient.getPaGroupsById(xPagopaPnCxId, null))
                .map(this::decodeGroupIdsToGroupNames)
                .map(apiKeyModelPage -> apiKeyBoConverter.convertResponseToDto(apiKeyModelPage.items()));
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(@NonNull String xPagopaPnCxId,
                                                  @Nullable List<String> xPagopaPnCxGroups,
                                                  @Nullable Integer limit,
                                                  @Nullable String lastEvaluatedKey,
                                                  @Nullable String lastEvaluatedLastUpdate,
                                                  @Nullable Boolean showVirtualKey,
                                                  @NonNull CxTypeAuthFleetDto xPagopaPnCxType) {

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.warn(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        ApiKeyPageable pageable = toApiKeyPageable(limit, lastEvaluatedKey, lastEvaluatedLastUpdate);
        return apiKeyRepository.getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, pageable)
                .zipWith(externalRegistriesClient.getPaGroupsById(xPagopaPnCxId, null))
                .map(this::decodeGroupIdsToGroupNames)
                .map(apiKeyModelPage -> apiKeyConverter.convertResponseToDto(apiKeyModelPage, showVirtualKey))
                .zipWhen(page -> apiKeyRepository.countWithFilters(xPagopaPnCxId, xPagopaPnCxGroups))
                .doOnNext(tuple -> tuple.getT1().setTotal(tuple.getT2()))
                .map(Tuple2::getT1);
    }

    private Page<ApiKeyModel> decodeGroupIdsToGroupNames(Tuple2<Page<ApiKeyModel>, List<PaGroup>> tuple) {
        Page<ApiKeyModel> apiKeysFound = tuple.getT1();
        List<PaGroup> paGroups = tuple.getT2();

        if (paGroups.isEmpty() || apiKeysFound.items().isEmpty()) {
            return apiKeysFound;
        }

        log.info("Start decode group names of {} api keys", apiKeysFound.items().size());

        Map<String, String> mapGroupsIdToName = convertPaGroupsToMap(paGroups);

        apiKeysFound.items().forEach(
                apiKeyModel -> apiKeyModel.setGroups(getGroupNamesByGroupIds(apiKeyModel.getGroups(), mapGroupsIdToName))
        );

        log.info("End decode group names");
        return apiKeysFound;
    }

    private Map<String, String> convertPaGroupsToMap(List<PaGroup> paGroups) {
        return paGroups.stream().collect(Collectors.toMap(PaGroup::getId, PaGroup::getName));
    }

    private List<String> getGroupNamesByGroupIds(List<String> groups, Map<String, String> mapGroupsIdToName) {
        return groups.stream()
                .map(group -> mapGroupsIdToName.getOrDefault(group, group))
                .toList();
    }

    private Mono<ApiKeyModel> saveAndCheckIfRotate(ApiKeyModel apiKeyModel, ApiKeyModel oldApiKey, String status, String xPagopaPnUid) {
        return apiKeyRepository.save(apiKeyModel)
                .flatMap(resp -> {
                    if (status.equalsIgnoreCase(ROTATE)) {
                        return apiKeyRepository.save(constructApiKeyModelForRotate(apiKeyModel, xPagopaPnUid))
                                .doOnNext(newApiKey -> log.info("ApiKey {} rotated with correlationId: {}", apiKeyModel.getId(), apiKeyModel.getCorrelationId()))
                                .onErrorResume(e -> apiKeyRepository.save(oldApiKey)
                                        .doOnNext(ak -> log.info("rollback ApiKey {} done", ak.getId()))
                                        .then(Mono.error(e)));
                    }
                    return Mono.just(resp);
                });
    }

    private ApiKeyModel constructApiKeyModelForRotate(ApiKeyModel apiKeyModel, String xPagopaPnUid) {
        ApiKeyModel newApiKeyModel = new ApiKeyModel();
        newApiKeyModel.setId(UUID.randomUUID().toString());
        newApiKeyModel.setVirtualKey(UUID.randomUUID().toString());
        newApiKeyModel.setStatus(ApiKeyStatusDto.ENABLED.getValue());
        newApiKeyModel.setGroups(apiKeyModel.getGroups());
        newApiKeyModel.setLastUpdate(LocalDateTime.now());
        newApiKeyModel.setName(apiKeyModel.getName());
        newApiKeyModel.setUid(xPagopaPnUid);
        newApiKeyModel.setCxId(apiKeyModel.getCxId());
        newApiKeyModel.setCxType(apiKeyModel.getCxType());
        newApiKeyModel.setCorrelationId(apiKeyModel.getCorrelationId());
        newApiKeyModel.getStatusHistory().add(createNewApiKeyHistory(CREATE, xPagopaPnUid));
        return newApiKeyModel;
    }

    protected ApiKeyHistoryModel createNewApiKeyHistory(String status, String pa) {
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setStatus(decodeStatus(status, true).getValue());
        apiKeyHistoryModel.setChangeByDenomination(pa);
        return apiKeyHistoryModel;
    }

    private boolean isOperationAllowed(ApiKeyModel apiKeyModel, String newStatus) {
        log.info("Verify if status can change from: {}, to: {}", apiKeyModel.getStatus(), newStatus);
        ApiKeyStatusDto status = ApiKeyStatusDto.fromValue(apiKeyModel.getStatus());
        if (newStatus.equals(DELETE)) {
            return status.equals(BLOCKED);
        }
        if (newStatus.equals(ROTATE)) {
            return status.equals(ENABLED);
        }
        if (newStatus.equals(BLOCK)) {
            return status.equals(ENABLED) || status.equals(ROTATED);
        }
        if (newStatus.equals(ENABLE) &&
                apiKeyModel.getStatusHistory().stream().noneMatch(apiKeyHistoryModel -> apiKeyHistoryModel.getStatus().equals(ROTATED.getValue()))) {
            return status.equals(BLOCKED);
        } else {
            return false;
        }
    }

    private ApiKeyStatusDto decodeStatus(String status, boolean history) {
        log.debug("Requested operation: {}", status);
        return switch (status) {
            case BLOCK -> ApiKeyStatusDto.BLOCKED;
            case ENABLE -> ApiKeyStatusDto.ENABLED;
            case CREATE -> {
                if (history) {
                    yield ApiKeyStatusDto.CREATED;
                } else {
                    yield ApiKeyStatusDto.ENABLED;
                }
            }
            case ROTATE -> ApiKeyStatusDto.ROTATED;
            default -> throw new ApiKeyManagerException(APIKEY_INVALID_STATUS, HttpStatus.BAD_REQUEST);
        };
    }

    private ApiKeyPageable toApiKeyPageable(Integer limit, String lastEvaluatedLastKey, String lastEvaluatedLastUpdate) {
        return ApiKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastEvaluatedLastKey)
                .lastEvaluatedLastUpdate(lastEvaluatedLastUpdate)
                .build();
    }
}
