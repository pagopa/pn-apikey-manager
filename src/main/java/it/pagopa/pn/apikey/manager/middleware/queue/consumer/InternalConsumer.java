package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.model.PublicKeyEventAction;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.apikey.manager.utils.ConsumerMDCUtils;
import it.pagopa.pn.commons.utils.MDCUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@CustomLog
public class InternalConsumer {
    private final PublicKeyService publicKeyService;
    private static final String HANDLER_REQUEST_JWKS = "pnPublicKeyEventInboundConsumer";
    private static final String HANDLER_REQUEST_DELETE = "pnPublicKeyTtlEventInboundConsumer";

    /**
     * Handles messages from the internal SQS queue.
     * @param message the incoming message containing PublicKeyEvent payload
     */
    @SqsListener(value = "${pn.apikey.manager.sqs.internalQueueName}")
    public void pnEventInboundInternalConsumer(Message<PublicKeyEvent.Payload> message) {
        log.info("Received message from internal queue: {}", message);
        ConsumerMDCUtils.addMessageHeadersToMDC(message.getHeaders());
        PublicKeyEvent.Payload payload = message.getPayload();
        MDC.put(MDCUtils.MDC_CX_ID_KEY, message.getPayload().getCxId());
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
                .doOnSuccess(unused -> {
                    log.logEndingProcess(HANDLER_REQUEST_DELETE);
                    MDC.remove(MDCUtils.MDC_CX_ID_KEY);
                })
                .doOnError(throwable ->  {
                    log.logEndingProcess(HANDLER_REQUEST_DELETE, false, throwable.getMessage());
                    MDC.remove(MDCUtils.MDC_CX_ID_KEY);
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
