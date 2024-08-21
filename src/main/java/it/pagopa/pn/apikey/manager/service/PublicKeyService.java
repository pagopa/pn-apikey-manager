package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;

    public Mono<PublicKeyModel> handlePublicKeyEvent(Message<PublicKeyEvent.Payload> message) {
        String logMessage = String.format("Cambio stato in DELETED per la public Key - kid=%s - cxid=%s",
                message.getPayload().getKid(),
                message.getPayload().getCxId());

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();
        return Mono.just(message.getPayload())
                .flatMap(publicKeyModel -> publicKeyRepository.findByKidAndCxId(publicKeyModel.getKid(), publicKeyModel.getCxId())
                        .onErrorResume(throwable -> {
                            log.warn("can not find keys with key {} and cxid {}", publicKeyModel.getKid(), publicKeyModel.getCxId(), throwable);
                            return Mono.empty();
                        }))
                .flatMap(publicKeyModel -> {
                    if(Instant.now().isBefore(publicKeyModel.getExpireAt())) {
                        return Mono.error(new RuntimeException("Key is not expired"));
                    } else {
                        return Mono.just(publicKeyModel);
                    }
                })
                .flatMap(publicKeyModel -> {
                    publicKeyModel.setStatus("DELETED");
                    return publicKeyRepository.changeStatus(publicKeyModel);
                })
                .doOnNext(responsePdndDto -> logEvent.generateSuccess().log())
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }
}
