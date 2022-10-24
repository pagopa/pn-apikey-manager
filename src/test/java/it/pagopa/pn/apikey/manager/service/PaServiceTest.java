package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

@ContextConfiguration(classes = {PaService.class})
@ExtendWith(SpringExtension.class)
class PaServiceTest {
    @MockBean
    private PaRepository paRepository;

    @Autowired
    private PaService paService;

    /**
     * Method under test: {@link PaService#searchAggregationId(String)}
     */
    @Test
    void testSearchAggregationId() {
        when(paRepository.searchAggregation((String) any())).thenReturn((Mono<PaAggregation>) mock(Mono.class));
        paService.searchAggregationId("42");
        verify(paRepository).searchAggregation((String) any());
    }

    /**
     * Method under test: {@link PaService#searchAggregationId(String)}
     */
    @Test
    void testSearchAggregationId2() {
        when(paRepository.searchAggregation((String) any())).thenReturn((Mono<PaAggregation>) mock(Mono.class));
        paService.searchAggregationId("X Pago Pa Pn Cx Id");
        verify(paRepository).searchAggregation((String) any());
    }
}

