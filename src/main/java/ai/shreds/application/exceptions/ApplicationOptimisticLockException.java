package ai.shreds.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an optimistic locking conflict occurs.
 * This exception is typically thrown when an entity has been modified
 * by another transaction between the time it was read and updated.
 */
public class ApplicationOptimisticLockException extends RuntimeException {

    private final String entityType;
    private final UUID entityId;

    /**
     * Constructs a new optimistic lock exception with the specified detail message, entity type, and entity ID.
     *
     * @param message The detailed error message
     * @param entityType The type of entity that had the optimistic lock conflict
     * @param entityId The ID of the entity that had the optimistic lock conflict
     */
    public ApplicationOptimisticLockException(String message, String entityType, UUID entityId) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Gets the type of entity that had the optimistic lock conflict.
     *
     * @return The entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the ID of the entity that had the optimistic lock conflict.
     *
     * @return The entity ID
     */
    public UUID getEntityId() {
        return entityId;
    }

    /**
     * Creates a detailed error message including the entity type and ID.
     *
     * @return Formatted error message
     */
    @Override
    public String getMessage() {
        return String.format("%s - Entity Type: %s, Entity ID: %s",
                super.getMessage(), entityType, entityId);
    }
}