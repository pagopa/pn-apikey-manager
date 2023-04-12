package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface ApiKeyRepository {

    Mono<String> delete(String id);

    Mono<ApiKeyModel> save (ApiKeyModel apiKeyModel);

    Mono<List<ApiKeyModel>> setNewVirtualKey(List<ApiKeyModel> apiKeyModels, String virtualKey);

    Mono<ApiKeyModel> findById(String id);

    Mono<List<ApiKeyModel>> findByCxId(String xPagopaPnCxId);
    Mono<List<ApiKeyModel>> findByCxIdAndStatusRotateAndEnabled(String xPagopaPnCxId);

    Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, ApiKeyPageable pageable);

    Mono<Integer> countWithFilters(String xPagopaPnCxId, List<String> xPagopaPnCxGroups);

    Mono<ApiKeyModel> changePdnd(String id, boolean flagPdnd);

}
