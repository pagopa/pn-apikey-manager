package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.VirtualKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;

@Service
@AllArgsConstructor
@CustomLog
public class VirtualKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public Mono<String> deleteVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {

        if (!VirtualKeyConstant.ALLOWED_CX_TYPE_VIRTUAL_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }

        return apiKeyRepository.findById(id)
                .flatMap(virtualKeyModel -> this.validateRoleForDeletion(virtualKeyModel, xPagopaPnUid, xPagopaPnCxId, xPagopaPnCxRole, xPagopaPnCxGroups))
                .flatMap(this::isDeleteOperationAllowed)
                .map(virtualKeyModel -> this.updateVirtualKeyStatusToDelete(virtualKeyModel, xPagopaPnUid))
                .flatMap(apiKeyRepository::save)
                .thenReturn("VirtualKey deleted");
    }

    private ApiKeyModel updateVirtualKeyStatusToDelete(ApiKeyModel virtualKeyModel, String xPagopaPnUid) {
        virtualKeyModel.setStatus(VirtualKeyStatusDto.DELETED.getValue());
        virtualKeyModel.getStatusHistory().add(createNewHistory(xPagopaPnUid, VirtualKeyStatusDto.DELETED.getValue()));
        return virtualKeyModel;
    }

    @NotNull
    private static ApiKeyHistoryModel createNewHistory(String xPagopaPnUid, String status) {
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setStatus(status);
        apiKeyHistoryModel.setChangeByDenomination(xPagopaPnUid);
        return apiKeyHistoryModel;
    }

    private Mono<ApiKeyModel> validateRoleForDeletion(ApiKeyModel virtualKeyModel, String xPagopaPnUid, String xPagopaCxId,String xPagopaPnCxRole, List<String> xPagopaPnCxGroups) {
        log.debug("validateRoleForDeletion - xPagopaPnUid: {}, xPagopaPnCxRole: {}, xPagopaPnCxGroups: {}", xPagopaPnUid, xPagopaPnCxRole, xPagopaPnCxGroups);
        return VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(isAdmin -> {
                    if((isAdmin && virtualKeyModel.getCxId().equals(xPagopaCxId)) || virtualKeyModel.getUid().equals(xPagopaPnUid)) {
                        return Mono.just(virtualKeyModel);
                    }
                    return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_DELETE, HttpStatus.FORBIDDEN));
                });
    }

    private Mono<ApiKeyModel> isDeleteOperationAllowed(ApiKeyModel virtualKeyModel) {
        VirtualKeyStatusDto status = VirtualKeyStatusDto.fromValue(virtualKeyModel.getStatus());
        if (!status.getValue().equals(VirtualKeyStatusDto.BLOCKED.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, virtualKeyModel.getStatus()), HttpStatus.CONFLICT));
        }
        return Mono.just(virtualKeyModel);
    }
}
