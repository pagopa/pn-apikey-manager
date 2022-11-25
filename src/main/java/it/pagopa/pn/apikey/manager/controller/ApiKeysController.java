package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.api.ApiKeysApi;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
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

    /**
     * PUT /apikey-manager/api-keys/{id}/status : Cambia lo stato dell&#39;api key
     * servizio di cambio stato dell&#39;api key
     *
     * @param xPagopaPnUid           User Identifier (required)
     * @param xPagopaPnCxType        Customer/Receiver Type (required)
     * @param xPagopaPnCxId          Customer/Receiver Identifier (required)
     * @param id                     Identificativo univoco dell&#39;api key (required)
     * @param requestApiKeyStatusDto Action per il cambio stato di un&#39;api key (required)
     * @param xPagopaPnCxGroups      Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. enable an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> changeStatusApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                         String id, RequestApiKeyStatusDto requestApiKeyStatusDto, List<String> xPagopaPnCxGroups,
                                                         final ServerWebExchange exchange) {
        return manageApiKeyService.changeStatus(id, requestApiKeyStatusDto, xPagopaPnUid, xPagopaPnCxType)
                .publishOn(scheduler)
                .map(s -> ResponseEntity.ok().build());
    }

    /**
     * DELETE /apikey-manager/api-keys/{id} : Rimozione api key
     * servizio di rimozione dell&#39;api key
     *
     * @param xPagopaPnUid      User Identifier (required)
     * @param xPagopaPnCxType   Customer/Receiver Type (required)
     * @param xPagopaPnCxId     Customer/Receiver Identifier (required)
     * @param id                Identificativo univoco dell&#39;api key (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Wrong state transition (i.e. delete an enabled key) (status code 409)
     * or Not found (status code 404)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId,
                                                    String id, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return manageApiKeyService.deleteApiKey(id, xPagopaPnCxType)
                .publishOn(scheduler)
                .map(s -> ResponseEntity.ok().build());
    }

    /**
     * GET /apikey-manager/api-keys : Ricerca api keys
     * servizio di consultazione della lista delle api keys
     *
     * @param xPagopaPnUid      User Identifier (required)
     * @param xPagopaPnCxType   Customer/Receiver Type (required)
     * @param xPagopaPnCxId     Customer/Receiver Identifier (required)
     * @param xPagopaPnCxGroups Customer Groups (optional)
     * @param limit             (optional)
     * @param lastKey           (optional)
     * @param lastUpdate        (optional)
     * @param showVirtualKey    (optional, default to false)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ApiKeysResponseDto>> getApiKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey, final ServerWebExchange exchange) {
        return manageApiKeyService.getApiKeyList(xPagopaPnCxId, xPagopaPnCxGroups, limit, lastKey, lastUpdate, showVirtualKey, xPagopaPnCxType)
                .map(apiKeyRowDtos -> ResponseEntity.ok().body(apiKeyRowDtos))
                .publishOn(scheduler);
    }

    /**
     * POST /apikey-manager/api-keys : Creazione api key
     * servizio di creazione di un&#39;api key
     *
     * @param xPagopaPnUid        User Identifier (required)
     * @param xPagopaPnCxType     Customer/Receiver Type (required)
     * @param xPagopaPnCxId       Customer/Receiver Identifier (required)
     * @param requestNewApiKeyDto (required)
     * @param xPagopaPnCxGroups   Customer Groups (optional)
     * @return OK (status code 200)
     * or Bad request (status code 400)
     * or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<ResponseNewApiKeyDto>> newApiKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, RequestNewApiKeyDto requestNewApiKeyDto, List<String> xPagopaPnCxGroups, final ServerWebExchange exchange) {
        return createApiKeyService.createApiKey(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, requestNewApiKeyDto, xPagopaPnCxGroups)
                .map(s -> ResponseEntity.ok().body(s))
                .publishOn(scheduler);
    }
}
