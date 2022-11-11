package it.pagopa.pn.apikey.manager.exception;

public class ApiKeyManagerExceptionError {

    private ApiKeyManagerExceptionError() { }

    public static final String INVALID_STATUS = "Can not change ApiKey status from %s to %s";
    public static final String CAN_NOT_DELETE = "Can not delete ApiKey - current state is %s";
    public static final String KEY_DOES_NOT_EXISTS = "ApiKey does not exist";

    public static final String AGGREGATE_INVALID_STATUS = "Aggregate cannot be eliminated because there are some associated PA";
    public static final String AGGREGATE_NOT_FOUND = "Aggregate not found";

}
