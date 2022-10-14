package it.pagopa.pn.apikey.manager.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.model.Problem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.time.OffsetDateTime;

@Configuration
@Order(-2)
@Slf4j
public class PnWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PnWebExceptionHandler() {
        objectMapper.findAndRegisterModules();
        objectMapper
                .configOverride(OffsetDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange serverWebExchange, @NonNull Throwable throwable) {
        DataBuffer dataBuffer = null;
        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        Problem problem;

        log.error("Error -> {}", throwable.getMessage());
        problem = handleException(throwable);

        problem.setTraceId(MDC.get("trace_id"));
        problem.setTimestamp(OffsetDateTime.now());
        serverWebExchange.getResponse().setStatusCode(HttpStatus.resolve(problem.getStatus()));

        try {
            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(problem));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        assert dataBuffer != null;
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    public Problem handleException(Throwable ex) {
       /* if (!(ex instanceof IPnException)) {
            ex = new PnInternalException("Errore generico", "PN_GENERIC_ERROR", ex);
        }

        Problem res = ((IPnException)ex).getProblem();
        if (res.getStatus() >= 500) {
            log.error("pn-exception " + res.getStatus() + " catched problem={}", res, ex);
        } else {
            log.warn("pn-exception " + res.getStatus() + " catched problem={}", res, ex);
        }

        return res;*/
        return new Problem();
    }
}
