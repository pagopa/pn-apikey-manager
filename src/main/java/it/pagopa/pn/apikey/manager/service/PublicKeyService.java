package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
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

    public Mono<PublicKeyResponseDto> rotatePublicKey(Mono<PublicKeyRequestDto> publicKeyRequestDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        Mono<PublicKeyRequestDto> cachedPublicKeyRequestDto = publicKeyRequestDto.cache();

        return cachedPublicKeyRequestDto
                .flatMap(validator::validatePublicKeyRequest)
                .flatMap(model -> PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups))
                .then(Mono.defer(() -> validator.validateRotatedKeyAlreadyExists(xPagopaPnCxId)))
                .flatMap(valid -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId))
                .flatMap(validator::validatePublicKeyRotation)
                .flatMap(model -> rotatePublicKeyAndSave(xPagopaPnUid, model))
                .flatMap(unused -> cachedPublicKeyRequestDto)
                .flatMap(requestDto -> createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, requestDto.getPublicKey(), requestDto.getName()))
                .flatMap(publicKeyRepository::save)
                .zipWhen(this::savePublicKeyCopy)
                .map(tuple -> {
                    PublicKeyModel originalPublicKeyModel = tuple.getT1();
                    PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
                    publicKeyResponseDto.setKid(originalPublicKeyModel.getKid());
                    publicKeyResponseDto.setIssuer(originalPublicKeyModel.getIssuer());
                    return publicKeyResponseDto;
                });
    }

    private Mono<PublicKeyModel> rotatePublicKeyAndSave(String xPagopaPnUid, PublicKeyModel model) {
        model.setStatus(PublicKeyStatusDto.ROTATED.getValue());
        model.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, PublicKeyStatusDto.ROTATED.getValue()));
        return publicKeyRepository.save(model);
    }

    private Mono<PublicKeyModel> createNewPublicKey(String xPagopaPnUid, String xPagopaPnCxId, String publicKey, String name) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(generateNewKid(publicKey, name));
        model.setName(name);
        model.setPublicKey(publicKey);
        model.setExpireAt(Instant.now().plus(360, ChronoUnit.DAYS));
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

    private Mono<PublicKeyModel> savePublicKeyCopy(PublicKeyModel originalModel) {
        PublicKeyModel copy = new PublicKeyModel(originalModel);
        copy.setKid(originalModel.getKid() + "_COPY");
        copy.setStatus(null);
        copy.setStatusHistory(null);
        copy.setTtl(originalModel.getExpireAt());
        return publicKeyRepository.save(copy);
    }
}
