package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;
import static it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto.*;


@Service
@Slf4j
public class ApiKeyService {

    private static final String BLOCK = "BLOCK";
    private static final String ENABLE = "ENABLE";
    private static final String ROTATE = "ROTATE";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";


    private final ApiKeyRepository apiKeyRepository;
    private final AggregationService aggregationService;
    private final PaService paService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, AggregationService aggregationService, PaService paService) {
        this.apiKeyRepository = apiKeyRepository;
        this.aggregationService = aggregationService;
        this.paService = paService;
    }

    public Mono<ResponseNewApiKeyDto> createApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                   RequestNewApiKeyDto requestNewApiKeyDto, List<String> xPagopaPnCxGroups) {

        List<String> groupToAdd = checkGroups(requestNewApiKeyDto.getGroups(), xPagopaPnCxGroups);
        log.debug("list groupsToAdd size: {}", groupToAdd.size());
        return paService.searchAggregationId(xPagopaPnCxId)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(PA_AGGREGATION_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR)))
                .flatMap(s -> {
                        requestNewApiKeyDto.setGroups(groupToAdd);
                        ApiKeyModel apiKeyModel = constructApiKeyModel(requestNewApiKeyDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                        return checkIfApikeyExists(s, apiKeyModel, xPagopaPnCxId);
                    });
    }

    public Mono<ApiKeyModel> changeStatus(String id, String status, String xPagopaPnUid) {
        return apiKeyRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(KEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR)))
                .flatMap(apiKeyModel -> {
                    if (apiKeyModel.size() == 1) {
                        if (isOperationAllowed(apiKeyModel.get(0), status)) {
                            apiKeyModel.get(0).setStatus(decodeStatus(status, false).getValue());
                            apiKeyModel.get(0).getStatusHistory().add(createNewApiKeyHistory(status, xPagopaPnUid));
                            return saveAndCheckIfRotate(apiKeyModel.get(0), status, xPagopaPnUid)
                                    .doOnNext(apiKeyModel1 -> log.info("Updated Apikey with id: {} and status: {}", id, status));
                        } else {
                            return Mono.error(new ApiKeyManagerException(INVALID_STATUS, HttpStatus.BAD_REQUEST));
                        }
                    }
                    return Mono.error(new ApiKeyManagerException(KEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    public Mono<String> deleteApiKey(String id) {
        return apiKeyRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(KEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR)))
                .flatMap(apiKeyModel -> {
                    if (apiKeyModel.size() == 1) {
                        if (isOperationAllowed(apiKeyModel.get(0), DELETE)) {
                            return apiKeyRepository.delete(apiKeyModel.get(0).getVirtualKey()).doOnNext(s -> log.info("Deleted api key with id: {}", id));
                        } else {
                            return Mono.error(new ApiKeyManagerException(INVALID_STATUS, HttpStatus.BAD_REQUEST));
                        }
                    }
                    return Mono.error(new ApiKeyManagerException(KEY_DOES_NOT_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    private boolean isOperationAllowed(ApiKeyModel apiKeyModel, String newStatus) {
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

    private Mono<ResponseNewApiKeyDto> checkIfApikeyExists(String s, ApiKeyModel apiKeyModel, String xPagopaPnCxId) {
        return aggregationService.searchAwsApiKey(s)
                .doOnNext(next -> log.info("Founded realApiKey for aggregate: {}", next.getAggregateId()))
                .switchIfEmpty(aggregationService.createNewAwsApiKey(xPagopaPnCxId)
                        .flatMap(aggregationService::createNewAggregation))
                .flatMap(apiKeyAggregation -> apiKeyRepository.save(apiKeyModel)
                        .map(this::createResponseNewApiKey));
    }

    private Mono<ApiKeyModel> saveAndCheckIfRotate(ApiKeyModel apiKeyModel, String status, String xPagopaPnUid) {
        return apiKeyRepository.save(apiKeyModel)
                .flatMap(resp -> {
                    if (status.equalsIgnoreCase(ROTATE)) {
                        return apiKeyRepository.save(constructApiKeyModelForRotate(apiKeyModel, xPagopaPnUid))
                                .doOnNext(apiKeyModel1 -> log.info("Created new Apikey with correlationId: {}", apiKeyModel1.getCorrelationId()));
                    }
                    return Mono.just(resp);
                });
    }

    private List<String> checkGroups(List<String> groups, List<String> xPagopaPnCxGroups) {
        List<String> groupsToAdd = new ArrayList<>();
        if (!groups.isEmpty() && (xPagopaPnCxGroups.containsAll(groups) || xPagopaPnCxGroups.isEmpty())) {
            groupsToAdd.addAll(groups);
            return groupsToAdd;
        } else if (groups.isEmpty() && !xPagopaPnCxGroups.isEmpty()) {
            groupsToAdd.addAll(xPagopaPnCxGroups);
            return groupsToAdd;
        } else if (groups.isEmpty()) {
            return groupsToAdd;
        }
        throw new ApiKeyManagerException("User cannot add groups: " + groups.iterator().next(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseNewApiKeyDto createResponseNewApiKey(ApiKeyModel apiKeyModel) {
        ResponseNewApiKeyDto responseNewApiKeyDto = new ResponseNewApiKeyDto();
        responseNewApiKeyDto.setApiKey(apiKeyModel.getVirtualKey());
        responseNewApiKeyDto.setId(apiKeyModel.getId());
        return responseNewApiKeyDto;
    }

    private ApiKeyModel constructApiKeyModel(RequestNewApiKeyDto requestNewApiKeyDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId(UUID.randomUUID().toString());
        apiKeyModel.setVirtualKey(UUID.randomUUID().toString());
        apiKeyModel.setStatus(ApiKeyStatusDto.ENABLED.getValue());
        apiKeyModel.setGroups(requestNewApiKeyDto.getGroups());
        apiKeyModel.setLastUpdate(LocalDateTime.now().toString());
        apiKeyModel.setName(requestNewApiKeyDto.getName());
        apiKeyModel.setUid(xPagopaPnUid);
        apiKeyModel.setCxId(xPagopaPnCxId);
        apiKeyModel.setCxType(xPagopaPnCxType.getValue());
        apiKeyModel.getStatusHistory().add(createNewApiKeyHistory(CREATE, xPagopaPnUid));
        return apiKeyModel;
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
        newApiKeyModel.setCorrelationId(apiKeyModel.getId());
        newApiKeyModel.getStatusHistory().add(createNewApiKeyHistory(CREATE, xPagopaPnUid));
        return newApiKeyModel;
    }

    private ApiKeyHistory createNewApiKeyHistory(String status, String pa) {
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setDate(LocalDateTime.now().toString());
        apiKeyHistory.setStatus(decodeStatus(status, true).getValue());
        apiKeyHistory.setChangeByDenomination(pa);
        return apiKeyHistory;
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
