package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.BLOCK_OPERATION;
import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;

@Component
@Slf4j
@AllArgsConstructor
public class PublicKeyValidator {

    private final PublicKeyRepository publicKeyRepository;

    public Mono<PublicKeyRequestDto> validatePublicKeyRequest(PublicKeyRequestDto publicKeyRequestDto) {
        if (publicKeyRequestDto.getName() == null || publicKeyRequestDto.getName().isEmpty()) {
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.PUBLIC_KEY_NAME_MANDATORY, HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyRequestDto);
    }

    public Mono<PublicKeyModel> validateDeletePublicKey(PublicKeyModel model) {
        log.debug("Validating delete public key with status {}", model.getStatus());
        if (PublicKeyStatusDto.BLOCKED.getValue().equals(model.getStatus())) {
            return Mono.just(model);
        } else {
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.PUBLIC_KEY_CAN_NOT_DELETE, HttpStatus.CONFLICT));
        }
    }

    public Mono<PublicKeyModel> validateChangeStatus(PublicKeyModel publicKeyModel, String status) {
        log.debug("validateChangeStatus for publicKeyModel with status: {}, to status: {}", publicKeyModel.getStatus(), status);
        if(status.equals(ENABLE_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.BLOCKED.name())) {
            return Mono.just(publicKeyModel);
        } else if(status.equals(BLOCK_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.ACTIVE.name())) {
            return Mono.just(publicKeyModel);
        } else {
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_STATE_TRANSITION, HttpStatus.CONFLICT));
        }
    }

    public Mono<Void> checkPublicKeyAlreadyExistsWithStatus(String xPagopaPnCxId, String status) {
        log.debug("validateKeyAlreadyExistsByStatus xPagopaPnCxId: {}, status: {}", xPagopaPnCxId, status);
        return publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, status)
                .hasElements()
                .flatMap(hasElements -> {
                    if (Boolean.TRUE.equals(hasElements)) {
                        return Mono.error(new ApiKeyManagerException(String.format(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_EXISTS, status), HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }
}