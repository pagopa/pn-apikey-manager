package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;

public class CheckExceptionUtils {

    private CheckExceptionUtils() {}
    public static boolean checkExceptionStatusForLogLevel(Throwable throwable) {
        return throwable instanceof ApiKeyManagerException apiKeyManagerException
                && !apiKeyManagerException.getStatus().is5xxServerError();
    }
}
