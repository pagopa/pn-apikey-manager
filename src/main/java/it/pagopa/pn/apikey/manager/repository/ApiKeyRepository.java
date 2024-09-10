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

    Mono<List<ApiKeyModel>> findByCxId(String xPagopaPnCxId, String scope);

    Mono<Page<ApiKeyModel>> findByCxIdAndStatusRotateAndEnabled(String xPagopaPnCxId);

    Mono<Page<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, ApiKeyPageable pageable);

    Mono<Integer> countWithFilters(String xPagopaPnCxId, List<String> xPagopaPnCxGroups);

    Mono<ApiKeyModel> changePdnd(String id, boolean flagPdnd);

    Mono<Page<ApiKeyModel>> findByUidAndCxIdAndStatusAndScope(String uid, String cxId, String status, String scope);

    Mono<Page<ApiKeyModel>> getVirtualKeys(String xPagopaPnUid, String xPagopaPnCxId, List<ApiKeyModel> cumulativeQueryResult, ApiKeyPageable pageable, boolean admin);

    Mono<Integer> countWithFilters(String xPagopaPnUid, String xPagopaPnCxId, boolean admin);

}
