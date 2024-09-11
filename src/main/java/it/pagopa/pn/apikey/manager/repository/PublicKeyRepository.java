package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

public interface PublicKeyRepository {
    Mono<Page<PublicKeyModel>> findByCxIdAndWithoutTtl(String xPagopaPnCxId);

}
