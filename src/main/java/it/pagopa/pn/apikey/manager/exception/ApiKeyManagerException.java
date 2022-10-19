package it.pagopa.pn.apikey.manager.exception;

public class ApiKeyManagerException extends RuntimeException{

    public ApiKeyManagerException() {
        super();
    }

    public ApiKeyManagerException(String message) {
        super(message);
    }

    public ApiKeyManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiKeyManagerException(Throwable cause) {
        super(cause);
    }

}
