package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<Void> changeStatus(String kid, String status, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(Mono.defer(() -> checkIfExistsActivePublicKey(xPagopaPnCxId, status)))
                .then(Mono.defer(() -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId)))
                .flatMap(publicKeyModel -> validator.validateChangeStatus(publicKeyModel, status))
                .flatMap(publicKeyModel -> updatePublicKeyStatus(publicKeyModel, status, xPagopaPnUid));
    }

    private Mono<Void> checkIfExistsActivePublicKey(String xPagoPaCxId, String status) {
        return status.equals(ENABLE_OPERATION)
                ? validator.checkPublicKeyAlreadyExistsWithStatus(xPagoPaCxId, PublicKeyStatusDto.ACTIVE.name())
                : Mono.empty();
    }

    @NotNull
    private Mono<Void> updatePublicKeyStatus(PublicKeyModel publicKeyModel, String status, String xPagopaPnUid) {
            String decodedStatus = decodeStatus(status);
            publicKeyModel.setStatus(decodedStatus);
            publicKeyModel.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, decodedStatus));
            return publicKeyRepository.save(publicKeyModel)
                .then();
    }

    private String decodeStatus(String status) {
        return status.equals(ENABLE_OPERATION) ? PublicKeyStatusDto.ACTIVE.name() : PublicKeyStatusDto.BLOCKED.name();
    }

    private PublicKeyModel.StatusHistoryItem createNewHistoryItem(String xPagopaPnUid, String status) {
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus(status);
        statusHistoryItem.setDate(Instant.now());
        return statusHistoryItem;
    }
}
