package ai.shreds.shared.exceptions;

/**
 * Exception thrown when there is a conflict with existing product data.
 * This is a replacement for SharedConflictException to resolve compilation issues.
 */
public class ProductConflictException extends RuntimeException {

    private final String errorCode = "CONFLICT_ERROR";
    private final int httpStatus = 409;

    public ProductConflictException(String message) {
        super(message);
    }

    public ProductConflictException(String field, String value) {
        super(String.format("Conflict for %s with value '%s'", field, value));
    }

    public ProductConflictException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}