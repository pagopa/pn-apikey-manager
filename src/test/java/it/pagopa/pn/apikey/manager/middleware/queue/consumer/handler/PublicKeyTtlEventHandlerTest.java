package it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
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
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyService.handlePublicKeyEvent(any())).thenReturn(Mono.empty());

        Consumer<Message<PublicKeyEvent.Payload>> consumer = handler.pnPublicKeyTtlEventInboundConsumer();
        consumer.accept(message);

        verify(publicKeyService, times(1)).handlePublicKeyEvent(message);
    }

    @Test
    void handleMessageWithError() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);
        when(publicKeyService.handlePublicKeyEvent(any())).thenReturn(Mono.error(mock(PnRuntimeException.class)));

        Assertions.assertThrows(PnRuntimeException.class, () -> handler.pnPublicKeyTtlEventInboundConsumer().accept(message));
    }
}