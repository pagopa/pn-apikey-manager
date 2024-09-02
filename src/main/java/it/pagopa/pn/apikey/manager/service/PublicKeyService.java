package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<PublicKeyResponseDto> createPublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        Mono<PublicKeyRequestDto> cachedRequestDto = publicKeyRequestDto.cache();

        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(cachedRequestDto)
                .flatMap(validator::validatePublicKeyRequest)
                .flatMap(item -> publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, PublicKeyStatusDto.ACTIVE.getValue()).hasElements())
                .zipWith(cachedRequestDto)
                .flatMap(response -> Boolean.TRUE.equals(response.getT1()) ? Mono.error(new ApiKeyManagerException("Public key with status ACTIVE already exists, to create a new public key use the rotate operation.", HttpStatus.BAD_REQUEST))
                        : createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, response.getT2()))
                .flatMap(publicKeyRepository::save)
                .zipWhen(this::savePublicKeyCopyItem)
                .map(tuple -> {
                    PublicKeyModel originalPublicKey = tuple.getT1();
                    PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
                    publicKeyResponseDto.setKid(originalPublicKey.getKid());
                    publicKeyResponseDto.setIssuer(originalPublicKey.getIssuer());
                    return publicKeyResponseDto;
                });
    }

    private Mono<PublicKeyModel> createNewPublicKey(String xPagopaPnUid, String xPagopaPnCxId, PublicKeyRequestDto publicKeyRequestDto) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(generateNewKid(publicKeyRequestDto.getPublicKey(), publicKeyRequestDto.getName()));
        model.setName(publicKeyRequestDto.getName());
        model.setPublicKey(publicKeyRequestDto.getPublicKey());
        model.setExpireAt(Instant.now().plus(355, ChronoUnit.DAYS));
        model.setCreatedAt(Instant.now());
        model.setStatus(PublicKeyStatusDto.ACTIVE.getValue());
        model.setCxId(xPagopaPnCxId);
        model.setStatusHistory(List.of(createNewHistoryItem(xPagopaPnUid, PublicKeyStatusDto.CREATED.getValue())));
        model.setIssuer(xPagopaPnCxId);
        return Mono.just(model);
    }

    private String generateNewKid(String publicKey, String name) {
        return UUID.nameUUIDFromBytes((publicKey+name).getBytes()).toString();
    }

    private PublicKeyModel.StatusHistoryItem createNewHistoryItem(String xPagopaPnUid, String status) {
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus(status);
        statusHistoryItem.setDate(Instant.now());
        return statusHistoryItem;
    }

    private Mono<PublicKeyModel> savePublicKeyCopyItem(PublicKeyModel publicKeyModel) {
        PublicKeyModel copyItem = new PublicKeyModel(publicKeyModel);
        copyItem.setKid(publicKeyModel.getKid()+"_COPY");
        copyItem.setStatus(null);
        copyItem.setStatusHistory(null);
        copyItem.setTtl(publicKeyModel.getExpireAt());
        return publicKeyRepository.save(copyItem);
    }

}
