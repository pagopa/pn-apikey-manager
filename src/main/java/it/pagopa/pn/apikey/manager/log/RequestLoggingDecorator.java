package it.pagopa.pn.apikey.manager.log;

import it.pagopa.pn.apikey.manager.utils.MaskDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

import static it.pagopa.pn.apikey.manager.log.RequestResponseLoggingFilter.LOG_REQUEST_BODY;

@Slf4j
public class RequestLoggingDecorator extends ServerHttpRequestDecorator {

    private final StringBuilder body = new StringBuilder();

    public RequestLoggingDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public @NotNull Flux<DataBuffer> getBody() {
        return super.getBody()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::capture)
                .doOnComplete(() -> {
                    var maskedBody = MaskDataUtils.maskInformation(getCapturedBody());
                    log.info(LOG_REQUEST_BODY, getMethod(), getURI(), maskedBody);
                });
    }

    public String getCapturedBody() {
        return body.toString();
    }

    private void capture(DataBuffer buffer) {
        var decoded = StandardCharsets.UTF_8.decode(buffer.asByteBuffer().asReadOnlyBuffer());
        body.append(decoded);
    }
}
