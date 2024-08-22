package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PublicKeyConverter {

    public Mono<PublicKeyModel> convertPayloadToModel(PublicKeyEvent.Payload payload) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(payload.getKid());
        model.setCxId(payload.getCxId());
        return Mono.just(model);
    }

}
