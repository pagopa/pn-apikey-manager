package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyValidator {

    private final ApiKeyRepository apiKeyRepository;

    public Mono<Void> validateCxType(CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!Objects.equals(CxTypeAuthFleetDto.PG.toString(), xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType.getValue()), HttpStatus.FORBIDDEN));
        }
        return Mono.empty();
    }

    public Mono<Void> checkVirtualKeyAlreadyExistsWithStatus(String xPagopaPnUid, String xPagopaPnCxId, String status) {
        return apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, status, ApiKeyModel.Scope.CLIENTID.toString())
                .flatMap(existingRotatedKeys -> {
                    if (!existingRotatedKeys.items().isEmpty()) {
                        return Mono.error(new ApiKeyManagerException(String.format(SAME_STATUS_APIKEY_ALREADY_EXISTS, status), HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }

    public Mono<ApiKeyModel> checkCxIdAndUid(String xPagopaPnCxId, String xPagopaPnCxUid, ApiKeyModel apiKey) {
        log.info("Checking CxId for virtualKey - id={}, cxId={}, uid={}", apiKey.getId(),apiKey.getCxId(), apiKey.getUid());
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId()) || !Objects.equals(xPagopaPnCxUid, apiKey.getUid())) {
            return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_OPERATION, HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> validateRotateVirtualKey(ApiKeyModel apiKey) {
        log.info("Checking status for virtualKey - id={}, status={}", apiKey.getId(), apiKey.getStatus());
        if (!Objects.equals(apiKey.getStatus(), ApiKeyStatusDto.ENABLED.toString())) {
            return Mono.error(new ApiKeyManagerException(
                        String.format(APIKEY_INVALID_STATUS, apiKey.getStatus(), ApiKeyStatusDto.ROTATED.getValue()),
                        HttpStatus.CONFLICT
                    )
            );
        }
        return Mono.just(apiKey);
    }

}
