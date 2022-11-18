package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.service.AggregationService;
import it.pagopa.pn.apikey.manager.service.PaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AggregationController.class})
@ExtendWith(SpringExtension.class)
class AggregationControllerTest {

    @Autowired
    private AggregationController aggregationController;

    @MockBean
    private AggregationService aggregationService;

    @MockBean
    private PaService paService;

    @Qualifier("apikeyManagerScheduler")
    @MockBean
    private Scheduler scheduler;

    @MockBean
    ServerWebExchange serverWebExchange;

    @Test
    void testGetAggregateList() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        AggregatesListResponseDto aggregatesListResponseDto = new AggregatesListResponseDto();
        aggregatesListResponseDto.setItems(new ArrayList<>());
        when(aggregationService.getAggregation(any(), any(), any(), any())).thenReturn(Mono.just(aggregatesListResponseDto));
        StepVerifier.create(aggregationController.getAggregatesList("name", 10, null, null,
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().body(aggregatesListResponseDto));

    }

    @Test
    void testMovePa() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        MovePaResponseDto dto = new MovePaResponseDto();
        dto.setUnprocessed(1);
        dto.setProcessed(1);
        when(paService.movePa(any(), any())).thenReturn(Mono.just(dto));
        StepVerifier.create(aggregationController.movePa("id", new AddPaListRequestDto(),
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().body(dto));
    }

    @Test
    void testDeleteApiKeys() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        when(aggregationService.deleteAggregate(any())).thenReturn(Mono.empty());
        StepVerifier.create(aggregationController.deleteApiKeys("id",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }

    @Test
    void testGetAssociablePa() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        AssociablePaResponseDto associablePaResponseDto = new AssociablePaResponseDto();
        associablePaResponseDto.setItems(new ArrayList<>());
        when(paService.getAssociablePa(any())).thenReturn(Mono.just(associablePaResponseDto));
        StepVerifier.create(aggregationController.getAssociablePa("name",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().build());
    }

    @Test
    void testAddPaListToAggregate() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        MovePaResponseDto dto = new MovePaResponseDto();
        dto.setUnprocessed(1);
        dto.setProcessed(1);
        when(paService.createNewPaAggregation(any(), any())).thenReturn(Mono.just(dto));
        StepVerifier.create(aggregationController.addPaListToAggregate("id", new AddPaListRequestDto(),
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().body(dto));
    }

    @Test
    void testGetAggregate() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        AggregateResponseDto dto = new AggregateResponseDto();
        dto.setId("id");
        when(aggregationService.getAggregate(any())).thenReturn(Mono.just(dto));
        StepVerifier.create(aggregationController.getAggregate("id",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().body(dto));
    }

    @Test
    void testGetPaAggregation() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        PaAggregateResponseDto dto = new PaAggregateResponseDto();
        dto.setTotal(0);
        dto.setItems(new ArrayList<>());
        when(aggregationService.getPaOfAggregate(any())).thenReturn(Mono.just(dto));
        StepVerifier.create(aggregationController.getPaAggregation("id",
                new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                        new AcceptHeaderLocaleContextResolver()))).expectNext(ResponseEntity.ok().body(dto));
    }

    @Test
    void testUpdateAggregate() {
        AggregationController aggregationController = new AggregationController(scheduler, aggregationService, paService);
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        SaveAggregateResponseDto dto = new SaveAggregateResponseDto();
        dto.setId("id");
        AggregateRequestDto requestDto = new AggregateRequestDto();
        when(aggregationService.updateAggregate(any(), any())).thenReturn(Mono.just(dto));
        StepVerifier.create(aggregationController.updateAggregate("id", requestDto,
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().body(dto));
    }

    @Test
    void testCreateAggregate() {
        AggregationController controller = new AggregationController(scheduler, aggregationService, paService);

        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

        SaveAggregateResponseDto dto = new SaveAggregateResponseDto();
        dto.setId("id");

        when(aggregationService.createAggregate(any())).thenReturn(Mono.just(dto));

        StepVerifier.create(controller.createAggregate(new AggregateRequestDto(),
                        new DefaultServerWebExchange(serverHttpRequestDecorator, response, webSessionManager, codecConfigurer,
                                new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().body(dto));
    }
}
