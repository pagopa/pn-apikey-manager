package it.pagopa.pn.apikey.manager.service;

import com.amazonaws.util.StringUtils;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.model.CreateApiKeyResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CreateApiKeyService {

    private static final String CREATE = "CREATE";

    private final ApiKeyRepository apiKeyRepository;
    private final AggregationService aggregationService;
    private final PaService paService;
    private final ManageApiKeyService manageApiKeyService;

    public CreateApiKeyService(ApiKeyRepository apiKeyRepository, AggregationService aggregationService, PaService paService, ManageApiKeyService manageApiKeyService) {
        this.apiKeyRepository = apiKeyRepository;
        this.aggregationService = aggregationService;
        this.paService = paService;
        this.manageApiKeyService = manageApiKeyService;
    }

    public Mono<ResponseNewApiKeyDto> createApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                   RequestNewApiKeyDto requestNewApiKeyDto, List<String> xPagopaPnCxGroups) {

        List<String> groupToAdd = checkGroups(requestNewApiKeyDto.getGroups(), xPagopaPnCxGroups);
        log.debug("list groupsToAdd size: {}, items: {}", groupToAdd.size(), groupToAdd.iterator().next());
        return paService.searchAggregationId(xPagopaPnCxId)
                .switchIfEmpty(createNewApiKey(xPagopaPnCxId,null))
                .doOnNext(s -> log.info("founded Pa AggregationId: {}", s))
                .flatMap(s -> {
                    requestNewApiKeyDto.setGroups(groupToAdd);
                    ApiKeyModel apiKeyModel = constructApiKeyModel(requestNewApiKeyDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                    return checkIfApikeyExists(s, apiKeyModel, xPagopaPnCxId);
                });
    }

    private Mono<String> createNewApiKey(String xPagopaPnCxId, ApiKeyAggregation apiKeyAggregation) {
        return aggregationService.createNewAwsApiKey(xPagopaPnCxId)
                .flatMap(createApiKeyResponse -> {
                    if(apiKeyAggregation!=null)
                        return aggregationService.addAwsApiKeyToAggregate(createApiKeyResponse, apiKeyAggregation)
                                .doOnNext(s1 -> log.info("Updated aggregate: {} with AWS apiKey",s1));
                    else
                        return createNewAggregate(createApiKeyResponse, xPagopaPnCxId);
                });
    }

    private Mono<String> createNewAggregate(CreateApiKeyResponse createApiKeyResponse, String xPagopaPnCxId) {
        return aggregationService.createNewAggregate(createApiKeyResponse)
                .doOnNext(apiKeyAggregation -> log.info("Created new Aggregate: {}",apiKeyAggregation.getAggregateId()))
                .flatMap(apiKeyAggregation -> paService.createNewPaAggregation(constructPaAggregationModel(apiKeyAggregation.getAggregateId(), xPagopaPnCxId))
                        .doOnNext(paAggregation -> log.info("created new PaAggregation: {}", paAggregation))
                        .map(PaAggregation::getAggregationId));
    }

    private Mono<ResponseNewApiKeyDto> checkIfApikeyExists(String s, ApiKeyModel apiKeyModel, String xPagopaPnCxId) {
        return aggregationService.getApiKeyAggregation(s)
                .doOnNext(next -> log.info("Founded aggregate: {}", s))
                .flatMap(apiKeyAggregation -> {
                    if (StringUtils.isNullOrEmpty(apiKeyAggregation.getApiKeyId())) {
                        return createNewApiKey(xPagopaPnCxId,apiKeyAggregation);
                    }
                    return Mono.just(apiKeyAggregation.getAggregateId());
                })
                .flatMap(resp -> apiKeyRepository.save(apiKeyModel)
                        .doOnNext(apiKeyModel1 -> log.info("created new apiKey with id: {}", apiKeyModel1.getId()))
                        .map(this::createResponseNewApiKey));
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
        log.debug("createResponse for new ApiKey: {}", responseNewApiKeyDto);
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
        apiKeyModel.setCorrelationId(UUID.randomUUID().toString());
        apiKeyModel.setCxId(xPagopaPnCxId);
        apiKeyModel.setCxType(xPagopaPnCxType.getValue());
        apiKeyModel.getStatusHistory().add(manageApiKeyService.createNewApiKeyHistory(CREATE, xPagopaPnUid));
        log.debug("constructed apiKeyModel: {}", apiKeyModel);
        return apiKeyModel;
    }

    private PaAggregation constructPaAggregationModel(String aggregateId, String paId) {
        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId(aggregateId);
        paAggregation.setPaId(paId);
        return paAggregation;
    }
}
