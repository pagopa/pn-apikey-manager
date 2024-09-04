package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PublicKeyValidator {

    public Mono<PublicKeyRequestDto> validatePublicKeyRequest(PublicKeyRequestDto publicKeyRequestDto) {
        if (publicKeyRequestDto.getName() == null || publicKeyRequestDto.getName().isEmpty()) {
            return Mono.error(new ApiKeyManagerException("Name is mandatory", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyRequestDto);
    }

    public Mono<PublicKeyModel> validateDeletePublicKey(PublicKeyModel model) {
        log.debug("Validating delete public key with status {}", model.getStatus());
        if (PublicKeyStatusDto.BLOCKED.getValue().equals(model.getStatus())) {
            return Mono.just(model);
        } else {
            return Mono.error(new ApiKeyManagerException("Public key can not be deleted", HttpStatus.BAD_REQUEST));
        }
    }
}
