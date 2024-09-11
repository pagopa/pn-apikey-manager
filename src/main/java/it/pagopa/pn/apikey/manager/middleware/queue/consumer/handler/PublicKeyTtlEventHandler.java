package it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.HandleEventUtils;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.utils.MDCUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@CustomLog
public class PublicKeyTtlEventHandler {
    private PublicKeyService publicKeyService;
    private static final String HANDLER_REQUEST = "pnPublicKeyTtlEventInboundConsumer";
    @Bean
    public Consumer<Message<PublicKeyEvent.Payload>> pnPublicKeyTtlEventInboundConsumer() {
        return message -> {
            log.debug("Handle message from {} with content {}", "pn-publicKey", message);
            MDC.put(MDCUtils.MDC_CX_ID_KEY, message.getPayload().getCxId());
            var monoResult = publicKeyService.handlePublicKeyTtlEvent(message)
                    .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST))
                    .doOnError(throwable ->  {
                        log.logEndingProcess(HANDLER_REQUEST, false, throwable.getMessage());
                        HandleEventUtils.handleException(message.getHeaders(), throwable);
                    });
            MDCUtils.addMDCToContextAndExecute(monoResult).block();
        };
    }
}
