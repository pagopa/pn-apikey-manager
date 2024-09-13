package it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.commons.utils.MDCUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class PublicKeyEventHandler {

    private final PublicKeyService publicKeyService;
    private static final String HANDLER_REQUEST = "pnPublicKeyEventInboundConsumer";

    @Bean
    public Consumer<Message<PublicKeyEvent.Payload>> pnPublicKeyEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", PnLogger.EXTERNAL_SERVICES.AMAZON, message);
            PublicKeyEvent.Payload response = message.getPayload();
            MDC.put(MDCUtils.MDC_CX_ID_KEY, response.getCxId());

            Mono<Void> handledMessage = publicKeyService.handlePublicKeyEvent(response.getCxId())
                    .doOnSuccess(unused -> {
                        MDC.remove(MDCUtils.MDC_CX_ID_KEY);
                        log.logEndingProcess(HANDLER_REQUEST);
                    })
                    .doOnError(throwable -> {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        MDC.remove(MDCUtils.MDC_CX_ID_KEY);
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    });

            MDCUtils.addMDCToContextAndExecute(handledMessage).block();

        };
    }
}
