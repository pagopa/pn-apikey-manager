package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Mono;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId);

    Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel);
}
