package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyPageable;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PublicKeyConverter publicKeyConverter;

    public Mono<PublicKeysResponseDto> getPublicKeys(CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole,
                                                     Integer limit, String lastKey, String createdAt, Boolean showPublicKey) {
        PublicKeyPageable pageable = toPublicKeyPageable(limit, lastKey, createdAt);
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(Mono.defer(() -> publicKeyRepository.getAllWithFilterPaginated(xPagopaPnCxId, pageable, new ArrayList<>())))
                .map(page -> publicKeyConverter.convertResponseToDto(page, showPublicKey))
                .zipWhen(page -> publicKeyRepository.countWithFilters(xPagopaPnCxId))
                .doOnNext(tuple -> tuple.getT1().setTotal(tuple.getT2()))
                .map(Tuple2::getT1);
    }

    private PublicKeyPageable toPublicKeyPageable(Integer limit, String lastKey, String createdAt) {
        return PublicKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastKey)
                .createdAt(createdAt)
                .build();
    }
}
