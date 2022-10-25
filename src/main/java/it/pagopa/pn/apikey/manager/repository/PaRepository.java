package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import reactor.core.publisher.Mono;

public interface PaRepository {

    Mono<PaAggregation> searchAggregation(String xPagopaPnCxId);

    Mono<PaAggregation> savePaAggregation(PaAggregation toSave);
}
