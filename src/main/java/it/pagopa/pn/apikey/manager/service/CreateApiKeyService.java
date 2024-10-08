package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.constant.PaAggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.model.PaGroup;
import it.pagopa.pn.apikey.manager.model.PaGroupStatus;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.constant.ProcessStatus.CHECKING_NAME_API_KEY_NEW_API_KEY;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Service
@lombok.CustomLog
public class CreateApiKeyService {

    private static final String CREATE = "CREATE";

    private final ApiKeyRepository apiKeyRepository;
    private final AggregationService aggregationService;
    private final PaAggregationsService paAggregationsService;
    private final ManageApiKeyService manageApiKeyService;
    private final ExternalRegistriesClient externalRegistriesClient;
    private final String flagPdnd;

    public CreateApiKeyService(ApiKeyRepository apiKeyRepository,
                               AggregationService aggregationService,
                               PaAggregationsService paAggregationsService,
                               ManageApiKeyService manageApiKeyService,
                               ExternalRegistriesClient externalRegistriesClient,
                               @Value("${pn.apikey.manager.flag.pdnd}") String flagPdnd) {
        this.apiKeyRepository = apiKeyRepository;
        this.aggregationService = aggregationService;
        this.paAggregationsService = paAggregationsService;
        this.manageApiKeyService = manageApiKeyService;
        this.externalRegistriesClient = externalRegistriesClient;
        this.flagPdnd = flagPdnd;
    }

    public Mono<ResponseNewApiKeyDto> createApiKey(@NonNull String xPagopaPnUid,
                                                   @NonNull CxTypeAuthFleetDto xPagopaPnCxType,
                                                   @NonNull String xPagopaPnCxId,
                                                   @NonNull Mono<RequestNewApiKeyDto> requestNewApiKey,
                                                   @Nullable List<String> xPagopaPnCxGroups) {
        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return requestNewApiKey.flatMap(requestNewApiKeyDto -> checkGroupsToAdd(requestNewApiKeyDto.getGroups(), xPagopaPnCxGroups, xPagopaPnCxId)
                .flatMap(groupToAdd -> {
                    log.debug("list groupsToAdd size: {}", groupToAdd.size());
                    return paAggregationsService.searchAggregationId(xPagopaPnCxId)
                            .switchIfEmpty(Mono.defer(() -> createNewAggregate(xPagopaPnCxId)))
                            .doOnNext(aggregateId -> log.info("Add PA {} to aggregate {}", xPagopaPnCxId, aggregateId))
                            .flatMap(aggregateId -> {
                                requestNewApiKeyDto.setGroups(groupToAdd);
                                ApiKeyModel apiKeyModel = constructApiKeyModel(requestNewApiKeyDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                                return apiKeyRepository.save(apiKeyModel)
                                        .map(this::createResponseNewApiKey);
                            });
                }));

    }

    private Mono<String> createNewAggregate(String xPagopaPnCxId) {
        return getPaById(xPagopaPnCxId).flatMap(this::createNewAggregate);
    }

    private Mono<String> createNewAggregate(InternalPaDetailDto internalPaDetailDto) {
        return aggregationService.createNewAggregate(internalPaDetailDto)
                .flatMap(aggregateId -> paAggregationsService.createNewPaAggregation(constructPaAggregationModel(aggregateId, internalPaDetailDto))
                        .onErrorResume(e -> aggregationService.deleteAggregate(aggregateId)
                                .doOnSuccess(a -> log.info("rollback aggregate {} done", aggregateId))
                                .doOnError(ea -> log.warn("can not rollback aggregate {}", aggregateId))
                                .then(Mono.error(e)))
                        .map(PaAggregationModel::getAggregateId));
    }

    private Mono<List<String>> checkGroupsToAdd(List<String> requestGroups, List<String> xPagopaPnCxGroups, String cxId) {
        log.logChecking(CHECKING_NAME_API_KEY_NEW_API_KEY);

        boolean isUserAdmin = xPagopaPnCxGroups == null || xPagopaPnCxGroups.isEmpty();

        if(requestGroups.isEmpty()) {
            log.logCheckingOutcome(CHECKING_NAME_API_KEY_NEW_API_KEY,true);
            return Mono.just(isUserAdmin ? requestGroups : xPagopaPnCxGroups);
        }

        Mono<List<String>> groupsToCheck = isUserAdmin ? getPaGroupsById(cxId) : Mono.just(xPagopaPnCxGroups);

        return groupsToCheck.map(groups -> {
           if(!new HashSet<>(groups).containsAll(requestGroups)) {
                requestGroups.removeIf(groups::contains);
               log.logCheckingOutcome(CHECKING_NAME_API_KEY_NEW_API_KEY, false, "User cannot add groups: " + requestGroups);
               throw new ApiKeyManagerException("User cannot add groups: " + requestGroups, HttpStatus.BAD_REQUEST);
            }
            log.logCheckingOutcome(CHECKING_NAME_API_KEY_NEW_API_KEY,true);
            return requestGroups;
        });
    }

    private Mono<List<String>> getPaGroupsById(String cxId) {
        return this.externalRegistriesClient.getPaGroupsById(cxId, PaGroupStatus.ACTIVE)
                .defaultIfEmpty(new ArrayList<>())
                .map(paGroups -> paGroups.stream().map(PaGroup::getId).toList());
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
        apiKeyModel.setLastUpdate(LocalDateTime.now());
        apiKeyModel.setName(requestNewApiKeyDto.getName());
        apiKeyModel.setUid(xPagopaPnUid);
        apiKeyModel.setCorrelationId(UUID.randomUUID().toString());
        apiKeyModel.setCxId(xPagopaPnCxId);
        apiKeyModel.setCxType(xPagopaPnCxType.getValue());
        apiKeyModel.getStatusHistory().add(manageApiKeyService.createNewApiKeyHistory(CREATE, xPagopaPnUid));
        apiKeyModel.setPdnd(Boolean.parseBoolean(flagPdnd));
        apiKeyModel.setScope(ApiKeyModel.Scope.APIKEY);
        log.debug("constructed apiKeyModel: {}", apiKeyModel.getId());
        return apiKeyModel;
    }

    private Mono<InternalPaDetailDto> getPaById(String paId) {
        return externalRegistriesClient.getPaById(paId);
    }

    private PaAggregationModel constructPaAggregationModel(String aggregateId, InternalPaDetailDto internalPaDetailDto) {
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId(aggregateId);
        paAggregationModel.setPaId(internalPaDetailDto.getId());
        paAggregationModel.setPaName(internalPaDetailDto.getName());
        paAggregationModel.setPageable(PaAggregationConstant.PAGEABLE_VALUE);
        return paAggregationModel;
    }
}
