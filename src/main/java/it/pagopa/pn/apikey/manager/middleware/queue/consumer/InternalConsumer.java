package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.model.PublicKeyEventAction;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.utils.MDCUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
@RequiredArgsConstructor
@CustomLog
public class InternalConsumer {
    private final PublicKeyService publicKeyService;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;
    private static final String HANDLER_REQUEST_JWKS = "pnPublicKeyEventInboundConsumer";
    private static final String HANDLER_REQUEST_DELETE = "pnPublicKeyTtlEventInboundConsumer";

    /**
     * Handles messages from the internal SQS queue.
     * @param message the incoming message containing PublicKeyEvent payload
     */
    @SqsListener(queueNames = "#{@pnApikeyManagerConfig.sqs.internalQueueName}")
    public void pnEventInboundInternalConsumer(Message<PublicKeyEvent.Payload> message) {
        log.info("Received message from internal queue: {}", message);
        PublicKeyEvent.Payload payload = message.getPayload();
        switch (PublicKeyEventAction.valueOf(payload.getAction())) {
            case JWKS:
                handleJwksEvent(message);
                break;
            case DELETE:
                handleDeleteEvent(message);
                break;
            default:
                throw new IllegalArgumentException("Unsupported action: " + payload.getAction());
        }
    }

    private void handleDeleteEvent(Message<PublicKeyEvent.Payload> message) {
        var monoResult = publicKeyService.handlePublicKeyTtlEvent(message)
                .doOnSuccess(unused -> log.logEndingProcess(HANDLER_REQUEST_DELETE))
                .doOnError(throwable ->  {
                    log.logEndingProcess(HANDLER_REQUEST_DELETE, false, throwable.getMessage());
                    HandleEventUtils.handleException(message.getHeaders(), throwable);
                });
        MDCUtils.addMDCToContextAndExecute(monoResult).block();
    }

    private void handleJwksEvent(Message<PublicKeyEvent.Payload> message) {
        var monoResult = publicKeyService.handlePublicKeyEvent(message.getPayload().getCxId())
                .doOnSuccess(unused -> {
                    MDC.remove(MDCUtils.MDC_CX_ID_KEY);
                    log.logEndingProcess(HANDLER_REQUEST_JWKS);
                })
                .doOnError(throwable -> {
                    log.logEndingProcess(HANDLER_REQUEST_JWKS, false, throwable.getMessage());
                    MDC.remove(MDCUtils.MDC_CX_ID_KEY);
                    HandleEventUtils.handleException(message.getHeaders(), throwable);
                });
        MDCUtils.addMDCToContextAndExecute(monoResult).block();
    }
}
