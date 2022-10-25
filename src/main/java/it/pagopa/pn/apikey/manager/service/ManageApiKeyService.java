package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyConverter apiKeyConverter;

    @Qualifier("apikeyManagerScheduler")
    private final Scheduler scheduler;

    public ManageApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyConverter apiKeyConverter, Scheduler scheduler){
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyConverter = apiKeyConverter;
        this.scheduler = scheduler;
    }

    public Mono<ApiKeyModel> changeStatus(String id, String status, String xPagopaPnUid) {
        return apiKeyRepository.findById(id)
                .doOnNext(apiKeyModels -> log.info("founded ApiKey with id: {}", id))
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel.get(0), status)) {
                        apiKeyModel.get(0).setStatus(decodeStatus(status, false).getValue());
                        apiKeyModel.get(0).getStatusHistory().add(createNewApiKeyHistory(status, xPagopaPnUid));
                        return saveAndCheckIfRotate(apiKeyModel.get(0), status, xPagopaPnUid)
                                .doOnNext(apiKeyModel1 -> log.info("Updated Apikey with id: {} and status: {}", id, status));
                    } else {
                        return Mono.error(new ApiKeyManagerException(INVALID_STATUS, HttpStatus.BAD_REQUEST));
                    }
                })
                .publishOn(scheduler);
    }

    public Mono<String> deleteApiKey(String id) {
        return apiKeyRepository.findById(id)
                .doOnNext(apiKeyModels -> log.info("founded ApiKey for id: {}", id))
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel.get(0), DELETE)) {
                        return apiKeyRepository.delete(apiKeyModel.get(0).getVirtualKey()).doOnNext(s -> log.info("Deleted ApiKey: {}", id));
                    } else {
                        return Mono.error(new ApiKeyManagerException(INVALID_STATUS, HttpStatus.BAD_REQUEST));
                    }
                })
                .publishOn(scheduler);
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey) {
        return apiKeyRepository.getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, limit, lastKey)
                .map(apiKeyConverter::convertResponsetoDto);
    }

    private Mono<ApiKeyModel> saveAndCheckIfRotate(ApiKeyModel apiKeyModel, String status, String xPagopaPnUid) {
        return apiKeyRepository.save(apiKeyModel)
                .doOnNext(apiKeyModel1 -> log.info("saved ApiKey: {}", apiKeyModel1.getId()))
                .flatMap(resp -> {
                    if (status.equalsIgnoreCase(ROTATE)) {
                        return apiKeyRepository.save(constructApiKeyModelForRotate(apiKeyModel, xPagopaPnUid))
                                .doOnNext(apiKeyModel1 -> log.info("Created new Apikey with correlationId: {}", apiKeyModel1.getCorrelationId()));
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
        newApiKeyModel.setLastUpdate(LocalDateTime.now().toString());
        newApiKeyModel.setName(apiKeyModel.getName());
        newApiKeyModel.setUid(xPagopaPnUid);
        newApiKeyModel.setCxId(apiKeyModel.getCxId());
        newApiKeyModel.setCxType(apiKeyModel.getCxType());
        newApiKeyModel.setCorrelationId(apiKeyModel.getCorrelationId());
        newApiKeyModel.getStatusHistory().add(createNewApiKeyHistory(CREATE, xPagopaPnUid));
        return newApiKeyModel;
    }

    protected ApiKeyHistory createNewApiKeyHistory(String status, String pa) {
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setDate(LocalDateTime.now().toString());
        apiKeyHistory.setStatus(decodeStatus(status, true).getValue());
        apiKeyHistory.setChangeByDenomination(pa);
        return apiKeyHistory;
    }


    private boolean isOperationAllowed(ApiKeyModel apiKeyModel, String newStatus) {
        log.info("Verify if status can change from: {}, to: {}", apiKeyModel.getStatus(), newStatus);
        ApiKeyStatusDto status = ApiKeyStatusDto.fromValue(apiKeyModel.getStatus());
        if (newStatus.equals(DELETE))
            return status.equals(BLOCKED) || status.equals(ENABLED);
        if (newStatus.equals(ROTATE))
            return status.equals(ENABLED);
        if (newStatus.equals(BLOCK))
            return status.equals(ENABLED) || status.equals(ROTATED);
        if (newStatus.equals(ENABLE) &&
                apiKeyModel.getStatusHistory().stream().noneMatch(apiKeyHistory -> apiKeyHistory.getStatus().equals(ROTATED.getValue())))
            return status.equals(BLOCKED);
        else
            return false;
    }

    private ApiKeyStatusDto decodeStatus(String body, boolean history) {
        log.debug("Requested operation: {}", body);
        switch (body) {
            case BLOCK:
                return BLOCKED;
            case ENABLE:
                return ApiKeyStatusDto.ENABLED;
            case CREATE:
                if (history)
                    return ApiKeyStatusDto.CREATED;
                else
                    return ApiKeyStatusDto.ENABLED;
            case ROTATE:
                return ApiKeyStatusDto.ROTATED;
            default:
                throw new ApiKeyManagerException(INVALID_STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
