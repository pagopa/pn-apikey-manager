package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Service
@Slf4j
public class CreateApiKeyService {

    private static final String CREATE = "CREATE";

    private final ApiKeyRepository apiKeyRepository;
    private final AggregationService aggregationService;
    private final PaAggregationsService paAggregationsService;
    private final ManageApiKeyService manageApiKeyService;
    private final ExternalRegistriesClient externalRegistriesClient;
    private final PnAuditLogBuilder auditLogBuilder;


    public CreateApiKeyService(ApiKeyRepository apiKeyRepository,
                               AggregationService aggregationService,
                               PaAggregationsService paAggregationsService,
                               ManageApiKeyService manageApiKeyService,
                               ExternalRegistriesClient externalRegistriesClient,
                               PnAuditLogBuilder auditLogBuilder) {
        this.apiKeyRepository = apiKeyRepository;
        this.aggregationService = aggregationService;
        this.paAggregationsService = paAggregationsService;
        this.manageApiKeyService = manageApiKeyService;
        this.externalRegistriesClient = externalRegistriesClient;
        this.auditLogBuilder = auditLogBuilder;
    }

    public Mono<ResponseNewApiKeyDto> createApiKey(@NonNull String xPagopaPnUid,
                                                   @NonNull CxTypeAuthFleetDto xPagopaPnCxType,
                                                   @NonNull String xPagopaPnCxId,
                                                   @NonNull RequestNewApiKeyDto requestNewApiKeyDto,
                                                   @Nullable List<String> xPagopaPnCxGroups) {
        String logMessage = String.format("Creazione di una API Key - xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s", xPagopaPnUid, xPagopaPnCxType.getValue(), xPagopaPnCxId, xPagopaPnCxGroups!=null?Arrays.toString(xPagopaPnCxGroups.toArray()):null);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_CREATE, logMessage)
                .uid(xPagopaPnUid)
                .build();
        logEvent.log();
        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        List<String> groupToAdd = checkGroups(requestNewApiKeyDto.getGroups(), xPagopaPnCxGroups);
        log.debug("list groupsToAdd size: {}", groupToAdd.size());
        return paAggregationsService.searchAggregationId(xPagopaPnCxId)
                .switchIfEmpty(Mono.defer(() -> createNewAggregate(xPagopaPnCxId)))
                .doOnNext(aggregateId -> log.info("Add PA {} to aggregate {}", xPagopaPnCxId, aggregateId))
                .flatMap(aggregateId -> {
                    String messageAction = String.format("xPagopaPnUid=%s - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s - createApiKey=%s", xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups!=null?Arrays.toString(xPagopaPnCxGroups.toArray()):null,aggregateId);
                    requestNewApiKeyDto.setGroups(groupToAdd);
                    ApiKeyModel apiKeyModel = constructApiKeyModel(requestNewApiKeyDto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId);
                    return apiKeyRepository.save(apiKeyModel)
                            .map(this::createResponseNewApiKey)
                            .onErrorResume(throwable -> {
                                logEvent.generateFailure(throwable.getMessage()).log();
                                return Mono.error(throwable);
                            })
                            .then(Mono.fromRunnable(() -> logEvent.generateSuccess(messageAction).log()));
                });
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

    private List<String> checkGroups(List<String> groups, List<String> xPagopaPnCxGroups) {
        if (xPagopaPnCxGroups == null) {
            xPagopaPnCxGroups = new ArrayList<>();
        }
        List<String> groupsToAdd = new ArrayList<>();
        if (!groups.isEmpty() && (new HashSet<>(xPagopaPnCxGroups).containsAll(groups) || xPagopaPnCxGroups.isEmpty())) {
            groupsToAdd.addAll(groups);
            return groupsToAdd;
        } else if (groups.isEmpty() && !xPagopaPnCxGroups.isEmpty()) {
            groupsToAdd.addAll(xPagopaPnCxGroups);
            return groupsToAdd;
        } else if (groups.isEmpty()) {
            return groupsToAdd;
        } else {
            groups.removeIf(xPagopaPnCxGroups::contains);
            throw new ApiKeyManagerException("User cannot add groups: " + groups, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
        return paAggregationModel;
    }
}
