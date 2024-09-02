package it.pagopa.pn.apikey.manager.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

public class PnForbiddenException extends PnRuntimeException {
    public static final String ERROR_CODE_MANDATE_NOT_FOUND = "PN_MANDATE_NOTFOUND";
    public PnForbiddenException() {
        super("Accesso negato!", "L'utente non è autorizzato ad accedere alla risorsa richiesta.",
                HttpStatus.NOT_FOUND.value(), ERROR_CODE_MANDATE_NOT_FOUND, null, null);
    }

}
