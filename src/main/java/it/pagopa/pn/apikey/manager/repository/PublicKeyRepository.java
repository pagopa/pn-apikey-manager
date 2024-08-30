package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface PublicKeyRepository {

    Mono<Page<PublicKeyModel>> getAllPaginated(String xPagopaPnCxId, PublicKeyPageable pageable, List<PublicKeyModel> cumulativeQueryResult);
}
