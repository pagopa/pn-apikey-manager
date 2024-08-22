package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class PublicKeyValidator {
    public Mono<PublicKeyEvent.Payload> validatePayload(PublicKeyEvent.Payload payload) {
        if (payload.getKid().isEmpty() || payload.getCxId().isEmpty()) {
            throw new ApiKeyManagerException("The key or cxid is empty.", HttpStatus.BAD_REQUEST);
        }
        if (payload.getAction().isEmpty() || !"DELETE".equals(payload.getAction())) {
            throw new ApiKeyManagerException("The status is empty or not valid.", HttpStatus.BAD_REQUEST);
        }
        return Mono.just(payload);
    }

    public Mono<PublicKeyModel> validateModel(PublicKeyModel publicKeyModel) {
        if (Instant.now().isBefore(publicKeyModel.getExpireAt())) {
            throw new ApiKeyManagerException(String.format("The key with kid %s and cxid %s, is not expired.", publicKeyModel.getKid(), publicKeyModel.getCxId()), HttpStatus.BAD_REQUEST);
        }
        return Mono.just(publicKeyModel);
    }
}