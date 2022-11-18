package it.pagopa.pn.apikey.manager.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanResponseDto;
import it.pagopa.pn.apikey.manager.service.UsagePlanService;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
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

@ContextConfiguration(classes = {UsagePlanController.class, UsagePlanService.class, PnApikeyManagerConfig.class})
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class UsagePlanControllerTest {

    @MockBean
    private UsagePlanService usagePlanService;

    @Autowired
    private UsagePlanController usagePlanController;

    /**
     * Method under test: {@link UsagePlanController#getUsagePlan(ServerWebExchange)}
     */
    @Test
    void testGetUsagePlan() {
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        UsagePlanResponseDto usagePlan = new UsagePlanResponseDto();
        usagePlan.setItems(new ArrayList<>());
        when(usagePlanService.getUsagePlanList()).thenReturn(Mono.just(usagePlan));
        StepVerifier.create(usagePlanController.getUsagePlan(new DefaultServerWebExchange(serverHttpRequestDecorator, response,
                        webSessionManager, codecConfigurer, new AcceptHeaderLocaleContextResolver())))
                .expectNext(ResponseEntity.ok().body(usagePlan));
    }
}
