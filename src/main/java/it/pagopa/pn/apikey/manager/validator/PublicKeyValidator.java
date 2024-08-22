package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Slf4j
public class PublicKeyValidator {

    public Mono<PublicKeyEvent.Payload> validatePayload(PublicKeyEvent.Payload payload) {
        if (payload.getKid().isEmpty() || payload.getCxId().isEmpty()) {
            return Mono.error(new ApiKeyManagerException("The key or cxid is empty.", HttpStatus.BAD_REQUEST));
        }
        if (payload.getAction().isEmpty() || !"DELETE".equals(payload.getAction())) {
            return Mono.error(new ApiKeyManagerException("The status is empty or not valid.", HttpStatus.BAD_REQUEST));
        }

        return Mono.just(payload);
    }

    public Mono<PublicKeyModel> checkItemExpiration(PublicKeyModel publicKeyModel) {
        if (Instant.now().isBefore(publicKeyModel.getExpireAt())) {
            log.warn(String.format("PublicKey with kid [%s] and cxid [%s], is not expired. Event will ignore",
                    publicKeyModel.getKid(), publicKeyModel.getCxId()));
        }
        return Mono.just(publicKeyModel);
    }
}
