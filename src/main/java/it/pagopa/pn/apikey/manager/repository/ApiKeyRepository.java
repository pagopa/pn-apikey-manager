package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import reactor.core.publisher.Mono;

public interface ApiKeyRepository {

    Mono<String> delete(String id);

    Mono<ApiKeyModel> save (ApiKeyModel apiKeyModel);

    Mono<ApiKeyModel> findById(String id);
}
