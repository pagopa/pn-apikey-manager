package it.pagopa.pn.apikey.manager.exception;

public class ApiKeyManagerExceptionError {

    private ApiKeyManagerExceptionError() { }

    public static final String APIKEY_INVALID_STATUS = "Can not change ApiKey status from %s to %s";
    public static final String APIKEY_CAN_NOT_DELETE = "Can not delete ApiKey - current state is %s";
    public static final String APIKEY_DOES_NOT_EXISTS = "ApiKey does not exist";
    public static final String APIKEY_CX_TYPE_NOT_ALLOWED = "CxTypeAuthFleet %s not allowed";

    public static final String AGGREGATE_INVALID_STATUS = "Non è possibile eliminare l'aggregato perché ci sono delle PA associate";
    public static final String AGGREGATE_NOT_FOUND = "Aggregato non trovato";

    public static final String INVALID_NAME_LENGTH = "Il nome della PA deve essere lungo almeno 3 caratteri";
    public static final String ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED = "ERROR_CODE_APIKEY_MANAGER_EVENTTYPENOTSUPPORTED";
    public static final String APIKEY_FORBIDDEN_OPERATION = "Given virtual key doesn't belong to user";
    public static final String SAME_STATUS_APIKEY_ALREADY_EXISTS = "User has another Virtual key with status %s";

    public static final String VIRTUALKEY_INVALID_STATUS = "Can not change Virtual key status from %s to %s";

    public static final String TOS_CONSENT_NOT_FOUND = "TOS consent for the B2B api usage not accepted.";
    public static final String VALID_PUBLIC_KEY_NOT_FOUND = "Valid public key not found, there must be at least a public key with status ACTIVE, ROTATED or BLOCKED.";

    public static final String PUBLIC_KEY_DOES_NOT_EXISTS = "Public key does not exist.";
    public static final String PUBLIC_KEY_NAME_MANDATORY = "Name is mandatory";
    public static final String PUBLIC_KEY_MANDATORY = "Public key is mandatory";
    public static final String PUBLIC_KEY_CAN_NOT_DELETE = "Public key can not be deleted";
    public static final String PUBLIC_KEY_INVALID_STATE_TRANSITION = "Invalid state transition";
    public static final String PUBLIC_KEY_ALREADY_EXISTS = "Public key with status %s already exists.";
    public static final String PUBLIC_KEY_ALREADY_EXISTS_ACTIVE = "Public key with status ACTIVE already exists, to create a new public key use the rotate operation.";
    public static final String ACCESS_DENIED = "Access denied.";
    public static final String TTL_PAYLOAD_INVALID_KID_CXID = "The key or cxid is empty.";
    public static final String TTL_PAYLOAD_INVALID_ACTION = "The action is empty or not valid.";
    public static final String PUBLIC_KEY_NOT_EXPIRED = "PublicKey with kid [%s] and cxid [%s], is not expired. Event will ignore";
    public static final String PUBLIC_KEY_ALREADY_DELETED = "PublicKey with kid [%s] and cxid [%s], is already DELETED. Event will ignore";
    public static final String PUBLIC_KEY_ALREADY_USED = "Public key already used.";
    public static final String FAILED_TO_CREATEJWKS_JSON = "Failed to create JWKS JSON";
    public static final String FAILED_TO_INVOKE_LAMBDA_FUNCTION = "Failed to invoke Lambda function";
}