package ai.shreds.shared.exceptions;

/**
 * Alternative exception for business conflicts, created to resolve compilation caching issues.
 * This replaces SharedConflictException functionality.
 */
public class SharedBusinessConflictException extends RuntimeException {

    private final String errorCode = "CONFLICT_ERROR";
    private final int httpStatus = 409;

    public SharedBusinessConflictException(String message) {
        super(message);
    }

    public SharedBusinessConflictException(String field, String value) {
        super(String.format("Conflict for %s with value '%s'", field, value));
    }

    public SharedBusinessConflictException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}