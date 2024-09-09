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
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<String> deletePublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(Mono.defer(() -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId)))
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

    public Mono<Void> changeStatus(String kid, String status, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(Mono.defer(() -> validator.checkPublicKeyAlreadyExistsWithStatus(xPagopaPnCxId, decodeToEntityStatus(status))))
                .then(Mono.defer(() -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId)))
                .flatMap(publicKeyModel -> validator.validateChangeStatus(publicKeyModel, status))
                .flatMap(publicKeyModel -> updatePublicKeyStatus(publicKeyModel, status, xPagopaPnUid))
                .then();
    }

    @NotNull
    private Mono<PublicKeyModel> updatePublicKeyStatus(PublicKeyModel publicKeyModel, String status, String xPagopaPnUid) {
        String decodedStatus = decodeToEntityStatus(status);
        publicKeyModel.setStatus(decodedStatus);
        publicKeyModel.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, decodedStatus));
        return publicKeyRepository.save(publicKeyModel);
    }

    private String decodeToEntityStatus(String status) {
        return status.equals(ENABLE_OPERATION) ? PublicKeyStatusDto.ACTIVE.name() : PublicKeyStatusDto.BLOCKED.name();
    }

    public Mono<PublicKeyResponseDto> createPublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        Mono<PublicKeyRequestDto> cachedRequestDto = publicKeyRequestDto.cache();

        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .then(cachedRequestDto)
                .flatMap(validator::validatePublicKeyRequest)
                .flatMap(req -> validator.checkPublicKeyAlreadyExistsWithStatus(xPagopaPnCxId, PublicKeyStatusDto.ACTIVE.getValue()))
                .onErrorMap(isPublicKeyAlreadyExistsError(), e -> new ApiKeyManagerException("Public key with status ACTIVE already exists, to create a new public key use the rotate operation.", HttpStatus.CONFLICT))
                .then(cachedRequestDto)
                .flatMap(publicKeyRequestDto1 -> createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, publicKeyRequestDto1, null))
                .flatMap(publicKeyRepository::save)
                .zipWhen(this::savePublicKeyCopyItem)
                .map(tuple -> {
                    PublicKeyModel originalPublicKey = tuple.getT1();
                    return toDtoResponse(originalPublicKey);
                });
    }

    @NotNull
    private static Predicate<Throwable> isPublicKeyAlreadyExistsError() {
        return t -> t instanceof ApiKeyManagerException && ((ApiKeyManagerException) t).getStatus() == HttpStatus.CONFLICT;
    }

    private Mono<PublicKeyModel> createNewPublicKey(String xPagopaPnUid, String xPagopaPnCxId, PublicKeyRequestDto publicKeyRequestDto, String correlationId) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(UUID.randomUUID().toString());
        model.setName(publicKeyRequestDto.getName());
        model.setPublicKey(publicKeyRequestDto.getPublicKey());
        model.setExponent(publicKeyRequestDto.getExponent());
        model.setAlgorithm(publicKeyRequestDto.getAlgorithm().getValue());
        model.setExpireAt(Instant.now().plus(355, ChronoUnit.DAYS));
        model.setCreatedAt(Instant.now());
        model.setStatus(PublicKeyStatusDto.ACTIVE.getValue());
        model.setCxId(xPagopaPnCxId);
        model.setStatusHistory(List.of(createNewHistoryItem(xPagopaPnUid, PublicKeyStatusDto.CREATED.getValue())));
        model.setIssuer(xPagopaPnCxId);
        model.setCorrelationId(correlationId);
        return Mono.just(model);
    }

    private Mono<PublicKeyModel> savePublicKeyCopyItem(PublicKeyModel publicKeyModel) {
        PublicKeyModel copyItem = new PublicKeyModel(publicKeyModel);
        copyItem.setKid(publicKeyModel.getKid()+"_COPY");
        copyItem.setStatus(null);
        copyItem.setStatusHistory(null);
        copyItem.setTtl(publicKeyModel.getExpireAt());
        return publicKeyRepository.save(copyItem);
    }

    private PublicKeyResponseDto toDtoResponse(PublicKeyModel publicKeyModel) {
        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        publicKeyResponseDto.setKid(publicKeyModel.getKid());
        publicKeyResponseDto.setIssuer(publicKeyModel.getIssuer());
        return publicKeyResponseDto;
    }

    public Mono<PublicKeyResponseDto> rotatePublicKey(Mono<PublicKeyRequestDto> publicKeyRequestDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        Mono<PublicKeyRequestDto> cachedPublicKeyRequestDto = publicKeyRequestDto.cache();

        return cachedPublicKeyRequestDto
                .flatMap(validator::validatePublicKeyRequest)
                .flatMap(model -> PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups))
                .then(Mono.defer(() -> validator.checkPublicKeyAlreadyExistsWithStatus(xPagopaPnCxId, PublicKeyStatusDto.ROTATED.getValue())))
                .then(Mono.defer(() -> publicKeyRepository.findByKidAndCxId(kid, xPagopaPnCxId)))
                .zipWith(cachedPublicKeyRequestDto)
                .flatMap(tuple -> {
                    PublicKeyModel publicKeyModel = tuple.getT1();
                    PublicKeyRequestDto requestDto = tuple.getT2();
                    return validator.validatePublicKeyRotation(publicKeyModel, requestDto.getPublicKey());
                })
                .flatMap(model -> updatePublicKeyStatus(model, PublicKeyStatusDto.ROTATED.getValue(), xPagopaPnUid))
                .zipWith(cachedPublicKeyRequestDto)
                .flatMap(tuple -> {
                    PublicKeyModel rotatedKey = tuple.getT1();
                    PublicKeyRequestDto requestDto = tuple.getT2();
                    return createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, requestDto, rotatedKey.getKid());
                })
                .flatMap(publicKeyRepository::save)
                .zipWhen(this::savePublicKeyCopyItem)
                .map(tuple -> {
                    PublicKeyModel originalPublicKeyModel = tuple.getT1();
                    return toDtoResponse(originalPublicKeyModel);
                });
    }
}
