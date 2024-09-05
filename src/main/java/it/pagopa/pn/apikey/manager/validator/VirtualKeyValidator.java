package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class VirtualKeyValidator {

    private final ApiKeyRepository apiKeyRepository;

    public VirtualKeyValidator(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    private final static String FORBIDDEN_MESSAGE = "User not allowed to update this virtual key";

    public static Mono<Void> validateCxType(CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!Objects.equals(CxTypeAuthFleetDto.PG.toString(), xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException("CxType does not match", HttpStatus.FORBIDDEN));
        }
        return Mono.empty();
    }

    public Mono<ApiKeyModel> checkCxIdAndUid(String xPagopaPnCxId, String uId, ApiKeyModel apiKey) {
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId()) || !Objects.equals(uId, apiKey.getUid())) {
            return Mono.error(new ApiKeyManagerException(FORBIDDEN_MESSAGE, HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> checkCxId(String xPagopaPnCxId, ApiKeyModel apiKey) {
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId())) {
            return Mono.error(new ApiKeyManagerException(FORBIDDEN_MESSAGE, HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<Void> validateStateTransition(ApiKeyModel apiKeyModel, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        if (requestVirtualKeyStatusDto.getStatus().equals(RequestVirtualKeyStatusDto.StatusEnum.ENABLE) && apiKeyModel.getStatus().equals(ApiKeyStatusDto.BLOCKED.toString()) ||
                (requestVirtualKeyStatusDto.getStatus().equals(RequestVirtualKeyStatusDto.StatusEnum.BLOCK) && apiKeyModel.getStatus().equals(ApiKeyStatusDto.ENABLED.toString())))
        {
            return Mono.empty();
        }
        return Mono.error(new ApiKeyManagerException("Invalid state transition", HttpStatus.CONFLICT));
    }

    public Mono<Void> validateNoOtherKeyWithSameStatus(String xPagopaPnUid, String cxId, String status) {
        return apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, cxId, status, ApiKeyModel.Scope.CLIENTID.name())
                .flatMap(page -> {
                    if (!page.items().isEmpty()) {
                        return Mono.error(new ApiKeyManagerException(String.format("A key with status %s already exists", status), HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }
}