package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    @MockBean
    private PnAuditLogBuilder auditLogBuilder;
    @MockBean
    private PnAuditLogEvent auditLogEvent;

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        auditLogBuilder = mock(PnAuditLogBuilder.class);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder);
    }

    @Test
    void handlePublicKeyEventSuccessfully() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().minusSeconds(60));
        publicKeyModel.setStatus("ACTIVE");
        PnAuditLogEvent successLogEvent = mock(PnAuditLogEvent.class);

        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.changeStatus(any(PublicKeyModel.class))).thenReturn(Mono.just(publicKeyModel));

        Message<PublicKeyEvent.Payload> message = mock(Message.class);
        PublicKeyEvent.Payload payload = mock(PublicKeyEvent.Payload.class);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getKid()).thenReturn("kid");
        when(payload.getCxId()).thenReturn("cxId");
        when(auditLogBuilder.before(any(), any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess()).thenReturn(successLogEvent);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyEvent(message);

        StepVerifier.create(result)
                .expectNextMatches(model -> "DELETED".equals(model.getStatus()))
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventKeyNotFound() {
        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.error(new RuntimeException("Key not found")));

        Message<PublicKeyEvent.Payload> message = mock(Message.class);
        PublicKeyEvent.Payload payload = mock(PublicKeyEvent.Payload.class);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getKid()).thenReturn("kid");
        when(payload.getCxId()).thenReturn("cxId");
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(auditLogEvent);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyEvent(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventKeyNotExpired() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().plusSeconds(60));
        publicKeyModel.setStatus("ACTIVE");
        PnAuditLogEvent failureLogEvent = mock(PnAuditLogEvent.class);

        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.just(publicKeyModel));

        Message<PublicKeyEvent.Payload> message = mock(Message.class);
        PublicKeyEvent.Payload payload = mock(PublicKeyEvent.Payload.class);
        when(message.getPayload()).thenReturn(payload);
        when(payload.getKid()).thenReturn("kid");
        when(payload.getCxId()).thenReturn("cxId");
        when(auditLogBuilder.before(any(),any())).thenReturn(auditLogBuilder);
        when(auditLogBuilder.build()).thenReturn(auditLogEvent);
        when(auditLogEvent.generateFailure(any(), any())).thenReturn(failureLogEvent);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Key is not expired"))
                .verify();
    }
}