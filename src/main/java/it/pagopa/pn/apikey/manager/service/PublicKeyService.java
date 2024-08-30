package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyPageable;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PublicKeyConverter publicKeyConverter;

    public Mono<PublicKeysResponseDto> getPublicKeys(CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole,
                                                     Integer limit, String lastKey, String createdAt, Boolean showPublicKey) {
        if (!PublicKeyConstant.ALLOWED_CX_TYPE_PUBLIC_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        PublicKeyPageable pageable = toPublicKeyPageable(limit, lastKey, createdAt);
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(item -> publicKeyRepository.getAllPaginated(xPagopaPnCxId, pageable, new ArrayList<>()))
                .map(page -> publicKeyConverter.convertResponseToDto(page, showPublicKey));
    }

    private PublicKeyPageable toPublicKeyPageable(Integer limit, String lastKey, String createdAt) {
        return PublicKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastKey)
                .createdAt(createdAt)
                .build();
    }
}
