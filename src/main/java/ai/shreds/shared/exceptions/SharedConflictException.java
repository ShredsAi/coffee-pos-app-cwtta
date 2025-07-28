package ai.shreds.shared.exceptions;

/**
 * Exception thrown when a conflict occurs in business operations.
 */
public class SharedConflictException extends BusinessException {

    private static final String ERROR_CODE = "CONFLICT";
    private static final int STATUS_CODE = 409;

    public SharedConflictException(String message) {
        super(message, ERROR_CODE, STATUS_CODE);
    }

    public SharedConflictException(String field, String value) {
        super(String.format("A conflict occurred for field '%s' with value '%s'.", field, value), ERROR_CODE, STATUS_CODE);
    }
}