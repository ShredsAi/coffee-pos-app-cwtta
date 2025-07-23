package ai.shreds.domain.exceptions;

/**
 * Exception thrown when an entity is not found in the domain layer.
 */
public class DomainEntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final String entityId;

    public DomainEntityNotFoundException(String message, String entityType, String entityId) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public DomainEntityNotFoundException(String message, String entityType, String entityId, Throwable cause) {
        super(message, cause);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetailedMessage() {
        return String.format("%s: Entity of type '%s' with id '%s' not found", 
            getMessage(), entityType, entityId);
    }

    @Override
    public String toString() {
        return String.format("DomainEntityNotFoundException{message='%s', entityType='%s', entityId='%s'}", 
            getMessage(), entityType, entityId);
    }
}