package ai.shreds.application.exceptions;

/**
 * Exception thrown when an entity is not found in the system.
 * This exception is typically thrown when attempting to retrieve, update,
 * or delete an entity that does not exist.
 */
public class ApplicationEntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final String entityId;

    /**
     * Constructs a new entity not found exception with the specified detail message, entity type, and entity ID.
     *
     * @param message The detailed error message
     * @param entityType The type of entity that was not found (e.g., "Warehouse", "InventoryItem")
     * @param entityId The ID of the entity that was not found
     */
    public ApplicationEntityNotFoundException(String message, String entityType, String entityId) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Gets the type of entity that was not found.
     *
     * @return The entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the ID of the entity that was not found.
     *
     * @return The entity ID
     */
    public String getEntityId() {
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