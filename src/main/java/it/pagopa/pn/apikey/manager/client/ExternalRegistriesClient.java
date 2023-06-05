package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.PaDetailDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.model.PaGroup;
import it.pagopa.pn.apikey.manager.model.PaGroupStatus;
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
                        .queryParamIfPresent("paNameFilter", Optional.ofNullable(name))
                        .path("/ext-registry/pa/v1/activated-on-pn")
                        .build())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PaDetailDto>>() {})
                .doOnError(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        throw new ApiKeyManagerException(ex.getMessage(), ex.getStatusCode());
                    }
                });
    }

    public Mono<InternalPaDetailDto> getPaById(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ext-registry-private/pa/v1/activated-on-pn/{id}")
                        .build(id))
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(InternalPaDetailDto.class)
                .doOnError(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        throw new ApiKeyManagerException(ex.getMessage(), ex.getStatusCode());
                    }
                });
    }

    public Mono<List<PaGroup>> getPaGroupsById(String id, PaGroupStatus status) {
        Optional<PaGroupStatus> optStatus =  status == null ? Optional.empty() : Optional.of(status);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ext-registry-private/pa/v1/groups-all")
                        .queryParamIfPresent("statusFilter", optStatus)
                        .build()
                )
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("x-pagopa-pn-cx-id", id);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PaGroup>>() {})
                .doOnError(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        throw new ApiKeyManagerException(ex.getMessage(), ex.getStatusCode());
                    }
                });
    }
}
