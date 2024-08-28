package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class PublicKeyValidator {

    private final PublicKeyRepository publicKeyRepository;

    public Mono<PublicKeyRequestDto> validatePublicKeyRequest(PublicKeyRequestDto publicKeyRequestDto) {
        if (publicKeyRequestDto.getName() == null || publicKeyRequestDto.getName().isEmpty()) {
            return Mono.error(new ApiKeyManagerException("Name is mandatory", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyRequestDto);
    }

    public Mono<Boolean> validateRotatedKeyAlreadyExists(String xPagopaPnCxId) {
        return publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, PublicKeyStatusDto.ROTATED.getValue())
                .hasElements()
                .flatMap(hasElements -> {
                    if (Boolean.TRUE.equals(hasElements)) {
                        return Mono.error(new ApiKeyManagerException("Public key with status ROTATED already exists.", HttpStatus.BAD_REQUEST));
                    }
                    return Mono.just(true);
                });
    }

    public Mono<PublicKeyModel> validatePublicKeyRotation(PublicKeyModel model) {
        if (!PublicKeyStatusDto.ACTIVE.getValue().equals(model.getStatus())) {
            return Mono.error(new ApiKeyManagerException("Public key with status ACTIVE not found.", HttpStatus.NOT_FOUND));
        }
        return Mono.just(model);
    }
}
