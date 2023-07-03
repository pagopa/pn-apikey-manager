package it.pagopa.pn.apikey.manager.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PaPageableTest {
    /**
     * Method under test: {@link PaPageable#isPage()}
     */
    @Test
    void testIsPage() {
        assertTrue((new PaPageable(1, "42", "Doe")).isPage());
        assertFalse((new PaPageable(1, "", "Doe")).isPage());
    }

    /**
     * Method under test: {@link PaPageable#isPageByName()}
     */
    @Test
    void testIsPageByName() {
        assertTrue((new PaPageable(1, "42", "Doe")).isPageByName());
        assertFalse((new PaPageable(1, "", "Doe")).isPageByName());
        assertFalse((new PaPageable(1, "42", "")).isPageByName());
    }

    /**
     * Method under test: {@link PaPageable#hasLimit()}
     */
    @Test
    void testHasLimit() {
        assertTrue((new PaPageable(1, "42", "Doe")).hasLimit());
        assertFalse((new PaPageable(0, "42", "Doe")).hasLimit());
        assertFalse((new PaPageable(null, "42", "Doe")).hasLimit());
    }
}

