package it.pagopa.pn.apikey.manager.service;

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

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(String xPagopaPnCxId, List<String> xPagopaPnCxGroups) {
        return Mono.empty();
    }
}
