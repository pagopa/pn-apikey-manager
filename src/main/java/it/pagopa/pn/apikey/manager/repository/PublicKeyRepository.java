package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface PublicKeyRepository {
    Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId);

    Mono<PublicKeyModel> updateItemStatus(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus);

    Flux<PublicKeyModel> findByCxIdAndStatus(String xPagopaPnCxId, String status);

    Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel);

    Mono<Page<PublicKeyModel>> getAllWithFilterPaginated(String xPagopaPnCxId, PublicKeyPageable pageable, List<PublicKeyModel> cumulativeQueryResult);

    Mono<Integer> countWithFilters(String xPagopaPnCxId);
}
