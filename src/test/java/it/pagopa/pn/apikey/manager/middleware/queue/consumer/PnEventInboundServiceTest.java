package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PnEventInboundServiceTest {
    private PnEventInboundService pnEventInboundService;
    private EventHandler eventHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        eventHandler = mock(EventHandler.class);
        objectMapper = new ObjectMapper();
        PnApikeyManagerConfig pnApikeyManagerConfig = new PnApikeyManagerConfig();
        PnApikeyManagerConfig.Sqs sqs = new PnApikeyManagerConfig.Sqs();
        sqs.setInternalQueueName("pn-apikey_manager_internal_queue");
        pnApikeyManagerConfig.setSqs(sqs);
        pnEventInboundService = new PnEventInboundService(eventHandler, objectMapper, pnApikeyManagerConfig);
    }
    @Test
    void customRouterDeleteEventValid() throws JsonProcessingException {
        MessageHeaders headers = new MessageHeaders(Map.of("aws_receivedQueue", "pn-apikey_manager_internal_queue", "aws_messageId", "messageId", "X-Amzn-Trace-Id", "traceId"));
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<String> message = MessageBuilder.createMessage(objectMapper.writeValueAsString(payload), headers);
        when(eventHandler.getHandler()).thenReturn(Map.of("DELETE", "it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler.PublicKeyTtlEventHandler"));

        MessageRoutingCallback.FunctionRoutingResult result = pnEventInboundService.customRouter().routingResult(message);

        assertNotNull(result);
    }

    @Test
    void customRouterJwksEventValid() throws JsonProcessingException {
        MessageHeaders headers = new MessageHeaders(Map.of("aws_receivedQueue", "pn-apikey_manager_internal_queue", "aws_messageId", "messageId", "X-Amzn-Trace-Id", "traceId"));
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("JWKS").build();
        Message<String> message = MessageBuilder.createMessage(objectMapper.writeValueAsString(payload), headers);
        when(eventHandler.getHandler()).thenReturn(Map.of("JWKS", "it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler.PublicKeyEventHandler"));

        MessageRoutingCallback.FunctionRoutingResult result = pnEventInboundService.customRouter().routingResult(message);

        assertNotNull(result);
    }

    @Test
    void customRouterNotAJson() {
        MessageHeaders headers = new MessageHeaders(Map.of("aws_receivedQueue", "pn-apikey_manager_internal_queue"));
        Message<String> message = MessageBuilder.createMessage("TestString", headers);
        when(eventHandler.getHandler()).thenReturn(Map.of("JWKS", "it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler.PublicKeyEventHandler"));

        MessageRoutingCallback router = pnEventInboundService.customRouter();
        assertThrows(PnInternalException.class, () -> router.routingResult(message));
    }

    @Test
    void customRouterEventTypeNonPresent() throws JsonProcessingException {
        MessageHeaders headers = new MessageHeaders(Map.of("aws_receivedQueue", "pn-apikey_manager_internal_queue", "aws_messageId", "messageId", "X-Amzn-Trace-Id", "traceId"));
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("TEST").build();
        Message<String> message = MessageBuilder.createMessage(objectMapper.writeValueAsString(payload), headers);
        when(eventHandler.getHandler()).thenReturn(Map.of("JWKS", "it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler.PublicKeyEventHandler"));

        MessageRoutingCallback router = pnEventInboundService.customRouter();
        assertThrows(PnInternalException.class, () -> router.routingResult(message));
    }

    @Test
    void customRouterQueueNameError() throws JsonProcessingException {
        MessageHeaders headers = new MessageHeaders(Map.of("aws_receivedQueue", "testQueue", "aws_messageId", "messageId", "X-Amzn-Trace-Id", "traceId"));
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("TEST").build();
        Message<String> message = MessageBuilder.createMessage(objectMapper.writeValueAsString(payload), headers);
        when(eventHandler.getHandler()).thenReturn(Map.of("JWKS", "it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler.PublicKeyEventHandler"));
        MessageRoutingCallback router = pnEventInboundService.customRouter();
        assertThrows(PnInternalException.class, () -> router.routingResult(message));
    }
}