package it.pagopa.pn.apikey.manager.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.commons.exceptions.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static it.pagopa.pn.commons.utils.MDCUtils.MDC_TRACE_ID_KEY;

@Slf4j
@Order(-2)
@Configuration
@Import(ExceptionHelper.class)
public class PnWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ExceptionHelper exceptionHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int STATUS_500 = 500;

    public PnWebExceptionHandler(ExceptionHelper exceptionHelper) {
        this.exceptionHelper = exceptionHelper;
        objectMapper.findAndRegisterModules();
        objectMapper
                .configOverride(OffsetDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange serverWebExchange, @NonNull Throwable throwable) {
        DataBuffer dataBuffer;
        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        try {
            Problem problem;
            if (throwable instanceof ApiKeyManagerException exception) {
                problem = handleApiKeyException(exception);
            } else {
                problem = handleException(throwable);
            }

            if (problem.getStatus() >= STATUS_500) {
                log.error("Exception uri: {}, message: {}", serverWebExchange.getRequest().getURI(), throwable.getMessage(), throwable);
            } else {
                log.warn("Exception uri: {}, message: {}", serverWebExchange.getRequest().getURI(), throwable.getMessage(), throwable);
            }

            problem.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
            problem.setTimestamp(OffsetDateTime.now());
            serverWebExchange.getResponse().setStatusCode(HttpStatus.resolve(problem.getStatus()));

            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(problem));
        } catch (JsonProcessingException e) {
            log.error("cannot output problem", e);
            dataBuffer = bufferFactory.wrap(exceptionHelper.generateFallbackProblem().getBytes(StandardCharsets.UTF_8));
        }
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private Problem handleApiKeyException(ApiKeyManagerException exception) {
        Problem problem = new Problem();
        problem.setStatus(exception.getStatus().value());
        problem.setTitle("ERROR");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    private Problem handleException(Throwable throwable) {
        Problem problem = new Problem();
        problem.setTitle("ERROR");
        problem.setStatus(STATUS_500);
        problem.setDetail(throwable.getMessage());
        return problem;
    }
}
