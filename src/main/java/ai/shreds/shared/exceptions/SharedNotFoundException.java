package ai.shreds.shared.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a requested entity is not found.
 */
public class SharedNotFoundException extends BusinessException {
    
    public SharedNotFoundException(String message) {
        super(message, "NOT_FOUND_ERROR", 404);
    }
    
    public SharedNotFoundException(String entityType, UUID id) {
        super(String.format("%s with ID %s not found", entityType, id), "NOT_FOUND_ERROR", 404);
    }
}