package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository repository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    private static final String AUTOMATIC_DELETE = "AUTOMATIC_DELETE";

    public Mono<PublicKeyModel> handlePublicKeyTtlEvent(Message<PublicKeyEvent.Payload> message) {

        PnAuditLogEvent logEvent = this.logMessage(message);

        PublicKeyEvent.Payload publicKeyEvent = message.getPayload();
        validator.validatePayload(publicKeyEvent);
        String kid = retrieveBaseKidFromPayload(publicKeyEvent.getKid());

        return validator.validatePayload(publicKeyEvent)
                .flatMap(payload -> repository.findByKidAndCxId(kid, publicKeyEvent.getCxId()))
                .flatMap(validator::checkItemExpiration)
                .flatMap(publicKeyModel -> {
                    publicKeyModel.setStatus("DELETED"); //TODO: CHANGE WITH ENUM AFTER OPENAPI GENERATION
                    publicKeyModel.getStatusHistory().add(createNewPublicKeyHistory("DELETED", AUTOMATIC_DELETE)); //TODO: CHANGE WITH ENUM AFTER OPENAPI GENERATION
                    return repository.updateItemStatus(publicKeyModel, Collections.singletonList("DELETED")); //TODO: CHANGE WITH ENUM AFTER OPENAPI GENERATION
                })
                .doOnNext(item -> logEvent.generateSuccess().log())
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }

    private String retrieveBaseKidFromPayload(String kid) {
        return kid.replace("_COPY", "");
    }

    protected PublicKeyModel.StatusHistoryItem createNewPublicKeyHistory(String status, String changeByDenomination) {
        PublicKeyModel.StatusHistoryItem historyItem = new PublicKeyModel.StatusHistoryItem();
        historyItem.setDate(Instant.now());
        historyItem.setStatus(status);
        historyItem.setChangeByDenomination(changeByDenomination);
        return historyItem;
    }

    public PnAuditLogEvent logMessage(Message<PublicKeyEvent.Payload> message) {
        String logMessage = String.format("Copied Public Key with kid=%s - cxid=%s was deleted by TTL, start verify related publicKey to update status to DELETED",
                message.getPayload().getKid(),
                message.getPayload().getCxId());

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();
        return logEvent;
    }

}
