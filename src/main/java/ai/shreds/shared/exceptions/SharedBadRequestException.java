package ai.shreds.shared.exceptions;

public class SharedBadRequestException extends RuntimeException {
    
    public SharedBadRequestException(String message) {
        super(message);
    }
    
    public SharedBadRequestException(String field, String message) {
        super(String.format("Invalid %s: %s", field, message));
    }
}