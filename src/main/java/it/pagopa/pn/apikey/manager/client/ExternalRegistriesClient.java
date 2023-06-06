package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.PaDetailDto;
import it.pagopa.pn.apikey.manager.model.InternalPaDetailDto;
import it.pagopa.pn.apikey.manager.model.PaGroup;
import it.pagopa.pn.apikey.manager.model.PaGroupStatus;
import it.pagopa.pn.commons.log.PnLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.apikey.manager.constant.ProcessStatus.*;

@Component
@lombok.CustomLog
public class ExternalRegistriesClient {

    private final WebClient webClient;

    protected ExternalRegistriesClient(ExternalRegistriesWebClient externalRegistriesWebClient) {
        this.webClient = externalRegistriesWebClient.init();
    }

    public Mono<List<PaDetailDto>> getAllPa(String name) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_APIKEY_MANAGER, PROCESS_SERVICE_AGGREGATION_GET_ALL_PA);
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
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_APIKEY_MANAGER, PROCESS_SERVICE_API_KEY_GET_PA_BY_ID);
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
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_APIKEY_MANAGER, PROCESS_SERVICE_API_KEY_GET_PA_GROUPS_BY_ID);
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
