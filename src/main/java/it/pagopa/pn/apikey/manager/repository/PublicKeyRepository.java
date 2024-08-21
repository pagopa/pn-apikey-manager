package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> changeStatus(String kid, String xPagopaPnCxId, String status);
    Mono<List<PublicKeyModel>> findByKidAndCxId(String kid, String xPagopaPnCxId);
}
