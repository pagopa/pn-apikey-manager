package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.api.ApiKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.service.CreateApiKeyService;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

@RestController
public class ApiKeysController implements ApiKeysApi {

    private final ManageApiKeyService manageApiKeyService;
    private final CreateApiKeyService createApiKeyService;

    @Qualifier("apikeyManagerScheduler")
    private final Scheduler scheduler;

    public ApiKeysController(ManageApiKeyService manageApiKeyService, CreateApiKeyService createApiKeyService, Scheduler scheduler) {
        this.manageApiKeyService = manageApiKeyService;
        this.createApiKeyService = createApiKeyService;
        this.scheduler = scheduler;
    }

    @Override
    public Mono<ResponseEntity<Void>> changeStatusApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                                String id, String status, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return manageApiKeyService.changeStatus(id,status,xPagopaPnUid).map(s -> ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                      String id, List<String> xPagopaPnCxGroups,  final ServerWebExchange exchange) {
        return manageApiKeyService.deleteApiKey(id).map(s -> ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<ApiKeysResponseDto>> getApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey, final ServerWebExchange exchange) {
        return manageApiKeyService.getApiKeyList(xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,lastUpdate,showVirtualKey)
                .map(apiKeyRowDtos -> ResponseEntity.ok().body(apiKeyRowDtos));
    }

    @Override
    public Mono<ResponseEntity<ResponseNewApiKeyDto>>newApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, RequestNewApiKeyDto requestNewApiKeyDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return createApiKeyService.createApiKey(xPagopaPnUid,xPagopaPnCxType,xPagopaPnCxId,requestNewApiKeyDto, xPagopaPnCxGroups)
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }
}
