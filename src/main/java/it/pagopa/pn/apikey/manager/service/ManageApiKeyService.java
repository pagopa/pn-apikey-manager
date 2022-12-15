package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
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
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    private static final String CX_TYPE_NOT_ALLOWED = "CxTypeAuthFleet {} not allowed";

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyConverter apiKeyConverter;
    private final PnAuditLogBuilder auditLogBuilder;


    public ManageApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyConverter apiKeyConverter, PnAuditLogBuilder auditLogBuilder) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyConverter = apiKeyConverter;
        this.auditLogBuilder = auditLogBuilder;
    }

    public Mono<ApiKeyModel> changeStatus(String id, RequestApiKeyStatusDto requestApiKeyStatusDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType) {
        String logMessage = "";
        PnAuditLogEventType type = null;
        String status = requestApiKeyStatusDto.getStatus().getValue();
        if(status.equals(ROTATE)){
            logMessage = String.format("Rotazione di una API Key - xPagopaPnUid=%s - xPagopaPnCxType=%s", xPagopaPnUid, xPagopaPnCxType);
            type = PnAuditLogEventType.AUD_AK_ROTATE;
        }
        if(status.equals(BLOCK)){
            logMessage = String.format("Blocco di una API Key - xPagopaPnUid=%s - xPagopaPnCxType=%s", xPagopaPnUid, xPagopaPnCxType);
            type = PnAuditLogEventType.AUD_AK_BLOCK;
        }
        if(status.equals(ENABLE)){
            logMessage = String.format("Riattivazione di una API Key - xPagopaPnUid=%s - xPagopaPnCxType=%s", xPagopaPnUid, xPagopaPnCxType);
            type = PnAuditLogEventType.AUD_AK_REACTIVATE;
        }
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(type, logMessage)
                .uid(xPagopaPnUid)
                .build();
        logEvent.log();

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.error(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel, status)) {
                        ApiKeyModel oldApiKey = new ApiKeyModel(apiKeyModel);
                        apiKeyModel.setStatus(decodeStatus(status, false).getValue());
                        apiKeyModel.setLastUpdate(LocalDateTime.now());
                        apiKeyModel.getStatusHistory().add(createNewApiKeyHistory(status, xPagopaPnUid));
                        String messageAction = String.format("xPagopaPnUid=%s - xPagopaPnCxType=%s", xPagopaPnUid, xPagopaPnCxType);
                        return saveAndCheckIfRotate(apiKeyModel, oldApiKey, status, xPagopaPnUid)
                                .doOnNext(apiKeyModel1 -> log.info("Updated Apikey with id: {} and status: {}", id, status))
                                .onErrorResume(throwable -> {
                                    logEvent.generateFailure(throwable.getMessage()).log();
                                    return Mono.error(throwable);
                                })
                                .then(Mono.fromRunnable(() -> logEvent.generateSuccess(messageAction).log()));
                    } else {
                        return Mono.error(new ApiKeyManagerException(String.format(APIKEY_INVALID_STATUS, apiKeyModel.getStatus(), status), HttpStatus.CONFLICT));
                    }
                });
    }

    public Mono<String> deleteApiKey(String id, CxTypeAuthFleetDto xPagopaPnCxType) {
        String logMessage = String.format("Cancellazione API Key - IdApiKey=%s - xPagopaPnCxType=%s", id, xPagopaPnCxType.getValue());
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_DELETE, logMessage)
                .build();
        logEvent.log();

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.error(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> {
                    if (isOperationAllowed(apiKeyModel, DELETE)) {
                        String messageAction = String.format("IdApiKey=%s - xPagopaPnCxType=%s", id, xPagopaPnCxType.getValue());
                        return apiKeyRepository.delete(id)
                                .onErrorResume(throwable -> {
                                    logEvent.generateFailure(throwable.getMessage()).log();
                                    return Mono.error(throwable);
                                })
                                .then(Mono.fromRunnable(() -> logEvent.generateSuccess(messageAction).log()));
                    } else {
                        return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, apiKeyModel.getStatus()), HttpStatus.CONFLICT));
                    }
                });
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(@NonNull String xPagopaPnCxId,
                                                  @Nullable List<String> xPagopaPnCxGroups,
                                                  @Nullable Integer limit,
                                                  @Nullable String lastEvaluatedKey,
                                                  @Nullable String lastEvaluatedLastUpdate,
                                                  @Nullable Boolean showVirtualKey,
                                                  @NonNull CxTypeAuthFleetDto xPagopaPnCxType) {
        String logMessage = String.format("Visualizzazione di una API Key - xPagopaPnCxType=%s - xPagopaPnCxId=%s - xPagopaPnCxGroups=%s", xPagopaPnCxType.getValue(), xPagopaPnCxId, xPagopaPnCxGroups!=null? Arrays.toString(xPagopaPnCxGroups.toArray()):null);
        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .cxId(xPagopaPnCxId)
                .build();
        logEvent.log();

        if (!ApiKeyConstant.ALLOWED_CX_TYPE.contains(xPagopaPnCxType)) {
            log.error(CX_TYPE_NOT_ALLOWED, xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        ApiKeyPageable pageable = toApiKeyPageable(limit, lastEvaluatedKey, lastEvaluatedLastUpdate);
        return apiKeyRepository.getAllWithFilter(xPagopaPnCxId, xPagopaPnCxGroups, pageable)
                .map(apiKeyModelPage -> apiKeyConverter.convertResponsetoDto(apiKeyModelPage, showVirtualKey))
                .onErrorResume(throwable -> {
                    logEvent.generateFailure(throwable.getMessage()).log();
                    return Mono.error(throwable);
                })
                .zipWhen(page -> apiKeyRepository.countWithFilters(xPagopaPnCxId, xPagopaPnCxGroups))
                .doOnNext(tuple -> tuple.getT1().setTotal(tuple.getT2()))
                .map(Tuple2::getT1);
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
        switch (status) {
            case BLOCK:
                return BLOCKED;
            case ENABLE:
                return ApiKeyStatusDto.ENABLED;
            case CREATE:
                if (history) {
                    return ApiKeyStatusDto.CREATED;
                } else {
                    return ApiKeyStatusDto.ENABLED;
                }
            case ROTATE:
                return ApiKeyStatusDto.ROTATED;
            default:
                throw new ApiKeyManagerException(APIKEY_INVALID_STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ApiKeyPageable toApiKeyPageable(Integer limit, String lastEvaluatedLastKey, String lastEvaluatedLastUpdate) {
        return ApiKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastEvaluatedLastKey)
                .lastEvaluatedLastUpdate(lastEvaluatedLastUpdate)
                .build();
    }
}
