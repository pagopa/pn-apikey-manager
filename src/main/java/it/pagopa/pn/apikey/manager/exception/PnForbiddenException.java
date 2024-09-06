package it.pagopa.pn.apikey.manager.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnForbiddenException extends PnRuntimeException {
    public static final String ERROR_CODE_APIKEY_MANAGER_FORBIDDEN = "PN_APIKEY_MANAGER_FORBIDDEN";
    public PnForbiddenException() {
        super("Accesso negato!", "L'utente non è autorizzato ad accedere alla risorsa richiesta.",
                HttpStatus.FORBIDDEN.value(), ERROR_CODE_APIKEY_MANAGER_FORBIDDEN, null, null);
    }

}
