package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.RequestPdndDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponseApiKeysDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ResponsePdndDto;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
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
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ApiKeyBoController.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiKeyBoControllerTest {

    @MockBean
    private ManageApiKeyService manageApiKeyService;


    @Autowired
    private ApiKeyBoController apiKeyBoController;

    @MockBean
    private PnAuditLogBuilder auditLogBuilder;

    @MockBean
    private PnAuditLogEvent pnAuditLogEvent;

    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @Test
    void testChangePdnd() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(), any())).thenReturn(null);

        ApiKeyBoController apiKeyBoController = new ApiKeyBoController(manageApiKeyService, auditLogBuilder, scheduler);

        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();


        RequestPdndDto apiKeyRequestPdndDto = new RequestPdndDto();
        ApiPdndDto apiPdndDto = new ApiPdndDto();
        apiPdndDto.setId("id");
        apiPdndDto.setPdnd(true);
        List<ApiPdndDto> apiPdndDtos = new ArrayList<>();
        apiPdndDtos.add(apiPdndDto);
        apiKeyRequestPdndDto.setItems(apiPdndDtos);

        ResponsePdndDto apiKeyResponsePdndDto = new ResponsePdndDto();
        apiKeyResponsePdndDto.setUnprocessedKey(apiPdndDtos.stream().map(ApiPdndDto::getId).toList());

        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);

        when(manageApiKeyService.changePdnd(any())).thenReturn(Mono.just(apiKeyResponsePdndDto));

        StepVerifier.create(apiKeyBoController.changePdnd(apiKeyRequestPdndDto,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }

    @Test
    void testGetApiKeys() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(), any())).thenReturn(null);

        ApiKeyBoController apiKeyBoController = new ApiKeyBoController(manageApiKeyService, auditLogBuilder, scheduler);

        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        ResponseApiKeysDto responseApiKeysDto = new ResponseApiKeysDto();
        responseApiKeysDto.setItems(new ArrayList<>());

        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(pnAuditLogEvent);

        when(manageApiKeyService.getBoApiKeyList(any())).thenReturn(Mono.just(responseApiKeysDto));

        StepVerifier.create(apiKeyBoController.getBoApiKeys("id",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }
}
