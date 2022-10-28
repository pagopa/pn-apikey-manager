package it.pagopa.pn.apikey.manager.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Paths;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    @InjectMocks
    private RequestResponseLoggingFilter requestResponseLoggingFilter;

    /**
     * Method under test: {@link RequestResponseLoggingFilter#filter(ServerWebExchange, WebFilterChain)}
     */
    @Test
    void testFilter() {
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getURI())
                .thenReturn(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri());
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        DefaultServerWebExchange exchange = new DefaultServerWebExchange(serverHttpRequestDecorator, response,
                webSessionManager, codecConfigurer, new AcceptHeaderLocaleContextResolver());

        WebFilterChain webFilterChain = mock(WebFilterChain.class);
        when(webFilterChain.filter(any())).thenReturn(Mono.empty());
        StepVerifier.create(requestResponseLoggingFilter.filter(exchange, webFilterChain)).verifyComplete();
    }

    /**
     * Method under test: {@link RequestResponseLoggingFilter#filter(ServerWebExchange, WebFilterChain)}
     */
    @Test
    void testFilter2() {
        ServerHttpRequestDecorator serverHttpRequestDecorator = mock(ServerHttpRequestDecorator.class);
        when(serverHttpRequestDecorator.getURI())
                .thenReturn(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri());
        when(serverHttpRequestDecorator.getHeaders()).thenReturn(new HttpHeaders());
        when(serverHttpRequestDecorator.getId()).thenReturn("https://example.org/example");
        WebSessionManager webSessionManager = mock(WebSessionManager.class);
        WebSession webSession = mock(WebSession.class);
        when(webSessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        MockServerHttpResponse response = new MockServerHttpResponse();
        DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        DefaultServerWebExchange exchange = new DefaultServerWebExchange(serverHttpRequestDecorator, response,
                webSessionManager, codecConfigurer, new AcceptHeaderLocaleContextResolver());

        WebFilterChain webFilterChain = mock(WebFilterChain.class);
        when(webFilterChain.filter(any())).thenReturn(Mono.empty());
       StepVerifier.create(requestResponseLoggingFilter.filter(exchange, webFilterChain)).verifyComplete();
    }
}

