package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Mono;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> changeStatus(PublicKeyModel publicKeyModel);
    Mono<PublicKeyModel> findByKidAndCxId(PublicKeyModel publicKeyModel);
}
