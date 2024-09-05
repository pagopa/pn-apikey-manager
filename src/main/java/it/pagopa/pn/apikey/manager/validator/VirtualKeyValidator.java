package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VirtualKeyValidator {

    private final ApiKeyRepository apiKeyRepository;

    public Mono<Void> validateCxType(CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!Objects.equals(CxTypeAuthFleetDto.PG.toString(), xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException("Error, cxType must be PG", HttpStatus.FORBIDDEN));
        }
        return Mono.empty();
    }

    public Mono<Void> checkExistingRotatedKeys(String xPagopaPnUid, String xPagopaPnCxId) {
        return apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString(), ApiKeyModel.Scope.CLIENTID.toString())
                .flatMap(existingRotatedKeys -> {
                    if (!existingRotatedKeys.items().isEmpty()) {
                        return Mono.error(new ApiKeyManagerException("User already has a rotated key with the same CxId", HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }

    public Mono<ApiKeyModel> checkCxIdAndUid(String xPagopaPnCxId, String xPagopaPnCxUid, ApiKeyModel apiKey) {
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId()) || !Objects.equals(xPagopaPnCxUid, apiKey.getUid())) {
            return Mono.error(new ApiKeyManagerException("CxId or uId does not match", HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> checkStatus(ApiKeyModel apiKey) {
        if (!Objects.equals(apiKey.getStatus(), ApiKeyStatusDto.ENABLED.toString())) {
            return Mono.error(new ApiKeyManagerException("virtualKey is not in enabled state", HttpStatus.CONFLICT));
        }
        return Mono.just(apiKey);
    }

}
