package ai.shreds.shared.exceptions;

/**
 * Abstract base class for custom business exceptions.
 * It standardizes the inclusion of an error code and HTTP status code.
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    /**
     * Constructs a new business exception with the specified detail message, error code, and status code.
     *
     * @param message    the detail message.
     * @param errorCode  the application-specific error code.
     * @param statusCode the HTTP status code to be returned.
     */
    protected BusinessException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}