package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.CustomLog;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@CustomLog
@NoArgsConstructor(access = AccessLevel.NONE)
public class PublicKeyUtils {

    public static final Set<String> ALLOWED_ROLES = Set.of("ADMIN");

    /**
     * Effettua la validazione dell'accesso per le Persone Giuridiche su risorse accessibili solo dagli amministratori.
     *
     * @param pnCxType   tipo utente (PF, PG, PA)
     * @param pnCxRole   ruolo (admin, operator)
     * @param pnCxGroups gruppi
     */
    public static Mono<Void> validaAccessoOnlyAdmin(CxTypeAuthFleetDto pnCxType, String pnCxRole, List<String> pnCxGroups) {
        String process = "validating access admin only";
        log.logChecking(process);
        if (!CxTypeAuthFleetDto.PG.name().equals(pnCxType.getValue())
                || (pnCxRole == null || !ALLOWED_ROLES.contains(pnCxRole.toUpperCase()) || !CollectionUtils.isEmpty(pnCxGroups))) {

            log.logCheckingOutcome(process, false, "only a PG admin can access this resource");
            return Mono.error(new ApiKeyManagerException(ApiKeyManagerExceptionError.ACCESS_DENIED , HttpStatus.FORBIDDEN));
        }
        log.debug("access granted for {}, role: {}, groups: {}", pnCxType, pnCxRole, pnCxGroups);
        log.logCheckingOutcome(process, true);
        return Mono.empty();
    }
}
