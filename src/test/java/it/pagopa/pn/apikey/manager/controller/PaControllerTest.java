package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.GetPaResponseDto;
import it.pagopa.pn.apikey.manager.service.PaService;
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

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PaController.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaControllerTest {

    @MockBean
    private PaService paService;


    @Autowired
    private PaController paController;


    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @Test
    void testChangePdnd() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(null);
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient1 = mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedAsyncClient1.table(any(), any())).thenReturn(null);

        PaController pdndController = new PaController(paService, scheduler);

        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        GetPaResponseDto getPaResponseDto = new GetPaResponseDto();
        when(paService.getPa(any(),any(),any())).thenReturn(Mono.just(getPaResponseDto));


        StepVerifier.create(paController.getPa("",10,"",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }
}
