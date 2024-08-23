package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private PublicKeyValidator validator = new PublicKeyValidator();
    private PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        publicKeyService = new PublicKeyService(publicKeyRepository, auditLogBuilder, validator);
    }

    @Test
    void handlePublicKeyEventSuccessfully() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().minusSeconds(60));
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatus("DELETE");

        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.updateItemStatus(any(), anyList())).thenReturn(Mono.just(publicKeyModel));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventKeyNotFound() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.error(new RuntimeException("Key not found")));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Key not found"))
                .verify();
    }

    @Test
    void handlePublicKeyEventKeyNotExpired() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().plusSeconds(60));
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatus("PENDING");

        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.updateItemStatus(any(), anyList())).thenReturn(Mono.just(publicKeyModel));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventWithoutKidAndCxId() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("").cxId("").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("The key or cxid is empty."))
                .verify();
    }

    @Test
    void handlePublicKeyEventInvalidAction() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("RESUME").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("The status is empty or not valid."))
                .verify();
    }
}