package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.AuthJwtIssuerModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyResponseDto;
import it.pagopa.pn.apikey.manager.repository.AuthJwtIssuerRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Slf4j
@Service
@RequiredArgsConstructor
@lombok.CustomLog
public class PublicKeyService {

    private final PublicKeyRepository publicKeyRepository;
    private final AuthJwtIssuerRepository authJwtIssuerRepository;
    private final PnAuditLogBuilder auditLogBuilder;
    private final PublicKeyValidator validator;

    public Mono<PublicKeyResponseDto> createPublicKey(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, Mono<PublicKeyRequestDto> publicKeyRequestDto, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole) {
        if (!ApiKeyConstant.ALLOWED_CX_TYPE_PUBLIC_KEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        return PublicKeyUtils.validaAccessoOnlyAdmin(xPagopaPnCxType, xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(item -> publicKeyRepository.findByCxIdAndStatus(xPagopaPnCxId, "ACTIVE").hasElements())
                .zipWith(publicKeyRequestDto)
                .flatMap(response -> Boolean.TRUE.equals(response.getT1()) ? Mono.error(new ApiKeyManagerException("Public key with status ACTIVE already exists.", HttpStatus.BAD_REQUEST))
                        : createNewPublicKey(xPagopaPnUid, xPagopaPnCxId, response.getT2()))
                .flatMap(publicKeyRepository::save)
                .flatMap(model -> {
                    model.setKid(model.getKid()+"_COPY");
                    model.setStatus(null);
                    model.setTtl(model.getExpireAt());
                    return publicKeyRepository.save(model);
                })
                .flatMap(model -> {
                    AuthJwtIssuerModel authJwtIssuer = createAuthJwtIssuerFromPublicKey(model);
                    authJwtIssuerRepository.save(authJwtIssuer);
                    return Mono.just(model);
                })
                .flatMap(model -> {
                    PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
                    publicKeyResponseDto.setKid(model.getKid());
                    publicKeyResponseDto.setIssuer(model.getIssuer());
                    return Mono.just(publicKeyResponseDto);
                });
    }

    private AuthJwtIssuerModel createAuthJwtIssuerFromPublicKey(PublicKeyModel model) {
        AuthJwtIssuerModel.AttributeResolverCfg.Cfg cfg1 = new AuthJwtIssuerModel.AttributeResolverCfg.Cfg();
        cfg1.setKeyAttributeName("virtual_key");
        AuthJwtIssuerModel.AttributeResolverCfg.Cfg cfg2 = new AuthJwtIssuerModel.AttributeResolverCfg.Cfg();
        cfg2.setPurposes(Arrays.asList(AuthJwtIssuerModel.AttributeResolverCfg.Cfg.Purpose.values()));
        AuthJwtIssuerModel.AttributeResolverCfg attributeResolverCfg1 = new AuthJwtIssuerModel.AttributeResolverCfg();
        attributeResolverCfg1.setCfg(cfg1);
        attributeResolverCfg1.setName("DATABASE");
        AuthJwtIssuerModel.AttributeResolverCfg attributeResolverCfg2 = new AuthJwtIssuerModel.AttributeResolverCfg();
        attributeResolverCfg1.setCfg(cfg2);
        attributeResolverCfg1.setName("PGCUSTOM");

        AuthJwtIssuerModel authJwtIssuerModel = new AuthJwtIssuerModel();
        //TODO chiedere
        authJwtIssuerModel.setHashKey("ISS~TA-issuer.dev.notifichedigitali.it");
        authJwtIssuerModel.setSortKey("CFG");
        authJwtIssuerModel.setAttributeResolversCfgs(List.of(attributeResolverCfg1, attributeResolverCfg2));
        authJwtIssuerModel.setIss(model.getIssuer());
        //TODO chiedere
        authJwtIssuerModel.setJwksCacheExpireSlot(Instant.now().plus(1, ChronoUnit.DAYS));
        authJwtIssuerModel.setJwksCacheMaxDurationSec(172800);
        //TODO chiedere
        authJwtIssuerModel.setJwksCacheOriginalExpireEpochSeconds(Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond());
        authJwtIssuerModel.setJwksCacheRenewSec(300);
        //FIXME
        authJwtIssuerModel.setJwksUrl("s3://<pn-jwks-cache-bucket-name>/jwks_pg_entries/iss_<x-pagopa-pn-cx-id>_jwks.json");
        authJwtIssuerModel.setModificationTimeEpochMs(Instant.now().toEpochMilli());
        authJwtIssuerModel.setCxId(model.getCxId());
        return authJwtIssuerModel;
    }

    private Mono<PublicKeyModel> createNewPublicKey(String xPagopaPnUid, String xPagopaPnCxId, PublicKeyRequestDto publicKeyRequestDto) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(UUID.randomUUID().toString());
        model.setName(publicKeyRequestDto.getName());
        model.setPublicKey(publicKeyRequestDto.getPublicKey());
        model.setExpireAt(Instant.now().plus(355, ChronoUnit.DAYS));
        model.setCreatedAt(Instant.now());
        model.setStatus("ACTIVE");
        model.setCxId(xPagopaPnCxId);
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setChangeByDenomination(xPagopaPnUid);
        statusHistoryItem.setStatus("CREATED");
        statusHistoryItem.setDate(Instant.now());
        model.setStatusHistory(List.of(statusHistoryItem));
        model.setIssuer(xPagopaPnCxId);
        return Mono.just(model);
    }
}
