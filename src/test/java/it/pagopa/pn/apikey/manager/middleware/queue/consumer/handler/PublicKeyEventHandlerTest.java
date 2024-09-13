package it.pagopa.pn.apikey.manager.middleware.queue.consumer.handler;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class PublicKeyEventHandlerTest {

    @Mock
    private PublicKeyService publicKeyService;

    @Mock
    private Message<PublicKeyEvent.Payload> message;

    @InjectMocks
    private PublicKeyEventHandler publicKeyEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleMessageSuccessfully() {
        // Arrange
        PublicKeyEvent.Payload event = PublicKeyEvent.Payload.builder()
                .cxId("test-cx-id")
                .action("test-action")
                .build();

        when(message.getPayload()).thenReturn(event);
        when(publicKeyService.handlePublicKeyEvent(event.getCxId())).thenReturn(Mono.empty());

        // Act
        publicKeyEventHandler.pnPublicKeyEventInboundConsumer().accept(message);

        // Assert
        verify(publicKeyService, times(1)).handlePublicKeyEvent(event.getCxId());
    }

    @Test
    void shouldHandleMessageWithDifferentCxId() {
        // Arrange
        PublicKeyEvent.Payload event = PublicKeyEvent.Payload.builder()
                .cxId("test-cx-id")
                .action("test-action")
                .build();
        when(message.getPayload()).thenReturn(event);
        when(publicKeyService.handlePublicKeyEvent(event.getCxId())).thenReturn(Mono.empty());

        // Act
        publicKeyEventHandler.pnPublicKeyEventInboundConsumer().accept(message);

        // Assert
        verify(publicKeyService, times(1)).handlePublicKeyEvent(event.getCxId());
    }

    @Test
    void shouldHandleMessageError() {
        // Arrange
        PublicKeyEvent.Payload event = PublicKeyEvent.Payload.builder()
                .cxId("test-cx-id")
                .action("test-action")
                .build();
        when(message.getPayload()).thenReturn(event);
        when(publicKeyService.handlePublicKeyEvent(event.getCxId())).thenReturn(Mono.error(new ApiKeyManagerException("No public keys found for cxId " + "test-cx-id", HttpStatus.NOT_FOUND)));

        // Act & Assert
        Assertions.assertThrows(ApiKeyManagerException.class, () -> publicKeyEventHandler.pnPublicKeyEventInboundConsumer().accept(message));
    }
}
