package it.pagopa.pn.apikey.manager.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaAggregationPageableTest {

    /**
     * Method under test: {@link PaAggregationPageable#isPage()}
     */
    @Test
    void testIsPageable() {
        assertTrue(new PaAggregationPageable(1, "42").isPage());
        assertFalse(new PaAggregationPageable(1, "").isPage());
    }

    @Test
    void testHasLimit() {
        assertTrue(new PaAggregationPageable(1, null).hasLimit());
        assertFalse(new PaAggregationPageable(0, null).hasLimit());
        assertFalse(new PaAggregationPageable(null, null).hasLimit());
    }
}
