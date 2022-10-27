package it.pagopa.pn.apikey.manager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiKeyManagerException extends RuntimeException{

    @Getter
    private final HttpStatus status;

    public ApiKeyManagerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
