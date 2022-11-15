package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PaAggregationsService {

    private final PaAggregationRepository paAggregationRepository;

    public PaAggregationsService(PaAggregationRepository paAggregationRepository) {
        this.paAggregationRepository = paAggregationRepository;
    }

    public Mono<String> searchAggregationId(String xPagoPaPnCxId) {
        return paAggregationRepository.searchAggregation(xPagoPaPnCxId)
                .map(PaAggregationModel::getAggregateId);
    }

    public Mono<PaAggregationModel> createNewPaAggregation(PaAggregationModel paAggregationModel) {
        return paAggregationRepository.savePaAggregation(paAggregationModel);
    }
}
