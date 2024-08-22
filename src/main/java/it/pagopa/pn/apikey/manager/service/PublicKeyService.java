package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
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

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository repository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyConverter converter;
    private final PublicKeyValidator validator;

    public Mono<PublicKeyModel> handlePublicKeyEvent(Message<PublicKeyEvent.Payload> message) {

        PnAuditLogEvent logEvent = this.logMessage(message);

        return Mono.just(message.getPayload())
                .flatMap(validator::validatePayload)
                .flatMap(converter::convertPayloadToModel)
                .flatMap(repository::findByKidAndCxId)
                .flatMap(validator::validateModel)
                .flatMap(publicKeyModel -> {
                    publicKeyModel.setStatus("DELETED");
                    return repository.changeStatus(publicKeyModel);
                })
                .doOnNext(responsePdndDto -> logEvent.generateSuccess().log())
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }

    public PnAuditLogEvent logMessage(Message<PublicKeyEvent.Payload> message) {
        String logMessage = String.format("Cambio stato in DELETED per la public Key - kid=%s - cxid=%s",
                message.getPayload().getKid(),
                message.getPayload().getCxId());

        PnAuditLogEvent logEvent = auditLogBuilder
                .before(PnAuditLogEventType.AUD_AK_VIEW, logMessage)
                .build();

        logEvent.log();
        return logEvent;
    }

}
