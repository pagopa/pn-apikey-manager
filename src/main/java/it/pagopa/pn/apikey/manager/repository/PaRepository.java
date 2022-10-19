package it.pagopa.pn.apikey.manager.repository;

import reactor.core.publisher.Mono;

public interface PaRepository {

    Mono<String> searchAggregation(String xPagopaPnCxId);
}
