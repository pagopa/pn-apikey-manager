package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PublicKeyValidator {

    public Mono<PublicKeyModel> validateChangeStatus(PublicKeyModel publicKeyModel, String status) {
        if (PublicKeyStatusDto.BLOCKED.getValue().equals(publicKeyModel.getStatus()) && !PublicKeyStatusDto.ACTIVE.getValue().equals(status)) {
            return Mono.error(new ApiKeyManagerException("Invalid state transition", HttpStatus.BAD_REQUEST));
        } else if (PublicKeyStatusDto.ACTIVE.getValue().equals(publicKeyModel.getStatus()) && !PublicKeyStatusDto.BLOCKED.getValue().equals(status)) {
            return Mono.error(new ApiKeyManagerException("Invalid state transition", HttpStatus.BAD_REQUEST));
        }
        return Mono.just(publicKeyModel);
    }
}
