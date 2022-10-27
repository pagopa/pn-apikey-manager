package it.pagopa.pn.apikey.manager.exception;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PnWebExceptionHandlerTest {
    @InjectMocks
    private PnWebExceptionHandler pnWebExceptionHandler;

    @Mock
    ServerWebExchange serverWebExchange;

    @Mock
    ServerHttpResponse serverHttpResponse;

    @Mock
    ServerHttpRequest serverHttpRequest;

    @Mock
    DataBufferFactory dataBufferFactory;

    @Mock
    DataBuffer dataBuffer;

    /**
     * Method under test: {@link PnWebExceptionHandler#handle(ServerWebExchange, Throwable)}
     */
    @Test
    void testHandle(){
        Problem problem = new Problem();
        problem.setStatus(400);
        Throwable throwable = new Throwable();
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(serverHttpResponse.bufferFactory()).thenReturn(dataBufferFactory);
        when(serverHttpResponse.writeWith(any())).thenReturn(Mono.empty());
        when(serverHttpResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(dataBufferFactory.wrap((byte[]) any())).thenReturn(dataBuffer);
        StepVerifier.create(pnWebExceptionHandler.handle(serverWebExchange,throwable)).verifyComplete();

    }

    /**
     * Method under test: {@link PnWebExceptionHandler#handle(ServerWebExchange, Throwable)}
     */
    @Test
    void testHandle2() {

        WebClientResponseException exception = mock(WebClientResponseException.class);
        when(exception.getMessage()).thenReturn("bad request");
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(serverHttpResponse.bufferFactory()).thenReturn(dataBufferFactory);
        when(serverHttpResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(dataBufferFactory.wrap((byte[]) any())).thenReturn(dataBuffer);
        when(serverHttpResponse.writeWith(any())).thenReturn(Mono.empty());
        Problem problem = new Problem();
        problem.setStatus(400);
        StepVerifier.create(pnWebExceptionHandler.handle(serverWebExchange,exception)).verifyComplete();

    }

    /**
     * Method under test: {@link PnWebExceptionHandler#handle(ServerWebExchange, Throwable)}
     */
    @Test
    void testHandle5() {
        ApiKeyManagerException exception = mock(ApiKeyManagerException.class);
        when(exception.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getMessage()).thenReturn("bad request");
        when(serverWebExchange.getResponse()).thenReturn(serverHttpResponse);
        when(serverHttpResponse.bufferFactory()).thenReturn(dataBufferFactory);
        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(serverHttpResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(dataBufferFactory.wrap((byte[]) any())).thenReturn(dataBuffer);
        when(serverHttpResponse.writeWith(any())).thenReturn(Mono.empty());
        Problem problem = new Problem();
        problem.setStatus(400);
        StepVerifier.create(pnWebExceptionHandler.handle(serverWebExchange,exception)).verifyComplete();

    }

}

