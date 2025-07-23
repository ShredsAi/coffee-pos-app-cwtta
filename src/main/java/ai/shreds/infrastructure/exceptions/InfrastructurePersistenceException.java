package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when persistence operations fail in the infrastructure layer.
 * Used to wrap specific JPA or database exceptions with more context.
 */
public class InfrastructurePersistenceException extends RuntimeException {

    private final String entityType;
    private final String operation;

    /**
     * Constructs a new persistence exception with details about the failed operation.
     * @param message error message
     * @param entityType the entity type that was being persisted/retrieved
     * @param operation the operation that failed (save, find, etc.)
     * @param cause the underlying exception
     */
    public InfrastructurePersistenceException(String message, String entityType, String operation, Throwable cause) {
        super(message, cause);
        this.entityType = entityType;
        this.operation = operation;
    }

    /**
     * Gets the entity type involved in the failed operation.
     * @return entity type name
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the operation that failed.
     * @return operation name
     */
    public String getOperation() {
        return operation;
    }
}