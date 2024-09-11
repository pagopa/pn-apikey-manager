package it.pagopa.pn.apikey.manager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PublicKeyUtilsTest {

    @Test
    void testCreateJWKFromData() {
        String n = "modulus";
        String e = "exponent";
        String kid = "keyId";
        String alg = "RS256";

        Map<String, Object> expectedJwk = new HashMap<>();
        expectedJwk.put("kty", "RSA");
        expectedJwk.put("n", n);
        expectedJwk.put("e", e);
        expectedJwk.put("kid", kid);
        expectedJwk.put("alg", alg);
        expectedJwk.put("use", "sig");

        Map<String, Object> actualJwk = PublicKeyUtils.createJWKFromData(n, e, kid, alg);

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