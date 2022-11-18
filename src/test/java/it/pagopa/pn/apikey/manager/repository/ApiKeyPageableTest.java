package it.pagopa.pn.apikey.manager.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyPageableTest {

    /**
     * Method under test: {@link ApiKeyPageable#isPage()}
     */
    @Test
    void testIsPageable() {
        assertTrue(new ApiKeyPageable(1, "42", "lastUpdate").isPage());
        assertFalse(new ApiKeyPageable(1, "", "lastUpdate").isPage());
        assertFalse(new ApiKeyPageable(1, "42", "").isPage());
    }

    @Test
    void testHasLimit() {
        assertTrue(new ApiKeyPageable(1, null, null).hasLimit());
        assertFalse(new ApiKeyPageable(0, null, null).hasLimit());
        assertFalse(new ApiKeyPageable(null, null, null).hasLimit());
    }
}
