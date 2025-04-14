package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    @Qualifier("strictTransportSecurity")
    private WebFilter strictTransportSecurity;

    @Test
    void testStrictTransportSecurity() {
        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/").build()
        );

        strictTransportSecurity.filter(exchange, mockChain).block();

        String headerValue = exchange.getResponse().getHeaders().getFirst("Strict-Transport-Security");
        assertNotNull(headerValue, "L'intestazione Strict-Transport-Security non dovrebbe essere null");
        assertEquals("max-age=31536000; includeSubDomains; preload", headerValue,
                "Il valore dell'intestazione Strict-Transport-Security non è corretto");

        verify(mockChain, times(1)).filter(exchange);
    }
}