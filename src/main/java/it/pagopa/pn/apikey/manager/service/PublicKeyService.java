package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyPageable;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.CheckExceptionUtils;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;
import static it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto.ACTIVE;
import static it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto.ROTATED;
import static it.pagopa.pn.apikey.manager.utils.PublicKeyUtils.createJWKFromData;
import java.util.UUID;
import java.util.function.Predicate;

import static it.pagopa.pn.apikey.manager.constant.ApiKeyConstant.ENABLE_OPERATION;
import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.ACCESS_DENIED;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final LambdaService lambdaService;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;
    private final PublicKeyConverter publicKeyConverter;

    private static final String AUTOMATIC_DELETE = "AUTOMATIC_DELETE";

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
                .flatMap(publicKeyModel -> updatePublicKeyStatus(publicKeyModel, decodeToEntityStatus(status), xPagopaPnUid))
                .then();
    }

    @NotNull
    private Mono<PublicKeyModel> updatePublicKeyStatus(PublicKeyModel publicKeyModel, String status, String xPagopaPnUid) {
        publicKeyModel.setStatus(status);
        publicKeyModel.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, status));
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

    public Mono<PublicKeyModel> handlePublicKeyTtlEvent(Message<PublicKeyEvent.Payload> message) {
        PnAuditLogEvent logEvent = this.logMessage(message);

        PublicKeyEvent.Payload publicKeyEvent = message.getPayload();
        String kid = retrieveBaseKidFromPayload(publicKeyEvent.getKid());

        return validator.validatePayload(publicKeyEvent)
                .flatMap(payload -> publicKeyRepository.findByKidAndCxId(kid, publicKeyEvent.getCxId()))
                .flatMap(validator::checkItemExpiration)
                .flatMap(validator::checkIfItemIsNotAlreadyDeleted)
                .flatMap(publicKeyModel -> this.updatePublicKeyStatus(publicKeyModel, PublicKeyStatusDto.DELETED.name(), AUTOMATIC_DELETE))
                .doOnNext(item -> logEvent.generateSuccess().log())
                .doOnError(throwable -> CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, logEvent));
    }

    private String retrieveBaseKidFromPayload(String kid) {
        return kid.replace("_COPY", "");
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

    public Mono<PublicKeysIssuerResponseDto> getIssuer(String xPagopaPnCxId, CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!CxTypeAuthFleetDto.PG.name().equals(xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException(ACCESS_DENIED, HttpStatus.FORBIDDEN));
        }

        return publicKeyRepository.getIssuer(xPagopaPnCxId)
                .map(Page::items)
                .flatMap(publicKeyModels -> {
                    log.debug("publicKeyModels.size: {}", publicKeyModels.size());
                    if (publicKeyModels.isEmpty()) {
                        return Mono.just(setPublicKeysIssuerResponseDto(false, null));
                    }
                    boolean hasActiveOrRotated = publicKeyModels.stream()
                            .anyMatch(model -> PublicKeyStatusDto.ACTIVE.name().equals(model.getStatus())
                                    || PublicKeyStatusDto.ROTATED.name().equals(model.getStatus()));
                    PublicKeysIssuerResponseDto.IssuerStatusEnum status = hasActiveOrRotated
                            ? PublicKeysIssuerResponseDto.IssuerStatusEnum.ACTIVE
                            : PublicKeysIssuerResponseDto.IssuerStatusEnum.INACTIVE;
                    return Mono.just(setPublicKeysIssuerResponseDto(true, status));
                });
    }

    private PublicKeysIssuerResponseDto setPublicKeysIssuerResponseDto(boolean isPresent, PublicKeysIssuerResponseDto.IssuerStatusEnum status) {
        PublicKeysIssuerResponseDto publicKeysIssuerResponseDto = new PublicKeysIssuerResponseDto();
        publicKeysIssuerResponseDto.setIsPresent(isPresent);
        publicKeysIssuerResponseDto.setIssuerStatus(status);
        return publicKeysIssuerResponseDto;
    }

    public Mono<Void> handlePublicKeyEvent(String cxId) {
        return publicKeyRepository.findByCxIdAndStatus(cxId, null)
                .filter(publicKeyModel -> ACTIVE.getValue().equals(publicKeyModel.getStatus()) || ROTATED.getValue().equals(publicKeyModel.getStatus()))
                .map(publicKeyModel -> createJWKFromData(publicKeyModel.getPublicKey(), publicKeyModel.getExponent(), publicKeyModel.getKid(), publicKeyModel.getAlgorithm()))
                .collectList()
                .switchIfEmpty(Mono.just(new ArrayList<>()))
                .flatMap(jwks -> lambdaService.invokeLambda(pnApikeyManagerConfig.getLambdaName(), cxId, jwks))
                .then()
                .doOnError(e -> log.error("Error handling public key event", e));
    }
}
