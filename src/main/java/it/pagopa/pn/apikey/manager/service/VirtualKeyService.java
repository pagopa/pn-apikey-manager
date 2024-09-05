package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@lombok.CustomLog
@Slf4j
public class VirtualKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final VirtualKeyValidator virtualKeyValidator;

    private static final String BLOCK = "BLOCK";
    private static final String ENABLE = "ENABLE";

    public VirtualKeyService(ApiKeyRepository apiKeyRepository, VirtualKeyValidator virtualKeyValidator) {
        this.apiKeyRepository = apiKeyRepository;
        this.virtualKeyValidator = virtualKeyValidator;
    }

    public Mono<Void> changeStatusVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String id, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto, List<String> xPagopaPnCxGroups) {
        log.info("Starting changeStatusVirtualKeys - id={}, xPagopaPnUid={}", id, xPagopaPnUid);
        return VirtualKeyValidator.validateCxType(xPagopaPnCxType)
                .then(Mono.defer(() -> switch (requestVirtualKeyStatusDto.getStatus()) {
                    case ENABLE, BLOCK ->
                            reactivateOrBlockVirtualKey(id, xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto, xPagopaPnCxGroups);
                    case ROTATE ->
                            rotateVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto);
                }));
    }

    Mono<Void> reactivateOrBlockVirtualKey(String id, String xPagopaPnUid, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto, List<String> xPagopaPnCxGroups) {
        return apiKeyRepository.findById(id)
                .flatMap(apiKeyModel -> virtualKeyValidator.validateNoOtherKeyWithSameStatus(xPagopaPnUid, xPagopaPnCxId, decodeToEntityStatus(requestVirtualKeyStatusDto.getStatus().getValue()))
                        .then(VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                                .flatMap(isAdmin -> checkUserPermission(xPagopaPnUid, xPagopaPnCxId, apiKeyModel, isAdmin))
                                .flatMap(apiKey -> virtualKeyValidator.validateStateTransition(apiKey, requestVirtualKeyStatusDto)
                                        .then(Mono.defer(() -> updateApikeyAndHistory(xPagopaPnUid, requestVirtualKeyStatusDto, apiKey)))
                                )
                        )
                ).then();
    }

    private Mono<ApiKeyModel> checkUserPermission(String xPagopaPnUid, String xPagopaPnCxId, ApiKeyModel apiKeyModel, Boolean isAdmin) {
        if (isAdmin) {
            return virtualKeyValidator.checkCxId(xPagopaPnCxId, apiKeyModel);
        } else {
            return virtualKeyValidator.checkCxIdAndUid(xPagopaPnCxId, xPagopaPnUid, apiKeyModel);
        }
    }

    private Mono<ApiKeyModel> updateApikeyAndHistory(String xPagopaPnUid, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto, ApiKeyModel apiKey) {
        apiKey.setStatus(decodeToEntityStatus(requestVirtualKeyStatusDto.getStatus().getValue()));
        apiKey.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, apiKey.getStatus()));
        return apiKeyRepository.save(apiKey);
    }


    private ApiKeyHistoryModel createNewHistoryItem(String xPagopaPnUid, String status) {
        ApiKeyHistoryModel statusHistoryItem = new ApiKeyHistoryModel();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus(status);
        statusHistoryItem.setDate(LocalDateTime.now());
        return statusHistoryItem;
    }

    private String decodeToEntityStatus(String status) {
        log.debug("Requested operation: {}", status);
        return switch (status) {
            case BLOCK -> ApiKeyStatusDto.BLOCKED.name();
            case ENABLE -> ApiKeyStatusDto.ENABLED.name();
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }

    private Mono<Void> rotateVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        return null;
    }
}