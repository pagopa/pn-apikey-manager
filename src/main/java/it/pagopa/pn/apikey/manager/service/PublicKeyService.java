package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
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

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<PublicKeyResponseDto> rotatePublicKey(Mono<PublicKeyRequestDto> publicKeyRequestDto, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String kid, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        //TODO kid is not used, remove it from the openapi in the branch PN-12373?

        if (!ApiKeyConstant.ALLOWED_CX_TYPE_PUBLIC_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }

        Mono<PublicKeyRequestDto> cachedRequestDto = publicKeyRequestDto.cache();

        return cachedRequestDto.flatMap(dto -> PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups, dto))
                .flatMap(validator::validatePublicKeyRequest)
                .flatMap(item -> publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, "ROTATED").hasElements())
                .zipWith(cachedRequestDto)
                .flatMap(response -> Boolean.TRUE.equals(response.getT1()) ? Mono.error(new ApiKeyManagerException("Public key with status ROTATED already exists.", HttpStatus.BAD_REQUEST))
                        : publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, "ACTIVE").next())
                .switchIfEmpty(Mono.error(new ApiKeyManagerException("Public key with status ACTIVE not found.", HttpStatus.NOT_FOUND)))
                .flatMap(model -> {
                    model.setStatus("ROTATED");
                    model.getStatusHistory().add(createNewHistoryItem(xPagopaPnUid, "ROTATED"));
                    publicKeyRepository.save(model);
                    return cachedRequestDto;
                })
                .flatMap(model -> createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, model))
                .flatMap(publicKeyRepository::save)
                .flatMap(model -> {
                    PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
                    publicKeyResponseDto.setKid(model.getKid());
                    publicKeyResponseDto.setIssuer(model.getIssuer());
                    return Mono.just(publicKeyResponseDto);
                });
    }

    private Mono<PublicKeyModel> createNewPublicKey(String xPagopaPnUid, String xPagopaPnCxId, PublicKeyRequestDto publicKeyRequestDto) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(generateNewKid(publicKeyRequestDto.getPublicKey(), publicKeyRequestDto.getName()));
        model.setName(publicKeyRequestDto.getName());
        model.setPublicKey(publicKeyRequestDto.getPublicKey());
        model.setExpireAt(Instant.now().plus(355, ChronoUnit.DAYS));
        model.setCreatedAt(Instant.now());
        model.setStatus("ACTIVE");
        model.setCxId(xPagopaPnCxId);
        model.setStatusHistory(List.of(createNewHistoryItem(xPagopaPnUid, "CREATED")));
        model.setIssuer(xPagopaPnCxId);
        return Mono.just(model);
    }

    private String generateNewKid(String publicKey, String name) {
        return UUID.nameUUIDFromBytes((publicKey+name).getBytes()).toString();
    }

    private PublicKeyModel.StatusHistoryItem createNewHistoryItem(String xPagopaPnUid, String created) {
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus(created);
        statusHistoryItem.setDate(Instant.now());
        return statusHistoryItem;
    }
}
