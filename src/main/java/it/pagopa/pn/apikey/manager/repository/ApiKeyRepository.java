package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface ApiKeyRepository {

    Mono<String> delete(String id);

    Mono<ApiKeyModel> save (ApiKeyModel apiKeyModel);

    Mono<ApiKeyModel> findById(String id);

    Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey, String lastUpdate);

}
