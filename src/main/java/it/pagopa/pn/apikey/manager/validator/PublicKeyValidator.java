package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.BLOCK_OPERATION;
import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;
import static it.pagopa.pn.apikey.manager.model.PublicKeyEventAction.DELETE;

@Component
@Slf4j
@AllArgsConstructor
public class PublicKeyValidator {

    private final PublicKeyRepository publicKeyRepository;

    public Mono<PublicKeyRequestDto> validatePublicKeyRequest(PublicKeyRequestDto publicKeyRequestDto) {
        if (StringUtils.isEmpty(publicKeyRequestDto.getName())) {
            return Mono.error(new ApiKeyManagerException(PUBLIC_KEY_NAME_MANDATORY, HttpStatus.BAD_REQUEST));
        }
        if (StringUtils.isEmpty(publicKeyRequestDto.getPublicKey())) {
            return Mono.error(new ApiKeyManagerException(PUBLIC_KEY_MANDATORY, HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyRequestDto);
    }

    public Mono<PublicKeyModel> validateDeletePublicKey(PublicKeyModel model) {
        log.debug("Validating delete public key with status {}", model.getStatus());
        if (PublicKeyStatusDto.BLOCKED.getValue().equals(model.getStatus()) || PublicKeyStatusDto.ROTATED.getValue().equals(model.getStatus())) {
            return Mono.just(model);
        } else {
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_DELETION, HttpStatus.CONFLICT));
        }
    }

    public Mono<PublicKeyModel> validateChangeStatus(PublicKeyModel publicKeyModel, String status) {
        log.debug("validateChangeStatus for publicKeyModel with status: {}, to status: {}", publicKeyModel.getStatus(), status);
        if(status.equals(ENABLE_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.BLOCKED.name())) {
            return Mono.just(publicKeyModel);
        } else if(status.equals(BLOCK_OPERATION) && publicKeyModel.getStatus().equals(PublicKeyStatusDto.ACTIVE.name())) {
            return Mono.just(publicKeyModel);
        } else {
            return Mono.error(new ApiKeyManagerException(String.format(ApiKeyManagerExceptionError.PUBLICKEY_INVALID_STATUS, publicKeyModel.getStatus(), status), HttpStatus.CONFLICT));
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

    public Mono<PublicKeyEvent.Payload> validatePayload(PublicKeyEvent.Payload payload) {
        if (StringUtils.isEmpty(payload.getKid()) || StringUtils.isEmpty(payload.getCxId())) {
            return Mono.error(new ApiKeyManagerException(TTL_PAYLOAD_INVALID_KID_CXID, HttpStatus.BAD_REQUEST));
        }
        if (StringUtils.isEmpty(payload.getAction()) || !DELETE.name().equals(payload.getAction())) {
            return Mono.error(new ApiKeyManagerException(TTL_PAYLOAD_INVALID_ACTION, HttpStatus.BAD_REQUEST));
        }

        return Mono.just(payload);
    }

    public Mono<PublicKeyModel> checkItemExpiration(PublicKeyModel publicKeyModel) {
        if (Instant.now().isBefore(publicKeyModel.getExpireAt())) {
            log.warn(String.format(PUBLIC_KEY_NOT_EXPIRED,
                    publicKeyModel.getKid(), publicKeyModel.getCxId()));
            return Mono.empty();
        }
        return Mono.just(publicKeyModel);
    }

    public Mono<PublicKeyModel> checkIfItemIsNotAlreadyDeleted(PublicKeyModel publicKeyModel) {
        if (PublicKeyStatusDto.DELETED.getValue().equals(publicKeyModel.getStatus())) {
            log.debug(String.format(PUBLIC_KEY_ALREADY_DELETED,
                    publicKeyModel.getKid(), publicKeyModel.getCxId()));
            return Mono.empty();
        }
        return Mono.just(publicKeyModel);
    }

    public Mono<PublicKeyModel> validatePublicKeyRotation(PublicKeyModel model, String newPublicKey) {
        if (!PublicKeyStatusDto.ACTIVE.getValue().equals(model.getStatus())) {
            return Mono.error(new ApiKeyManagerException(String.format(ApiKeyManagerExceptionError.PUBLICKEY_INVALID_STATUS, model.getStatus(), PublicKeyStatusDto.ROTATED.getValue()), HttpStatus.CONFLICT));
        }

        if(model.getPublicKey().equalsIgnoreCase(newPublicKey)) {
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_USED, HttpStatus.CONFLICT));
        }
        return Mono.just(model);
    }
}