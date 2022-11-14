package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.PaDetailDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExternalRegistriesClientTest {

    @MockBean
    WebClient webClient;

    @MockBean
    ExternalRegistriesWebClient externalRegistriesWebClient;

    @Test
    void callEService1() {
        when(externalRegistriesWebClient.init()).thenReturn(webClient);
        ExternalRegistriesClient externalRegistriesClient = new ExternalRegistriesClient(externalRegistriesWebClient);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((Function<UriBuilder, URI>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<PaDetailDto>>() {})).thenReturn(Mono.just(new ArrayList<>()));

        StepVerifier.create(externalRegistriesClient.getAllPa("name"))
                .expectNext(new ArrayList<>())
                .verifyComplete();

    }

    @Test
    void callEService2() {
        when(externalRegistriesWebClient.init()).thenReturn(webClient);
        ExternalRegistriesClient externalRegistriesClient = new ExternalRegistriesClient(externalRegistriesWebClient);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((Function<UriBuilder, URI>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        InternalPaDetailDto internalPaDetailDto = new InternalPaDetailDto();
        when(responseSpec.bodyToMono(InternalPaDetailDto.class)).thenReturn(Mono.just(internalPaDetailDto));

        StepVerifier.create(externalRegistriesClient.getPaById("id"))
                .expectNext(internalPaDetailDto)
                .verifyComplete();

    }
}
