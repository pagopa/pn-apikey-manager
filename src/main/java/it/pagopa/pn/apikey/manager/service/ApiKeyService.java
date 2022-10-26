package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.ApiKeyConverter;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    private final ApiKeyConverter apiKeyConverter;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyConverter apiKeyConverter) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyConverter = apiKeyConverter;
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(String xPagopaPnCxId, List<String> xPagopaPnCxGroups, int limit, String lastKey, String lastUpdate, Boolean showVirtualKey) {

        return apiKeyRepository.getAllWithFilter(xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,lastUpdate)
                .doOnNext(apiKeyModelPage -> {
                    log.info("founded list size");// TO DO INFO SUL FOUND
                })
                .map(apiKeyModelPage -> apiKeyConverter.convertResponsetoDto(apiKeyModelPage,showVirtualKey));
    }

}
