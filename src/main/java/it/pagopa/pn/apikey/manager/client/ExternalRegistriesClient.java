package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.PaDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ExternalRegistriesClient {

    private final WebClient webClient;

    protected ExternalRegistriesClient(ExternalRegistriesWebClient externalRegistriesWebClient) {
        this.webClient = externalRegistriesWebClient.init();
    }

    public Mono<List<PaDetailDto>> getAllPa(String name) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParamIfPresent("paNameFilter", Optional.of(name))
                        .path("/ext-registry/pa/v1/activated-on-pn")
                        .build())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PaDetailDto>>() {})
                .doOnError(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        throw new ApiKeyManagerException(ex.getMessage(), ex.getStatusCode());
                    }
                });
    }

    public Mono<PaDetailDto> getPaById(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ext-registry-private/pa/v1/activated-on-pn/{id}")
                        .build(id))
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(PaDetailDto.class)
                .doOnError(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        throw new ApiKeyManagerException(ex.getMessage(), ex.getStatusCode());
                    }
                });
    }
}
