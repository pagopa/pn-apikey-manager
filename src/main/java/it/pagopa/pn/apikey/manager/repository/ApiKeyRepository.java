package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import reactor.core.publisher.Mono;

import java.util.List;


public interface ApiKeyRepository {

    Mono<List<ApiKeyModel>> getAllWithFilter(String xPagopaPnCxId, List<String> xPagopaPnCxGroups);

}
