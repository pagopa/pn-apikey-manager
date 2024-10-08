package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;
import static it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto.StatusEnum.ENABLE;
import static it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils.decodeToEntityStatus;

@Component
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyValidator {

    private final ApiKeyRepository apiKeyRepository;
    private final PublicKeyRepository publicKeyRepository;
    private final PnUserAttributesClient pnUserAttributesClient;
    private final PnExternalRegistriesClient pnExternalRegistriesClient;

    public Mono<Void> validateCxType(CxTypeAuthFleetDto xPagopaPnCxType) {
        if (!Objects.equals(CxTypeAuthFleetDto.PG.toString(), xPagopaPnCxType.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType.getValue()), HttpStatus.FORBIDDEN));
        }
        return Mono.empty();
    }

    public Mono<Void> checkVirtualKeyAlreadyExistsWithStatus(String xPagopaPnUid, String xPagopaPnCxId, String status) {
        return apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, status, ApiKeyModel.Scope.CLIENTID.toString())
                .flatMap(existingKeys -> {
                    if (!existingKeys.items().isEmpty()) {
                        return Mono.error(new ApiKeyManagerException(String.format(SAME_STATUS_APIKEY_ALREADY_EXISTS, status), HttpStatus.CONFLICT));
                    }
                    return Mono.empty();
                });
    }

    public Mono<ApiKeyModel> checkCxIdAndUid(String xPagopaPnCxId, String xPagopaPnCxUid, ApiKeyModel apiKey, RequestVirtualKeyStatusDto.StatusEnum statusEnum) {
        log.info("Checking CxId for virtualKey - id={}, cxId={}, uid={}", apiKey.getId(), apiKey.getCxId(), apiKey.getUid());
        if (ENABLE.equals(statusEnum)) {
            String blockedBy = apiKey.getStatusHistory().stream()
                    .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                    .filter(apiKeyHistoryModel -> apiKeyHistoryModel.getStatus().equals(ApiKeyStatusDto.BLOCKED.toString()))
                    .findFirst()
                    .map(ApiKeyHistoryModel::getChangeByDenomination)
                    .orElse(null);
            if (StringUtils.hasText(blockedBy) && !blockedBy.equalsIgnoreCase(xPagopaPnCxUid)) {
                return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_OPERATION_FOR_NON_ADMIN, HttpStatus.FORBIDDEN));
            }
        }
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId()) || !Objects.equals(xPagopaPnCxUid, apiKey.getUid())) {
            return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_OPERATION, HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> validateRotateVirtualKey(ApiKeyModel apiKey) {
        log.info("Checking status for virtualKey - id={}, status={}", apiKey.getId(), apiKey.getStatus());
        if (!Objects.equals(apiKey.getStatus(), ApiKeyStatusDto.ENABLED.toString())) {
            return Mono.error(new ApiKeyManagerException(
                        String.format(VIRTUALKEY_INVALID_STATUS, apiKey.getStatus(), ApiKeyStatusDto.ROTATED.getValue()),
                        HttpStatus.CONFLICT
                    )
            );
        }
        return Mono.just(apiKey);
    }

    public Mono<ApiKeyModel> validateRoleForDeletion(ApiKeyModel virtualKeyModel, String xPagopaPnUid, String xPagopaCxId,String xPagopaPnCxRole, List<String> xPagopaPnCxGroups) {
        log.debug("validateRoleForDeletion - xPagopaPnUid: {}, xPagopaPnCxRole: {}, xPagopaPnCxGroups: {}", xPagopaPnUid, xPagopaPnCxRole, xPagopaPnCxGroups);
        return VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(isAdmin -> {
                    if((isAdmin && virtualKeyModel.getCxId().equals(xPagopaCxId)) || virtualKeyModel.getUid().equals(xPagopaPnUid)) {
                        return Mono.just(virtualKeyModel);
                    }
                    return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_OPERATION, HttpStatus.FORBIDDEN));
                });
    }

    public Mono<ApiKeyModel> isDeleteOperationAllowed(ApiKeyModel virtualKeyModel) {
        VirtualKeyStatusDto status = VirtualKeyStatusDto.fromValue(virtualKeyModel.getStatus());
        if (!status.getValue().equals(VirtualKeyStatusDto.BLOCKED.getValue()) && !status.getValue().equals(VirtualKeyStatusDto.ROTATED.getValue())) {
            return Mono.error(new ApiKeyManagerException(String.format(VIRTUALKEY_INVALID_STATUS, virtualKeyModel.getStatus(), VirtualKeyStatusDto.DELETED.getValue()), HttpStatus.CONFLICT));
        }
        return Mono.just(virtualKeyModel);
    }

    public Mono<Void> validateTosAndValidPublicKey(String xPagopaCxId, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxRole, List<String> groups) {
        return validateTosConsent(xPagopaCxId, xPagopaPnCxType, xPagopaPnCxRole, groups)
                .then(publicKeyRepository.findByCxIdAndWithoutTtl(xPagopaCxId))
                .flatMap(publicKeys -> {
                    if (publicKeys.items().stream().noneMatch(elem -> "ACTIVE".equals(elem.getStatus()) || "ROTATED".equals(elem.getStatus()) || "BLOCKED".equals(elem.getStatus()))) {
                        return Mono.error(new ApiKeyManagerException(VALID_PUBLIC_KEY_NOT_FOUND, HttpStatus.FORBIDDEN));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateTosConsent(String xPagopaCxId, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxRole, List<String> groups) {
        return pnExternalRegistriesClient.findPrivacyNoticeVersion(ConsentTypeDto.TOS_DEST_B2B.getValue(), CxTypeAuthFleetDto.PG.getValue())
                .flatMap(versionDto -> pnUserAttributesClient.getPgConsentByType(xPagopaCxId, xPagopaPnCxType.getValue(), xPagopaPnCxRole, ConsentTypeDto.TOS_DEST_B2B, groups, Integer.toString(versionDto.getVersion())))
                .flatMap(consentDto -> {
                    if (Boolean.TRUE.equals(consentDto.getAccepted())) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new ApiKeyManagerException(TOS_CONSENT_NOT_FOUND, HttpStatus.FORBIDDEN));
                    }
                });
    }

    public Mono<ApiKeyModel> checkCxId(String xPagopaPnCxId, ApiKeyModel apiKey) {
        if (!Objects.equals(xPagopaPnCxId, apiKey.getCxId())) {
            return Mono.error(new ApiKeyManagerException(APIKEY_FORBIDDEN_OPERATION, HttpStatus.FORBIDDEN));
        }
        return Mono.just(apiKey);
    }

    public Mono<Void> validateStateTransition(ApiKeyModel apiKeyModel, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        if (requestVirtualKeyStatusDto.getStatus().equals(RequestVirtualKeyStatusDto.StatusEnum.ENABLE) && apiKeyModel.getStatus().equals(ApiKeyStatusDto.BLOCKED.toString()) ||
                (requestVirtualKeyStatusDto.getStatus().equals(RequestVirtualKeyStatusDto.StatusEnum.BLOCK) && apiKeyModel.getStatus().equals(ApiKeyStatusDto.ENABLED.toString())))
        {
            return Mono.empty();
        }
        return Mono.error(new ApiKeyManagerException(String.format(VIRTUALKEY_INVALID_STATUS, apiKeyModel.getStatus(), decodeToEntityStatus(requestVirtualKeyStatusDto.getStatus())), HttpStatus.CONFLICT));
    }
}
