package it.pagopa.pn.apikey.manager.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AggregatePageableTest {
    /**
     * Method under test: {@link AggregatePageable#isPageable()}
     */
    @Test
    void testIsPageable() {
        assertTrue((new AggregatePageable(1, "42")).isPageable());
        assertFalse((new AggregatePageable(1, "")).isPageable());
    }
}

