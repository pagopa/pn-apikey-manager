package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.VirtualKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ResponseNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Service
@CustomLog
@AllArgsConstructor
public class VirtualKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final VirtualKeyValidator validator;

    public Mono<ResponseNewVirtualKeyDto> createVirtualKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<RequestNewVirtualKeyDto> requestNewVirtualKeyDto, String role, List<String> groups) {
        if (!VirtualKeyConstant.ALLOWED_CX_TYPE_VIRTUAL_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return validator.validateTosAndValidPublicKey(xPagopaPnCxId, xPagopaPnUid, xPagopaPnCxType, role, groups)
                .then(this.validateActiveVirtualKey(xPagopaPnUid, xPagopaPnCxId))
                .then(requestNewVirtualKeyDto)
                .flatMap(dto -> createVirtualKeyModel(dto, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId))
                .flatMap(apiKeyRepository::save)
                .flatMap(this::createVirtualKeyDto);
    }

    private Mono<Void> validateActiveVirtualKey(String xPagopaPnUid, String xPagopaPnCxId) {
        return apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, VirtualKeyStatusDto.ENABLED.getValue(), String.valueOf(ApiKeyModel.Scope.CLIENTID))
                .flatMap(apiKeyModelList -> {
                    if (apiKeyModelList.items() != null && !apiKeyModelList.items().isEmpty()) {
                        return Mono.error(new ApiKeyManagerException("Virtual key with status ENABLED already exists.", HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }

    private Mono<ApiKeyModel> createVirtualKeyModel(RequestNewVirtualKeyDto dto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        ApiKeyModel model = new ApiKeyModel();
        model.setId(UUID.randomUUID().toString());
        model.setCorrelationId("");
        model.setGroups(new ArrayList<>());
        model.setLastUpdate(LocalDateTime.now());
        model.setName(dto.getName());
        model.setPdnd(false);
        model.setStatus(VirtualKeyStatusDto.ENABLED.getValue());
        ApiKeyHistoryModel historyModel = new ApiKeyHistoryModel();
        historyModel.setChangeByDenomination(UUID.randomUUID().toString());
        historyModel.setDate(LocalDateTime.now());
        historyModel.setStatus(VirtualKeyStatusDto.CREATED.getValue());
        model.setStatusHistory(List.of(historyModel));
        model.setVirtualKey(UUID.randomUUID().toString());
        model.setCxId(xPagopaPnCxId);
        model.setCxType(xPagopaPnCxType.getValue());
        model.setUid(xPagopaPnUid);
        model.setScope(ApiKeyModel.Scope.CLIENTID);
        return Mono.just(model);
    }

    private Mono<ResponseNewVirtualKeyDto> createVirtualKeyDto(ApiKeyModel apiKeyModel) {
        ResponseNewVirtualKeyDto response = new ResponseNewVirtualKeyDto();
        response.setId(apiKeyModel.getId());
        response.setVirtualKey(apiKeyModel.getVirtualKey());
        return Mono.just(response);
    }
}
