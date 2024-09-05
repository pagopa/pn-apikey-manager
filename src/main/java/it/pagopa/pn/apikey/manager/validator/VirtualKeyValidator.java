package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.Objects;

@Component
public class VirtualKeyValidator {

    public Mono<Void> validateCxType(CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!Objects.equals(CxTypeAuthFleetDto.PG.toString(), xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException("Error, cxType must be PG", HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    public Mono<Void> checkExistingRotatedKeys(Page<ApiKeyModel> existingRotatedKeys) {
        if (!existingRotatedKeys.items().isEmpty()) {
            return Mono.error(new ApiKeyManagerException("User already has a rotated key with the same CxId", HttpStatus.CONFLICT));
        }
        return Mono.empty();
    }

    public Mono<ApiKeyModel> checkCxId(String xPagopaPnCxId, ApiKeyModel apiKey) {
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId())) {
            return Mono.error(new ApiKeyManagerException("CxId does not match", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> checkStatus(ApiKeyModel apiKey) {
        if (!Objects.equals(apiKey.getStatus(), ApiKeyStatusDto.ENABLED.toString())) {
            return Mono.error(new ApiKeyManagerException("virtualKey is not in enabled state", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(apiKey);
    }

}
