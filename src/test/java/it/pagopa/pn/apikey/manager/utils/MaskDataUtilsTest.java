package it.pagopa.pn.apikey.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaskDataUtilsTest {

    @Test
    void testMaskInformation() {
        assertEquals("Data Buffered", MaskDataUtils.maskInformation("Data Buffered"));
        assertEquals("\"virtualKey\" : \"U*\"", MaskDataUtils.maskInformation("\"virtualKey\" : \"UU\""));
    }
}

