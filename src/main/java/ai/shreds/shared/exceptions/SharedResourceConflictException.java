package ai.shreds.shared.exceptions;

/**
 * Alternative conflict exception to test if the issue is file-name specific.
 */
public class SharedResourceConflictException extends RuntimeException {

    public SharedResourceConflictException(String message) {
        super(message);
    }

    public SharedResourceConflictException(String field, String value) {
        super(String.format("Conflict for %s with value '%s'", field, value));
    }

    public SharedResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}