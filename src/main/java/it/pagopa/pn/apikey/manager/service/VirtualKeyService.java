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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CAN_NOT_DELETE;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

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
                .flatMap(virtualKeyModel -> this.validateRole(virtualKeyModel, xPagopaPnUid, xPagopaPnCxRole, xPagopaPnCxGroups))
                .flatMap(this::isOperationAllowed)
                .flatMap(virtualKeyModel -> this.createVirtualKeyModelToDelete(virtualKeyModel, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, id))
                .flatMap(apiKeyRepository::save)
                .thenReturn("VirtualKey deleted");
    }

    private Mono<ApiKeyModel> createVirtualKeyModelToDelete(ApiKeyModel virtualKeyModel, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String id) {

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId(id);
        apiKeyModel.setVirtualKey(id);
        apiKeyModel.setStatus(virtualKeyModel.getStatus());
        apiKeyModel.setCxId(xPagopaPnCxId);
        apiKeyModel.setCxGroup(xPagopaPnCxGroups);
        apiKeyModel.setName(virtualKeyModel.getName());
        apiKeyModel.setPdnd(virtualKeyModel.isPdnd());
        apiKeyModel.setUid(xPagopaPnUid);
        apiKeyModel.setCxType(xPagopaPnCxType.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        //TODO aggiungere un nuovo stato nel VirtualKeyStatusDto ?
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination(xPagopaPnUid);

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);

        return Mono.just(apiKeyModel);
    }

    private Mono<ApiKeyModel> validateRole(ApiKeyModel virtualKeyModel, String xPagopaPnUid, String xPagopaPnCxRole, List<String> xPagopaPnCxGroups) {
        return VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(isAdmin -> {
                    if (!isAdmin && !virtualKeyModel.getUid().equals(xPagopaPnUid)) {
                        return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, virtualKeyModel.getStatus()), HttpStatus.BAD_REQUEST));
                    }
                    return Mono.just(virtualKeyModel);
                });
    }

    private Mono<ApiKeyModel> isOperationAllowed(ApiKeyModel virtualKeyModel) {
        VirtualKeyStatusDto status = VirtualKeyStatusDto.fromValue(virtualKeyModel.getStatus());
        if (!status.getValue().equals(VirtualKeyStatusDto.BLOCKED.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CAN_NOT_DELETE, virtualKeyModel.getStatus()), HttpStatus.BAD_REQUEST));
        }
        return Mono.just(virtualKeyModel);
    }
}
