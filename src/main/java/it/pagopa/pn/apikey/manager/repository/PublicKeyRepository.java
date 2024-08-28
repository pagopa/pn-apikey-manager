package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId);

    Flux<PublicKeyModel> findByCxIdAndStatus(String xPagopaPnCxId, String status);

    Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel);
}
