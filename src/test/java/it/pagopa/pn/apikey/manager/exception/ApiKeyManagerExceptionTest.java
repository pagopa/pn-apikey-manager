package it.pagopa.pn.apikey.manager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiKeyManagerExceptionTest {
    /**
     * Method under test: {@link ApiKeyManagerException#ApiKeyManagerException(String, HttpStatus)}
     */
    @Test
    void testConstructor() {
        ApiKeyManagerException actualApiKeyManagerException = new ApiKeyManagerException("An error occurred",
                HttpStatus.CONTINUE);

        assertNull(actualApiKeyManagerException.getCause());
        assertEquals(0, actualApiKeyManagerException.getSuppressed().length);
        assertEquals(HttpStatus.CONTINUE, actualApiKeyManagerException.getStatus());
        assertEquals("An error occurred", actualApiKeyManagerException.getMessage());
        assertEquals("An error occurred", actualApiKeyManagerException.getLocalizedMessage());
    }
}

