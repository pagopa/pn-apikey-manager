package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.service.ManageApiKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import org.junit.jupiter.api.Test;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ApiKeysPrvtController.class})
@ExtendWith(SpringExtension.class)
class ApiKeysPrvtControllerTest {

    @Autowired
    private ApiKeysPrvtController apiKeysPrvtController;

    @MockBean
    private ManageApiKeyService manageApiKeyService;

    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @MockBean
    ServerWebExchange serverWebExchange;
    @MockBean
    private PnAuditLogBuilder pnAuditLogBuilder;

    @MockBean
    private PnAuditLogEvent pnAuditLogEvent;

    @Test
    void testChangeVirtualKeyApiKey() {
        ApiKeysPrvtController apiKeysPrvtController = new ApiKeysPrvtController(manageApiKeyService, pnAuditLogBuilder, scheduler);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        String xPagopaPnCxId = "cxId";
        String virtualKey = "virtualKey";
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("cxId");
        List<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);
        when(pnAuditLogBuilder.before(any(),any())).thenReturn(pnAuditLogBuilder);
        when(pnAuditLogBuilder.build()).thenReturn(pnAuditLogEvent);
        when(manageApiKeyService.changeVirtualKey(any(),any())).thenReturn(Mono.just(apiKeyModelList));
        StepVerifier.create(apiKeysPrvtController.changeVirtualKeyApiKey(xPagopaPnCxId, virtualKey,
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().build());
    }
}
