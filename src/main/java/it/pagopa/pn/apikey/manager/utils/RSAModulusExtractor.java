package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.ERROR_EXTRACTING_MODULUS;

@Slf4j
public class RSAModulusExtractor {
    public static String extractModulus(String pemKey) {
        log.info("Extracting modulus from RSA public key {}", pemKey);

        pemKey = pemKey.replaceAll("\\s+", "").replaceAll("\\n", "");
        Security.addProvider(new BouncyCastleProvider());

        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(pemKey);
            BigInteger modulus;
            if (isSPKI(publicKeyBytes)) {
                modulus = getModulusFromSPKI(publicKeyBytes);
            } else {
                modulus = getModulusFromPkcs1Key(publicKeyBytes);
            }

            return encodeModulus(modulus);
        } catch (Exception e) {
            log.error("Error extracting modulus from RSA public key", e);
            throw new ApiKeyManagerException(ERROR_EXTRACTING_MODULUS, HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean isSPKI(byte[] decodedKey) {
        if (decodedKey[0] == 0x30) {
            // Look for the OID for RSA encryption
            byte[] rsaOid = {0x06, 0x09, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01};
            for (int i = 0; i < decodedKey.length - rsaOid.length; i++) {
                if (matchSubArray(decodedKey, i, rsaOid)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchSubArray(byte[] array, int startIndex, byte[] subArray) {
        if (startIndex + subArray.length > array.length) {
            return false;
        }
        for (int i = 0; i < subArray.length; i++) {
            if (array[startIndex + i] != subArray[i]) {
                return false;
            }
        }
        return true;
    }

    private static BigInteger getModulusFromSPKI(byte[] spkiBytes) throws Exception {
        log.info("Extracting modulus from SPKI key");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(spkiBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);
        RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
        return rsaPublicKeySpec.getModulus();
    }

    private static BigInteger getModulusFromPkcs1Key(byte[] publicKeyBytes) {
        log.info("Extracting modulus from PKCS1 key");
        // Usa ASN1InputStream di Bouncy Castle per analizzare la chiave
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(publicKeyBytes))) {
            // Legge il dato ASN.1 primitivo (la struttura PKCS#1)
            ASN1Primitive asn1Primitive = asn1InputStream.readObject();

            // Converte l'oggetto ASN.1 in una struttura RSAPublicKey
            org.bouncycastle.asn1.pkcs.RSAPublicKey rsaPublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(asn1Primitive);

            return rsaPublicKey.getModulus();
        } catch (IOException e) {
            log.error("Error extracting modulus from RSA public key", e);
            throw new ApiKeyManagerException(ERROR_EXTRACTING_MODULUS, HttpStatus.BAD_REQUEST);
        }
    }

    private static String encodeModulus(BigInteger modulus) {
        byte[] modulusBytes = modulus.toByteArray();
        int bitLength = modulus.bitLength();

        // Ensure the byte array length matches (bitLength + 7) // 8
        int expectedLength = (bitLength + 7) / 8;
        byte[] trimmedModulusBytes = new byte[expectedLength];

        if (modulusBytes.length == expectedLength) {
            trimmedModulusBytes = modulusBytes;
        } else if (modulusBytes.length == expectedLength + 1 && modulusBytes[0] == 0) {
            System.arraycopy(modulusBytes, 1, trimmedModulusBytes, 0, expectedLength);
        } else {
            throw new IllegalArgumentException("Unexpected modulus byte array length");
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(trimmedModulusBytes);
    }
}