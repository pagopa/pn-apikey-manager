package it.pagopa.pn.apikey.manager.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.RequestNewApiKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ResponseNewApiKeyDto;
import it.pagopa.pn.apikey.manager.repository.AggregationRepositoryImpl;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepositoryImpl;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import it.pagopa.pn.apikey.manager.service.ApiKeyService;
import it.pagopa.pn.apikey.manager.service.PaService;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@ContextConfiguration(classes = {ApiKeysController.class})
@ExtendWith(SpringExtension.class)
class ApiKeysControllerTest {

    @MockBean
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeysController apiKeysController;

    /**
     * Method under test: {@link ApiKeysController#changeStatusApiKey(String, CxTypeAuthFleetDto, String, String, String, List, ServerWebExchange)}
     */
    @Test
    void testChangeStatusApiKey() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient),null,null);
        when(apiKeyService.changeStatus(any(), any(), any()))
                .thenReturn(Mono.just(new ApiKeyModel()));
        ApiKeysController apiKeysController = new ApiKeysController(apiKeyService);
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
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build()).verifyComplete();
    }

    /**
     * Method under test: {@link ApiKeysController#deleteApiKeys(String, CxTypeAuthFleetDto, String, String, List, ServerWebExchange)}
     */
    @Test
    void testDeleteApiKeys() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient), null,null);

        when(apiKeyService.deleteApiKey(any())).thenReturn(Mono.just("id"));
        ApiKeysController apiKeysController = new ApiKeysController(apiKeyService);
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
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build()).verifyComplete();
    }

    /**
     * Method under test: {@link ApiKeysController#getApiKeys(String, CxTypeAuthFleetDto, String, List, ServerWebExchange)}
     */
    @Test
    void testGetApiKeys2() {
        ArrayList<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        ApiKeysResponseDto apiKeyModel = new ApiKeysResponseDto();
        apiKeyModel.setItems(new ArrayList<>());

        ResponseEntity<ApiKeysResponseDto> responseEntity = ResponseEntity.ok().body(apiKeyModel);
        StepVerifier.create(apiKeysController.getApiKeys("X Pagopa Pn Uid", CxTypeAuthFleetDto.PA, "42", xPagopaPnCxGroups,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).verifyComplete();
    }

    /**
     * Method under test: {@link ApiKeysController#newApiKey(String, CxTypeAuthFleetDto, String, RequestNewApiKeyDto, List, ServerWebExchange)}
     */
    @Test
    void testNewApiKey() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(),any())).thenReturn(null);
        new ApiKeyRepositoryImpl(dynamoDbEnhancedAsyncClient);
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(),any())).thenReturn(null);
        new AggregationService(new AggregationRepositoryImpl(dynamoDbEnhancedAsyncClient1), null,null);

        ResponseNewApiKeyDto apiKeyModel = new ResponseNewApiKeyDto();
        apiKeyModel.setApiKey("");
        apiKeyModel.setId("");

        when(apiKeyService.createApiKey(any(), any(), any(),
                any(),  any())).thenReturn(Mono.just(apiKeyModel));
        ApiKeysController apiKeysController = new ApiKeysController(apiKeyService);
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
                        new AcceptHeaderLocaleContextResolver()))).expectNext(responseEntity).verifyComplete();
    }

    @InjectMocks
    ApiKeysController apiKeysController;

    @Mock
    ServerWebExchange serverWebExchange;

    @Mock
    ApiKeyService apiKeyService;

    private static Integer limit;
    private static String xPagopaPnUid;
    private static String lastKey;
    private static String xPagopaPnCxId;
    private static List<String> xPagopaPnCxGroups;
    private static CxTypeAuthFleetDto xPagopaPnCxType;
    private static ApiKeysResponseDto apiKeysResponseDto;

    @BeforeAll
    static void setup(){
        xPagopaPnUid = "PA-test-1";
        xPagopaPnCxType = CxTypeAuthFleetDto.PA;
        xPagopaPnCxId = "user1";
        xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        limit = 10;
        lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";

        apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);
    }

    @Test
    void testGetApiKeys() {
        when(apiKeyService.getApiKeyList(anyString(),any(),anyInt(),anyString())).thenReturn(Mono.just(apiKeysResponseDto));
        StepVerifier.create(apiKeysController.getApiKeys(xPagopaPnUid,xPagopaPnCxType,xPagopaPnCxId,xPagopaPnCxGroups,limit,lastKey,serverWebExchange))
                .expectNext(ResponseEntity.ok().body(apiKeysResponseDto));
    }
}

