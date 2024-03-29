package it.pagopa.pn.apikey.manager.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.service.CreateApiKeyService;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;

import java.util.ArrayList;
import java.util.List;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @MockBean
    private PnAuditLogBuilder auditLogBuilder;

    @MockBean
    private PnAuditLogEvent pnAuditLogEvent;

    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @MockBean
    ServerWebExchange serverWebExchange;

    @ParameterizedTest
    @EnumSource(RequestApiKeyStatusDto.StatusEnum.class)
    void testChangeStatusApiKey(RequestApiKeyStatusDto.StatusEnum status) {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        when(manageApiKeyService.changeStatus(any(), any(), any(), eq(CxTypeAuthFleetDto.PA), any(), any()))
                .thenReturn(Mono.just(new ApiKeyModel()));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, auditLogBuilder, scheduler);
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        RequestApiKeyStatusDto requestApiKeyStatusDto = new RequestApiKeyStatusDto();
        requestApiKeyStatusDto.setStatus(status);
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);
        StepVerifier.create(apiKeysController.changeStatusApiKey("foo", CxTypeAuthFleetDto.PA, "foo", "foo", Mono.just(requestApiKeyStatusDto), xPagopaPnCxGroups,
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().build());
    }


    @Test
    void testDeleteApiKeys() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        when(manageApiKeyService.deleteApiKey(any(), eq(CxTypeAuthFleetDto.PA))).thenReturn(Mono.just("id"));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, auditLogBuilder, scheduler);
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);
        StepVerifier.create(apiKeysController.deleteApiKeys("foo", CxTypeAuthFleetDto.PA, "foo", "foo", xPagopaPnCxGroups,
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().build());
    }


    @Test
    void testNewApiKey() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(), any())).thenReturn(null);

        ResponseNewApiKeyDto apiKeyModel = new ResponseNewApiKeyDto();
        apiKeyModel.setApiKey("");
        apiKeyModel.setId("");

        when(createApiKeyService.createApiKey(eq("uid"), eq(CxTypeAuthFleetDto.PA), eq("cxId"), any(), any()))
                .thenReturn(Mono.just(apiKeyModel));
        ApiKeysController apiKeysController = new ApiKeysController(manageApiKeyService, createApiKeyService, auditLogBuilder, scheduler);
        RequestNewApiKeyDto requestNewApiKeyDto = new RequestNewApiKeyDto();
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        ResponseEntity<ResponseNewApiKeyDto> responseEntity = ResponseEntity.ok().body(apiKeyModel);
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);
        StepVerifier.create(apiKeysController.newApiKey("uid", CxTypeAuthFleetDto.PA, "cxId", Mono.just(requestNewApiKeyDto), xPagopaPnCxGroups,
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(responseEntity);
    }

    @Test
    void testGetApiKeys() {
        String xPagopaPnUid = "PA-test-1";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PA;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();
        apiKeysResponseDto.setItems(apiKeyRowDtos);
        apiKeysResponseDto.setLastKey(lastKey);
        apiKeysResponseDto.setLastUpdate(lastUpdate);
        when(manageApiKeyService.getApiKeyList(anyString(), any(), any(), any(), any(), anyBoolean(), eq(xPagopaPnCxType)))
                .thenReturn(Mono.just(apiKeysResponseDto));
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);
        StepVerifier.create(apiKeysController.getApiKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, 10, lastKey, lastUpdate, showVirtualKey, serverWebExchange))
                .expectNext(ResponseEntity.ok().body(apiKeysResponseDto));
    }

}
