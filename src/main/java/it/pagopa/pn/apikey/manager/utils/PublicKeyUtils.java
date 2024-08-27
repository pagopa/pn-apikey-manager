package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRequestDto;
import lombok.CustomLog;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@CustomLog
public class PublicKeyUtils {

    public static final Set<String> ALLOWED_ROLES = Set.of("ADMIN");

    /**
     * Effettua la validazione dell'accesso per le Persone Giuridiche su risorse accessibili solo dagli amministratori.
     *
     * @param pnCxType   tipo utente (PF, PG, PA)
     * @param pnCxRole   ruolo (admin, operator)
     * @param pnCxGroups gruppi
     * @param dto
     */
    public static Mono<PublicKeyRequestDto> validaAccessoOnlyAdmin(CxTypeAuthFleetDto pnCxType, String pnCxRole, List<String> pnCxGroups, PublicKeyRequestDto dto) {
        String process = "validating access admin only";
        log.logChecking(process);
        if (CxTypeAuthFleetDto.PG.name().equals(pnCxType.getValue())
                && (pnCxRole == null || !ALLOWED_ROLES.contains(pnCxRole.toUpperCase()) || !CollectionUtils.isEmpty(pnCxGroups))) {

            log.logCheckingOutcome(process, false, "only a PG admin can access this resource");
            return Mono.error(new PnForbiddenException());
        }
        log.debug("access granted for {}, role: {}, groups: {}", pnCxType, pnCxRole, pnCxGroups);
        log.logCheckingOutcome(process, true);
        return Mono.just(dto);
    }
}
