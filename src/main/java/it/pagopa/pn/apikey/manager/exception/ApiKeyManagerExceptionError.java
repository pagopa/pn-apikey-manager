package it.pagopa.pn.apikey.manager.exception;

public class ApiKeyManagerExceptionError {

    private ApiKeyManagerExceptionError() { }

    public static final String APIKEY_INVALID_STATUS = "Can not change ApiKey status from %s to %s";
    public static final String APIKEY_CAN_NOT_DELETE = "Can not delete ApiKey - current state is %s";
    public static final String APIKEY_FORBIDDEN_DELETE = "Not authorized to delete ApiKey";
    public static final String APIKEY_DOES_NOT_EXISTS = "ApiKey does not exist";
    public static final String APIKEY_CX_TYPE_NOT_ALLOWED = "CxTypeAuthFleet %s not allowed";

    public static final String AGGREGATE_INVALID_STATUS = "Non è possibile eliminare l'aggregato perché ci sono delle PA associate";
    public static final String AGGREGATE_NOT_FOUND = "Aggregato non trovato";

    public static final String INVALID_NAME_LENGTH = "Il nome della PA deve essere lungo almeno 3 caratteri";

}
