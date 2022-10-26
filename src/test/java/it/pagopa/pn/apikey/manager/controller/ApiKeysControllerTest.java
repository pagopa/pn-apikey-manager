package it.pagopa.pn.apikey.manager.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.AggregationRepositoryImpl;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepositoryImpl;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import it.pagopa.pn.apikey.manager.service.CreateApiKeyService;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@ContextConfiguration(classes = {ApiKeysController.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiKeysControllerTest {

    @MockBean
    private ManageApiKeyService manageApiKeyService;

    @MockBean
    private CreateApiKeyService createApiKeyService;

    @Autowired
    private ApiKeysController apiKeysController;

    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @MockBean
    ServerWebExchange serverWebExchange;

    /**
     * Method under test: {@link ApiKeysController#changeStatusApiKey(String, CxTypeAuthFleetDto, String, String, String, List, ServerWebExchange)}
     */
    @Test
    void testChangeStatusApiKey() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient),null,null);
        when(manageApiKeyService.changeStatus(any(), any(), any()))
                .thenReturn(Mono.just(new ApiKeyModel()));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, scheduler);
        ArrayList<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        StepVerifier.create(apiKeysController.changeStatusApiKey("foo", CxTypeAuthFleetDto.PA, "foo", "foo", "foo", xPagopaPnCxGroups,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }

    /**
     * Method under test: {@link ApiKeysController#deleteApiKeys(String, CxTypeAuthFleetDto, String, String, List, ServerWebExchange)}
     */
    @Test
    void testDeleteApiKeys() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient), null,null);

        when(manageApiKeyService.deleteApiKey(any())).thenReturn(Mono.just("id"));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, scheduler);
        ArrayList<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        StepVerifier.create(apiKeysController.deleteApiKeys("foo", CxTypeAuthFleetDto.PA, "foo", "foo", xPagopaPnCxGroups,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }

    /**
     * Method under test: {@link ApiKeysController#newApiKey(String, CxTypeAuthFleetDto, String, RequestNewApiKeyDto, List, ServerWebExchange)}
     */
    @Test
    void testNewApiKey() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient,"");
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient1), null,null);

        ResponseNewApiKeyDto apiKeyModel = new ResponseNewApiKeyDto();
        apiKeyModel.setApiKey("");
        apiKeyModel.setId("");

        when(createApiKeyService.createApiKey(any(), any(), any(),
                any(),  any())).thenReturn(Mono.just(apiKeyModel));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, scheduler);
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        ArrayList<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        ResponseEntity<ResponseNewApiKeyDto> responseEntity = ResponseEntity.ok().body(apiKeyModel);
        StepVerifier.create(apiKeysController.newApiKey("foo", CxTypeAuthFleetDto.PA, "foo", requestNewApiKeyDto, xPagopaPnCxGroups,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(responseEntity);
    }

    @Test
    void testGetApiKeys() {
        String xPagopaPnUid = "PA-test-1";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PA;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("REC");
        Integer limit = 10;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);
        when(manageApiKeyService.getApiKeyList(anyString(),any(),anyInt(),anyString())).thenReturn(Mono.just(apiKeysResponseDto));
        StepVerifier.create(apiKeysController.getApiKeys(xPagopaPnUid,xPagopaPnCxType,xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,serverWebExchange))
                .expectNext(ResponseEntity.ok().body(apiKeysResponseDto));
    }
}

