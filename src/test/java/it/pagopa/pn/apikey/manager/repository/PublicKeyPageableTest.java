package it.pagopa.pn.apikey.manager.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PublicKeyPageableTest {

    @Test
    void isPageReturnsTrueWhenBothKeysArePresent() {
        assertTrue(new PublicKeyPageable(1, "key", "createdAt").isPage());
    }

    @Test
    void isPageReturnsFalseWhenLastEvaluatedKeyIsMissing() {
        assertFalse(new PublicKeyPageable(1, "", "createdAt").isPage());
    }

    @Test
    void isPageReturnsFalseWhenLastEvaluatedCreatedAtIsMissing() {
        assertFalse(new PublicKeyPageable(1, "key", "").isPage());
    }

    @Test
    void isPageReturnsFalseWhenBothKeysAreMissing() {
        assertFalse(new PublicKeyPageable(1, "", "").isPage());
    }

    @Test
    void hasLimitReturnsTrueWhenLimitIsPositive() {
        assertTrue(new PublicKeyPageable(1, null, null).hasLimit());
    }

    @Test
    void hasLimitReturnsFalseWhenLimitIsZero() {
        assertFalse(new PublicKeyPageable(0, null, null).hasLimit());
    }

    @Test
    void hasLimitReturnsFalseWhenLimitIsNull() {
        assertFalse(new PublicKeyPageable(null, null, null).hasLimit());
    }
}