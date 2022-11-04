package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import it.pagopa.pn.apikey.manager.repository.PaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PaAggregationsService {

    private final PaRepository paRepository;

    public PaAggregationsService(PaRepository paRepository) {
        this.paRepository = paRepository;
    }

    public Mono<String> searchAggregationId(String xPagoPaPnCxId){
        return paRepository.searchAggregation(xPagoPaPnCxId)
                .map(PaAggregation::getAggregationId);
    }

    public Mono<PaAggregation> createNewPaAggregation(PaAggregation paAggregation) {
        return paRepository.savePaAggregation(paAggregation);
    }
}
