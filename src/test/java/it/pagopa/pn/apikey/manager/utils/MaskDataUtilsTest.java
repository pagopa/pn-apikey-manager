package it.pagopa.pn.apikey.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MaskDataUtilsTest {
    /**
     * Method under test: {@link MaskDataUtils#maskInformation(String)}
     */
    @Test
    void testMaskInformation() {
        assertEquals("Data Buffered", MaskDataUtils.maskInformation("Data Buffered"));
        assertEquals("\"value\" : \"**\"", MaskDataUtils.maskInformation("\"value\" : \"UU\""));
        assertEquals("\"value\" : \"a***b-a***b\"", MaskDataUtils.maskInformation("\"value\" : \"aaaab-abbbb\""));
        assertEquals("\"value\" : \"\"", MaskDataUtils.maskInformation("\"value\" : \"\""));
        assertEquals("\"value\" : \"a****b-a****b-a****b\"", MaskDataUtils.maskInformation("\"value\" : \"aaaaab-abbbbb-abbbbb\""));
        assertEquals("\"value\" : \"*-**-***-a**b\"", MaskDataUtils.maskInformation("\"value\" : \"a-bb-ccc-aabb\""));

    }
}

