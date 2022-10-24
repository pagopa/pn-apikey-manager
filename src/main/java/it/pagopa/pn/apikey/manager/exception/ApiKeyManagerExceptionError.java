package it.pagopa.pn.apikey.manager.exception;

public class ApiKeyManagerExceptionError {

    private ApiKeyManagerExceptionError(){}

    public static final String INVALID_STATUS = "Invalid status change";
    public static final String KEY_DOES_NOT_EXISTS = "ApiKey does not exist";
    public static final String KEY_IS_NOT_UNIQUE = "ApiKey is not unique";
    public static final String PA_AGGREGATION_NOT_FOUND = "PA is not associated with an aggregation";
}
