package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ContextConfiguration(classes = {PaAggregationsService.class})
@ExtendWith(SpringExtension.class)
class PaAggregationsServiceTest {
    @MockBean
    private PaAggregationRepository paAggregationRepository;

    @Autowired
    private PaAggregationsService paAggregationsService;

    /**
     * Method under test: {@link PaAggregationsService#searchAggregationId(String)}
     */
    @Test
    void testSearchAggregationId() {
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        when(paAggregationRepository.searchAggregation(any())).thenReturn(Mono.just(paAggregationModel));
        StepVerifier.create(paAggregationsService.searchAggregationId("42"))
                .expectNext(paAggregationModel.getAggregateId()).verifyComplete();
    }

    @Test
    void createNewPaAggregationtest(){
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        when(paAggregationRepository.savePaAggregation(paAggregationModel)).thenReturn(Mono.just(paAggregationModel));
        StepVerifier.create(paAggregationsService.createNewPaAggregation(paAggregationModel))
                .expectNext(paAggregationModel).verifyComplete();
    }
}


