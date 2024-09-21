package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.FAILED_TO_CREATEJWKS_JSON;
import static it.pagopa.pn.apikey.manager.utils.RSAModulusExtractor.extractModulus;

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

    public static Map<String, Object> createJWKFromData(String key, String e, String kid, String alg) {
        // Create a JWK Map
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");   // Always RSA
        jwk.put("n", extractModulus(key));         // Base64Url encoded modulus (from DB)
        jwk.put("e", e);         // Base64Url encoded exponent (from DB)
        jwk.put("kid", kid);     // Key ID (from DB)
        jwk.put("alg", alg);     // Algorithm (from DB, e.g., "RS256")
        jwk.put("use", "sig");   // Key use is always "sig" for signature

        // Convert the JWK Map to JSON
        return jwk;
    }

    public static String createJWKSJson(List<Map<String, Object>> jwkMaps) {
        // Create a JWKS Map with "keys" field containing the list of JWKs
        Map<String, List<Map<String, Object>>> jwks = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        jwks.put("keys", jwkMaps);
        String jwksJson;

        try {
            jwksJson = objectMapper.writeValueAsString(jwks);
        } catch (JsonProcessingException e) {
            throw new ApiKeyManagerException(FAILED_TO_CREATEJWKS_JSON, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return jwksJson;
    }
}
