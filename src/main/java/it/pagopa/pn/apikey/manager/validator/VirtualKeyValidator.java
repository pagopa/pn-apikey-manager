package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyValidator {

    private final ApiKeyRepository apiKeyRepository;
    private final PublicKeyRepository publicKeyRepository;
    private final PnUserAttributesClient pnUserAttributesClient;
    private final PnExternalRegistriesClient pnExternalRegistriesClient;

    public Mono<Void> validateTosAndValidPublicKey(String xPagopaCxId, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxRole, List<String> groups) {
        return validateTosConsent(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxRole, groups)
                .then(publicKeyRepository.findByCxIdAndWithoutTtl(xPagopaCxId))
                .flatMap(publicKeys -> {
                    if (publicKeys.items().stream().noneMatch(elem -> "ACTIVE".equals(elem.getStatus()) || "ROTATED".equals(elem.getStatus()) || "BLOCKED".equals(elem.getStatus()))) {
                        return Mono.error(new ApiKeyManagerException(VALID_PUBLIC_KEY_NOT_FOUND, HttpStatus.FORBIDDEN));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateTosConsent(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxRole, List<String> groups) {
        return pnExternalRegistriesClient.findPrivacyNoticeVersion(ConsentTypeDto.TOS_DEST_B2B.getValue(), CxTypeAuthFleetDto.PG.getValue())
                .flatMap(versionDto -> pnUserAttributesClient.getPgConsentByType(xPagopaPnUid, xPagopaPnCxType.getValue(), xPagopaPnCxRole, ConsentTypeDto.TOS_DEST_B2B, groups, Integer.toString(versionDto.getVersion())))
                .flatMap(consentDto -> {
                    if (Boolean.TRUE.equals(consentDto.getAccepted())) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new ApiKeyManagerException(TOS_CONSENT_NOT_FOUND, HttpStatus.FORBIDDEN));
                    }
                });
    }
}
