package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class ApiKeyService {

    private static final String BLOCK = "BLOCK";
    private static final String ENABLE = "ENABLE";
    private static final String ROTATE = "ROTATE";
    private static final String CREATE = "CREATE";


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

        return paService.searchAggregationId(xPagopaPnCxId)
                .flatMap(s -> {
                    if (s != null) {
                        requestNewApiKeyDto.setGroups(groupToAdd);
                        ApiKeyModel apiKeyModel = constructApiKeyModel(requestNewApiKeyDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                        return checkIfApikeyExists(s, apiKeyModel, xPagopaPnCxId);
                    } else {
                        return Mono.error(new ApiKeyManagerException("PA is not associated with an aggregation"));
                    }
                });
    }

    private Mono<ResponseNewApiKeyDto> checkIfApikeyExists(String s, ApiKeyModel apiKeyModel, String xPagopaPnCxId) {
        return aggregationService.searchAwsApiKey(s)
                .flatMap(apiKeyAggregation -> {
                    if (apiKeyAggregation == null) {
                        return aggregationService.createNewAwsApiKey(xPagopaPnCxId)
                                .flatMap(s1 -> aggregationService.createNewAggregation(s1)
                                        .flatMap(apiKeyAggregation1 -> apiKeyRepository.save(apiKeyModel)
                                                .map(this::createResponseNewApiKey)));
                    }
                    return apiKeyRepository.save(apiKeyModel).map(this::createResponseNewApiKey);
                });
    }

    public Mono<String> deleteApiKey(String id) {
        return apiKeyRepository.delete(id);
    }

    public Mono<ApiKeyModel> changeStatus(String id, String status, String xPagopaPnUid) {
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> {
                    apiKeyModel.setStatus(decodeStatus(status).getValue());
                    apiKeyModel.getStatusHistory().add(createNewApiKeyHistory(status, xPagopaPnUid));
                    return saveAndCheckIfRotate(apiKeyModel, status, xPagopaPnUid);
                });
    }

    private Mono<ApiKeyModel> saveAndCheckIfRotate(ApiKeyModel apiKeyModel, String status, String xPagopaPnUid) {
        return apiKeyRepository.save(apiKeyModel)
                .flatMap(resp -> {
                    if (status.equalsIgnoreCase(ROTATE)) {
                        return apiKeyRepository.save(constructApiKeyModelForRotate(apiKeyModel, xPagopaPnUid));
                    }
                    return Mono.just(resp);
                });
    }

    private List<String> checkGroups(List<String> groups, List<String> xPagopaPnCxGroups) {
        List<String> groupsToAdd = new ArrayList<>();
        if (!groups.isEmpty() && xPagopaPnCxGroups.containsAll(groups)) {
            groupsToAdd.addAll(groups);
            return groupsToAdd;
        } else if (groups.isEmpty() && !xPagopaPnCxGroups.isEmpty()) {
            groupsToAdd.addAll(xPagopaPnCxGroups);
            return groupsToAdd;
        } else if (groups.isEmpty()) {
            return groupsToAdd;
        }
        throw new ApiKeyManagerException("User cannot add groups: " + groups.iterator().next());
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
        apiKeyModel.setStatus(ApiKeyStatusDto.CREATED.getValue());
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
        newApiKeyModel.setStatus(ApiKeyStatusDto.CREATED.getValue());
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
        apiKeyHistory.setStatus(decodeStatus(status).getValue());
        apiKeyHistory.setChangeByDenomination(pa);
        return apiKeyHistory;
    }

    private ApiKeyStatusDto decodeStatus(String body) {
        switch (body) {
            case BLOCK:
                return ApiKeyStatusDto.BLOCKED;
            case ENABLE:
                return ApiKeyStatusDto.ENABLED;
            case ROTATE:
                return ApiKeyStatusDto.ROTATED;
            case CREATE:
                return ApiKeyStatusDto.CREATED;
            default:
                throw new ApiKeyManagerException("Invalid status change");
        }
    }
}
