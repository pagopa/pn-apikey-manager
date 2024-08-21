package it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.commons.utils.MDCUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicKeyTtlEventHandlerTest {

    private PublicKeyTtlEventHandler handler;
    private PublicKeyService publicKeyService;

    @BeforeEach
    void setUp() {
        publicKeyService = mock(PublicKeyService.class);
        handler = new PublicKeyTtlEventHandler(publicKeyService);
    }

    @Test
    void handleMessageSuccessfully() {
        Message<PublicKeyEvent.Payload> message = mock(Message.class);
        PublicKeyEvent.Payload payload = mock(PublicKeyEvent.Payload.class);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getCxId()).thenReturn("cxId");
        when(publicKeyService.handlePublicKeyEvent(any(Message.class))).thenReturn(Mono.empty());

        Consumer<Message<PublicKeyEvent.Payload>> consumer = handler.pnPublicKeyTtlEventInboundConsumer();
        consumer.accept(message);

        verify(publicKeyService, times(1)).handlePublicKeyEvent(message);
        assertEquals("cxId", MDC.get(MDCUtils.MDC_CX_ID_KEY));
    }

    @Test
    void handleMessageWithError() {
        Message<PublicKeyEvent.Payload> message = mock(Message.class);
        PublicKeyEvent.Payload publicKeyEventPayload = new PublicKeyEvent.Payload();
        when(message.getPayload()).thenReturn(publicKeyEventPayload);
        when(publicKeyService.handlePublicKeyEvent(any())).thenReturn(Mono.error(mock(PnRuntimeException.class)));

        Assertions.assertThrows(PnRuntimeException.class, () -> handler.pnPublicKeyTtlEventInboundConsumer().accept(message));
    }
}