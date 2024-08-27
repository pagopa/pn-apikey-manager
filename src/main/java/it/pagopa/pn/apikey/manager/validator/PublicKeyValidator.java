package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
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
}
