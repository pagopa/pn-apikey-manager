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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.BLOCK_OPERATION;
import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;
import static it.pagopa.pn.apikey.manager.model.PublicKeyEventAction.DELETE;

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

    public Mono<PublicKeyEvent.Payload> validatePayload(PublicKeyEvent.Payload payload) {
        if (payload.getKid().isEmpty() || payload.getCxId().isEmpty()) {
            return Mono.error(new ApiKeyManagerException("The key or cxid is empty.", HttpStatus.BAD_REQUEST));
        }
        if (payload.getAction().isEmpty() || !DELETE.name().equals(payload.getAction())) {
            return Mono.error(new ApiKeyManagerException("The status is empty or not valid.", HttpStatus.BAD_REQUEST));
        }

        return Mono.just(payload);
    }

    public Mono<PublicKeyModel> checkItemExpiration(PublicKeyModel publicKeyModel) {
        if (Instant.now().isBefore(publicKeyModel.getExpireAt())) {
            log.warn(String.format("PublicKey with kid [%s] and cxid [%s], is not expired. Event will ignore",
                    publicKeyModel.getKid(), publicKeyModel.getCxId()));
            return Mono.empty();
        }
        return Mono.just(publicKeyModel);
    }

    public Mono<PublicKeyModel> checkIfItemIsNotAlreadyDeleted(PublicKeyModel publicKeyModel) {
        if ("DELETED".equals(publicKeyModel.getStatus())) { //TODO: CHANGE WITH ENUM AFTER OPENAPI GENERATION
            log.debug(String.format("PublicKey with kid [%s] and cxid [%s], is already DELETED. Event will ignore",
                    publicKeyModel.getKid(), publicKeyModel.getCxId()));
            return Mono.empty();
        }
        return Mono.just(publicKeyModel);
    }
}