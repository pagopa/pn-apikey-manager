package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.apikey.manager.TestUtils.VALID_PUBLIC_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RSAModulusExtractorTest {

    @Test
    void testSuccessExtractionSpki() {
        String spkiKey = "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBvMt1uroX5K8p+nQBbOlEY\n" +
                "h/VWfe+r9x68DRp2sUJSmcC9jdku8DA1lrPH8p3MiYozjwBAlcTUPYJo6iBMjP34\n" +
                "HqQQMV1ENqzg0m1yQ4P9ZnmWbhTFFY5Op9mURG7r05h23aeiAL3tCs+Kw19eYv4B\n" +
                "XosbSaePKlXP+5Fjsfb2ljMLKjgIuWMwRzagpad07LyV6paNfe2M3LhlC1Pzoecl\n" +
                "vRPAK2NDNZzaPe4Uo+R5PNjyXqIikjV3T56ez3DNLhO2itYYH0sqRhhEwxGPpoQw\n" +
                "XTHttUUA764xG1EuefidprYZUEViSmZ74tLkwLgPAWgV2Xooz7DuqtmLxp9r1vYl\n" +
                "AgMBAAE=";
        String modulus = RSAModulusExtractor.extractModulus(spkiKey);
        String expectedModulus = "bzLdbq6F-SvKfp0AWzpRGIf1Vn3vq_cevA0adrFCUpnAvY3ZLvAwNZazx_KdzImKM48AQJXE1D2CaOogTIz9-B6kEDFdRDas4NJtckOD_WZ5lm4UxRWOTqfZlERu69OYdt2nogC97QrPisNfXmL-AV6LG0mnjypVz_uRY7H29pYzCyo4CLljMEc2oKWndOy8leqWjX3tjNy4ZQtT86HnJb0TwCtjQzWc2j3uFKPkeTzY8l6iIpI1d0-ens9wzS4TtorWGB9LKkYYRMMRj6aEMF0x7bVFAO-uMRtRLnn4naa2GVBFYkpme-LS5MC4DwFoFdl6KM-w7qrZi8afa9b2JQ";
        assertEquals(expectedModulus, modulus);
    }

    @Test
    void testSuccessExtractionPkcs() {
        String modulus = RSAModulusExtractor.extractModulus(VALID_PUBLIC_KEY);
        String expectedModulus = "m4Y-SQaygmOUU_TRCfNDHnYFsBdERf4X65fgAOd1cnC9118xqg6lI9d8MmNk_YXj9jv5ByvLfdcUsc_frA3gzQ";
        assertEquals(expectedModulus, modulus);
    }

    @Test
    void testFailExtraction() {
        String invalidKey = "invalid_key";
        assertThrows(ApiKeyManagerException.class, () -> RSAModulusExtractor.extractModulus(invalidKey));
    }
}