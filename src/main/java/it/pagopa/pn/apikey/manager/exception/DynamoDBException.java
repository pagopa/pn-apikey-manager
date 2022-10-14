package it.pagopa.pn.apikey.manager.exception;

public class DynamoDBException extends Exception {

	private static final long serialVersionUID = 5257836388656020237L;

	private static final String CODE = "";
	 
	public DynamoDBException() {
        super();
    }

	public DynamoDBException(String message) {
        super(message);
    }

	public DynamoDBException(String message, Throwable cause) {
        super(message, cause);
    }

	public DynamoDBException(Throwable cause) {
        super(cause);
    }

}
