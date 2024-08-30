package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<String> deletePublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        if (!ApiKeyConstant.ALLOWED_CX_TYPE_PUBLIC_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }

        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(valid -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException("Public key not found", HttpStatus.NOT_FOUND)))
                .flatMap(validator::validateDeletePublicKey)
                .doOnNext(model -> {
                    ArrayList<PublicKeyModel.StatusHistoryItem> history = new ArrayList<>(model.getStatusHistory());
                    model.setStatus(PublicKeyStatusDto.DELETED.getValue());
                    history.add(createNewHistoryItem(xPagopaPnUid, PublicKeyStatusDto.DELETED.getValue()));
                    model.setStatusHistory(history);
                    publicKeyRepository.save(model);
                })
                .thenReturn("Public key deleted");
    }

    private PublicKeyModel.StatusHistoryItem createNewHistoryItem(String xPagopaPnUid, String status) {
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus(status);
        statusHistoryItem.setDate(Instant.now());
        return statusHistoryItem;
    }
}
