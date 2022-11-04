package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.PaAggregation;
import it.pagopa.pn.apikey.manager.repository.PaRepository;
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
    private PaRepository paRepository;

    @Autowired
    private PaAggregationsService paAggregationsService;

    /**
     * Method under test: {@link PaAggregationsService#searchAggregationId(String)}
     */
    @Test
    void testSearchAggregationId() {
        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        when(paRepository.searchAggregation(any())).thenReturn(Mono.just(paAggregation));
        StepVerifier.create(paAggregationsService.searchAggregationId("42"))
                .expectNext(paAggregation.getAggregationId()).verifyComplete();
    }

    @Test
    void createNewPaAggregationtest(){
        PaAggregation paAggregation = new PaAggregation();
        paAggregation.setAggregationId("id");
        when(paRepository.savePaAggregation(paAggregation)).thenReturn(Mono.just(paAggregation));
        StepVerifier.create(paAggregationsService.createNewPaAggregation(paAggregation))
                .expectNext(paAggregation).verifyComplete();
    }
}


