package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;

    public Mono<Object> handlePublicKeyEvent(Message<PublicKeyEvent> message) {
        return Mono.empty();
    }
}
