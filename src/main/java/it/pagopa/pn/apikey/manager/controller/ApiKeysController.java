package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.api.ApiKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.service.ApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class ApiKeysController implements ApiKeysApi {

    private final ApiKeyService apiKeyService;

    public ApiKeysController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public Mono<ResponseEntity<Void>> changeStatusApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                         String id, String body, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public   Mono<ResponseEntity<Void>> deleteApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                      String id, List<String> xPagopaPnCxGroups,  final ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<ApiKeysResponseDto>> getApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey, final ServerWebExchange exchange) {
        return apiKeyService.getApiKeyList(xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,lastUpdate,showVirtualKey)
                .map(apiKeyRowDtos -> ResponseEntity.ok().body(apiKeyRowDtos));
    }

    @Override
    public Mono<ResponseEntity<ResponseNewApiKeyDto>>newApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, RequestNewApiKeyDto requestNewApiKeyDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return Mono.empty();
    }
}
