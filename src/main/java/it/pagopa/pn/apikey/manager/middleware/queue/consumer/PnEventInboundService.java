package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.MDCUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED;
import static it.pagopa.pn.apikey.manager.model.PublicKeyEventAction.DELETE;
import static it.pagopa.pn.apikey.manager.model.PublicKeyEventAction.JWKS;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PnEventInboundService {
    public static final String EVENT_TYPE_NOT_PRESENT_WITH_PARAMS = "eventType not present, cannot start scheduled action headers={} payload={}";
    public static final String EVENT_TYPE_NOT_PRESENT = "eventType not present, cannot start scheduled action";

    private final EventHandler eventHandler;
    private final ObjectMapper objectMapper;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public FunctionRoutingResult routingResult(Message<?> message) {
                MessageHeaders messageHeaders = message.getHeaders();

                String traceId = null;
                String messageId = null;

                if (messageHeaders.containsKey("aws_messageId"))
                    messageId = messageHeaders.get("aws_messageId", String.class);
                if (messageHeaders.containsKey("X-Amzn-Trace-Id"))
                    traceId = messageHeaders.get("X-Amzn-Trace-Id", String.class);

                traceId = Objects.requireNonNullElseGet(traceId, () -> "traceId:" + UUID.randomUUID());
                MDCUtils.clearMDCKeys();
                MDC.put(MDCUtils.MDC_TRACE_ID_KEY, traceId);
                MDC.put(MDCUtils.MDC_PN_CTX_MESSAGE_ID, messageId);
                return new FunctionRoutingResult(handleMessage(message));
            }
        };
    }

    private String handleMessage(Message<?> message) {
        log.debug("Message received from customRouter {}", message);
        String eventType = (String) message.getHeaders().get("eventType");
        log.info("Message received from customRouter with eventType = {}", eventType );

        if(!StringUtils.hasText(eventType)) {
            eventType = handleOtherEvent(message);
        }

        String handlerName = eventHandler.getHandler().get(eventType);
        if (!StringUtils.hasText(handlerName)) {
            log.error("undefined handler for eventType={}", eventType);
        }

        log.debug("Handler for eventType={} is {}", eventType, handlerName);

        return handlerName;
    }

    private String handleOtherEvent(Message<?> message) {
        String eventType;
        String queueName = (String) message.getHeaders().get("aws_receivedQueue");
        if (Objects.equals(queueName, pnApikeyManagerConfig.getSqs().getInternalQueueName())) {
            PublicKeyEvent.Payload payload = null;
            try {
                payload = this.objectMapper.readValue((String) message.getPayload(), PublicKeyEvent.Payload.class);
            } catch (JsonProcessingException e) {
                log.error(EVENT_TYPE_NOT_PRESENT_WITH_PARAMS, message.getHeaders(), message.getPayload());
                throw new PnInternalException(EVENT_TYPE_NOT_PRESENT, ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED);
            }

            if(JWKS.name().equalsIgnoreCase(payload.getAction())) {
                eventType = "JWKS_EVENTS";
            }
            else if (DELETE.name().equalsIgnoreCase(payload.getAction())) {
                eventType = "DELETE_EVENTS";
            }
            else {
                log.error("eventType not present, cannot start scheduled action headers={} payload={}", message.getHeaders(), message.getPayload());
                throw new PnInternalException(EVENT_TYPE_NOT_PRESENT, ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED);
            }
        }
        else {
            log.error("eventType not present, cannot start scheduled action headers={} payload={}", message.getHeaders(), message.getPayload());
            throw new PnInternalException(EVENT_TYPE_NOT_PRESENT, ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED);
        }
        return eventType;
    }
}
