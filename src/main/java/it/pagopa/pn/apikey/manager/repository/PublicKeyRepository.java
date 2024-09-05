package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId);

    Mono<PublicKeyModel> updateItemStatus(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus);

    Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel);
}
