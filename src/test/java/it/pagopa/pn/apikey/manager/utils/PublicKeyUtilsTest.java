package it.pagopa.pn.apikey.manager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.constant.RoleConstant;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.apikey.manager.TestUtils.VALID_PUBLIC_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicKeyUtilsTest {

    @Test
    void validaAccessoOnlyAdmin_grantsAccessForAdmin() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, RoleConstant.ADMIN_ROLE, List.of());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonAdminRole() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, "OPERATOR", List.of());

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonPGType() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PF, RoleConstant.ADMIN_ROLE, List.of());

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonEmptyGroups() {
        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, RoleConstant.ADMIN_ROLE, List.of("group1"));

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNullRole() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, null, List.of());

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void testCreateJWKFromData() {
        String e = "exponent";
        String kid = "keyId";
        String alg = "RS256";

        Map<String, Object> expectedJwk = new HashMap<>();
        expectedJwk.put("kty", "RSA");
        expectedJwk.put("n", "m4Y-SQaygmOUU_TRCfNDHnYFsBdERf4X65fgAOd1cnC9118xqg6lI9d8MmNk_YXj9jv5ByvLfdcUsc_frA3gzQ");
        expectedJwk.put("e", e);
        expectedJwk.put("kid", kid);
        expectedJwk.put("alg", alg);
        expectedJwk.put("use", "sig");

        Map<String, Object> actualJwk = PublicKeyUtils.createJWKFromData(VALID_PUBLIC_KEY, e, kid);

        assertEquals(expectedJwk, actualJwk);
    }

    @Test
    void testCreateJWKSJson() {
        Map<String, Object> jwk1 = new HashMap<>();
        jwk1.put("kty", "RSA");
        jwk1.put("n", "modulus1");
        jwk1.put("e", "exponent1");
        jwk1.put("kid", "keyId1");
        jwk1.put("alg", "RS256");
        jwk1.put("use", "sig");

        Map<String, Object> jwk2 = new HashMap<>();
        jwk2.put("kty", "RSA");
        jwk2.put("n", "modulus2");
        jwk2.put("e", "exponent2");
        jwk2.put("kid", "keyId2");
        jwk2.put("alg", "RS256");
        jwk2.put("use", "sig");

        List<Map<String, Object>> jwkList = List.of(jwk1, jwk2);
        String expectedJson = "{\"keys\":[{\"kty\":\"RSA\",\"n\":\"modulus1\",\"e\":\"exponent1\",\"kid\":\"keyId1\",\"alg\":\"RS256\",\"use\":\"sig\"},{\"kty\":\"RSA\",\"n\":\"modulus2\",\"e\":\"exponent2\",\"kid\":\"keyId2\",\"alg\":\"RS256\",\"use\":\"sig\"}]}";
        String actualJson = PublicKeyUtils.createJWKSJson(jwkList);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Map<String, Object>>> expectedMap;
        try {
            expectedMap = objectMapper.readValue(expectedJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, List<Map<String, Object>>> actualMap;

        try {
            actualMap = objectMapper.readValue(actualJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedMap, actualMap);
    }

    @Test
    void testCreateJWKSJsonEmptyList() {
        List<Map<String, Object>> jwkList = List.of();
        String actualJson = PublicKeyUtils.createJWKSJson(jwkList);

        assertEquals("{\"keys\":[]}", actualJson);
    }
}