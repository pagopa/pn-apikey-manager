package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.model.PublicKeyEventAction;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;

class InternalConsumerTest {

    private PublicKeyService publicKeyService;
    private InternalConsumer internalConsumer;

    @BeforeEach
    void setUp() {
        publicKeyService = Mockito.mock(PublicKeyService.class);
        internalConsumer = new InternalConsumer(publicKeyService);
    }

    @Test
    void testHandleJwksEvent() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action(PublicKeyEventAction.JWKS.name())
                .cxId("testCxId")
                .build();

        Message<PublicKeyEvent.Payload> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(payload);
        Mockito.when(publicKeyService.handlePublicKeyEvent(any())).thenReturn(Mono.empty());

        internalConsumer.pnEventInboundInternalConsumer(message);

        Mockito.verify(publicKeyService, Mockito.times(1)).handlePublicKeyEvent("testCxId");
        assertDoesNotThrow(() -> internalConsumer.pnEventInboundInternalConsumer(message));
    }

    @Test
    void testHandleDeleteEvent() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action(PublicKeyEventAction.DELETE.name())
                .build();

        Message<PublicKeyEvent.Payload> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(payload);
        Mockito.when(publicKeyService.handlePublicKeyTtlEvent(any())).thenReturn(Mono.empty());

        internalConsumer.pnEventInboundInternalConsumer(message);

        Mockito.verify(publicKeyService, Mockito.times(1)).handlePublicKeyTtlEvent(message);
        assertDoesNotThrow(() -> internalConsumer.pnEventInboundInternalConsumer(message));
    }

    @Test
    void testHandleJwksEvent_doOnError() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action(PublicKeyEventAction.JWKS.name())
                .cxId("testCxId")
                .build();

        Message<PublicKeyEvent.Payload> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(payload);
        Mockito.when(publicKeyService.handlePublicKeyEvent(any()))
                .thenReturn(Mono.error(new RuntimeException("JWKS error")));

        assertThrows(RuntimeException.class, () -> internalConsumer.pnEventInboundInternalConsumer(message));
        Mockito.verify(publicKeyService, Mockito.times(1)).handlePublicKeyEvent("testCxId");
    }

    @Test
    void testHandleDeleteEvent_doOnError() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action(PublicKeyEventAction.DELETE.name())
                .build();

        Message<PublicKeyEvent.Payload> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(payload);
        Mockito.when(publicKeyService.handlePublicKeyTtlEvent(any()))
                .thenReturn(Mono.error(new RuntimeException("DELETE error")));

        assertThrows(RuntimeException.class, () -> internalConsumer.pnEventInboundInternalConsumer(message));
        Mockito.verify(publicKeyService, Mockito.times(1)).handlePublicKeyTtlEvent(message);
    }

    @Test
    void testUnsupportedActionThrowsException() {
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder()
                .action("UNKNOWN_ACTION")
                .build();

        Message<PublicKeyEvent.Payload> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(payload);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> internalConsumer.pnEventInboundInternalConsumer(message)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            throw exception;
        });
        Mockito.verifyNoInteractions(publicKeyService);
    }

}