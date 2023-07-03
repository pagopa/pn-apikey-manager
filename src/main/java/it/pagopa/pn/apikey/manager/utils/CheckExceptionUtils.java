package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;

public class CheckExceptionUtils {

    private CheckExceptionUtils() {}
    private static boolean checkExceptionStatusForLogLevel(Throwable throwable) {
        return throwable instanceof ApiKeyManagerException apiKeyManagerException
                && !apiKeyManagerException.getStatus().is5xxServerError();
    }

    public static void logAuditOnErrorOrWarnLevel(Throwable throwable, PnAuditLogEvent logEvent) {
        if(checkExceptionStatusForLogLevel(throwable)){
            logEvent.generateWarning(throwable.getMessage()).log();
        }
        else{
            logEvent.generateFailure(throwable.getMessage()).log();
        }
    }
}
