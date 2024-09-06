package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isEmpty(publicKeyRequestDto.getName())) {
            return Mono.error(new ApiKeyManagerException("Name is mandatory", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyRequestDto);
    }

    public Mono<PublicKeyModel> validateDeletePublicKey(PublicKeyModel model) {
        log.debug("Validating delete public key with status {}", model.getStatus());
        if (PublicKeyStatusDto.BLOCKED.getValue().equals(model.getStatus())) {
            return Mono.just(model);
        } else {
            return Mono.error(new ApiKeyManagerException("Public key can not be deleted", HttpStatus.CONFLICT));
        }
    }

    public Mono<PublicKeyModel> validateChangeStatus(PublicKeyModel publicKeyModel, String status) {
        log.debug("validateChangeStatus for publicKeyModel with status: {}, to status: {}", publicKeyModel.getStatus(), status);
        if(status.equals(ENABLE_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.BLOCKED.name())) {
            return Mono.just(publicKeyModel);
        } else if(status.equals(BLOCK_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.ACTIVE.name())) {
            return Mono.just(publicKeyModel);
        } else {
            return Mono.error(new ApiKeyManagerException("Invalid state transition", HttpStatus.CONFLICT));
        }
    }

    public Mono<Void> checkPublicKeyAlreadyExistsWithStatus(String xPagopaPnCxId, String status) {
        log.debug("validateKeyAlreadyExistsByStatus xPagopaPnCxId: {}, status: {}", xPagopaPnCxId, status);
        return publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, status)
                .hasElements()
                .flatMap(hasElements -> {
                    if (Boolean.TRUE.equals(hasElements)) {
                        return Mono.error(new ApiKeyManagerException(String.format("Public key with status %s already exists.", status), HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }

    public Mono<PublicKeyModel> validatePublicKeyRotation(PublicKeyModel model) {
        if (!PublicKeyStatusDto.ACTIVE.getValue().equals(model.getStatus())) {
            return Mono.error(new ApiKeyManagerException("Public key can not be rotated.", HttpStatus.CONFLICT));
        }
        return Mono.just(model);
    }
}
