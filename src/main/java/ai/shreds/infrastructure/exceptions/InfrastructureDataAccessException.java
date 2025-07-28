package ai.shreds.infrastructure.exceptions;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.sql.SQLException;

/**
 * Exception thrown when database or data access operations fail.
 * This exception encapsulates technical data access failures that should be handled at the repository layer.
 * Provides detailed context about the failed operation including entity information and database state.
 */
public class InfrastructureDataAccessException extends RuntimeException {

    private final String entity;
    private final String operation;
    private final Object entityId;
    private final String sqlState;
    private final Integer errorCode;
    private final String tableName;
    private final Instant timestamp;
    private final Map<String, Object> additionalContext;

    /**
     * Creates a new data access exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InfrastructureDataAccessException(String message) {
        super(message);
        this.entity = null;
        this.operation = null;
        this.entityId = null;
        this.sqlState = null;
        this.errorCode = null;
        this.tableName = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new data access exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InfrastructureDataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.entity = null;
        this.operation = null;
        this.entityId = null;
        this.sqlState = extractSqlState(cause);
        this.errorCode = extractErrorCode(cause);
        this.tableName = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new data access exception with context about the entity and operation.
     *
     * @param entity the entity type that was being accessed (e.g., "Product", "Category")
     * @param operation the database operation that failed (e.g., "save", "find", "delete")
     * @param cause the cause of the exception
     */
    public InfrastructureDataAccessException(String entity, String operation, Throwable cause) {
        super(formatMessage(entity, operation, null), cause);
        this.entity = entity;
        this.operation = operation;
        this.entityId = null;
        this.sqlState = extractSqlState(cause);
        this.errorCode = extractErrorCode(cause);
        this.tableName = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new data access exception with comprehensive context.
     *
     * @param entity the entity type
     * @param operation the database operation
     * @param entityId the ID of the entity being operated on
     * @param tableName the database table name
     * @param cause the cause of the exception
     */
    public InfrastructureDataAccessException(String entity, String operation, Object entityId, 
                                            String tableName, Throwable cause) {
        super(formatMessage(entity, operation, entityId), cause);
        this.entity = entity;
        this.operation = operation;
        this.entityId = entityId;
        this.sqlState = extractSqlState(cause);
        this.errorCode = extractErrorCode(cause);
        this.tableName = tableName;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new data access exception with full context and additional metadata.
     *
     * @param entity the entity type
     * @param operation the database operation
     * @param entityId the ID of the entity
     * @param tableName the database table name
     * @param additionalContext additional context information
     * @param cause the cause of the exception
     */
    public InfrastructureDataAccessException(String entity, String operation, Object entityId, 
                                            String tableName, Map<String, Object> additionalContext, 
                                            Throwable cause) {
        super(formatMessage(entity, operation, entityId), cause);
        this.entity = entity;
        this.operation = operation;
        this.entityId = entityId;
        this.sqlState = extractSqlState(cause);
        this.errorCode = extractErrorCode(cause);
        this.tableName = tableName;
        this.timestamp = Instant.now();
        this.additionalContext = additionalContext != null ? new HashMap<>(additionalContext) : new HashMap<>();
    }

    /**
     * @return the entity type involved in the operation, or null if not specified
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @return the operation that was being performed, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return the ID of the entity being operated on, or null if not specified
     */
    public Object getEntityId() {
        return entityId;
    }

    /**
     * @return the SQL state code if available, or null if not specified
     */
    public String getSqlState() {
        return sqlState;
    }

    /**
     * @return the database vendor-specific error code, or null if not available
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @return the database table name, or null if not specified
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return the timestamp when the exception occurred
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * @return additional context information as a map
     */
    public Map<String, Object> getAdditionalContext() {
        return new HashMap<>(additionalContext);
    }

    /**
     * Adds additional context information to the exception.
     *
     * @param key the context key
     * @param value the context value
     */
    public void addContext(String key, Object value) {
        if (key != null) {
            additionalContext.put(key, value);
        }
    }

    /**
     * Gets a specific context value.
     *
     * @param key the context key
     * @return the context value, or null if not found
     */
    public Object getContext(String key) {
        return additionalContext.get(key);
    }

    /**
     * @return a summary of the operation information for logging purposes
     */
    public String getOperationSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (operation != null) {
            summary.append("Operation: ").append(operation);
        }
        
        if (entity != null) {
            summary.append(", Entity: ").append(entity);
        }
        
        if (entityId != null) {
            summary.append(", ID: ").append(entityId);
        }
        
        if (tableName != null) {
            summary.append(", Table: ").append(tableName);
        }
        
        if (sqlState != null) {
            summary.append(", SQL State: ").append(sqlState);
        }
        
        if (errorCode != null) {
            summary.append(", Error Code: ").append(errorCode);
        }
        
        return summary.toString();
    }

    /**
     * Database operation type checking methods
     */
    
    /**
     * @return whether this exception represents a save/insert operation failure
     */
    public boolean isSaveFailure() {
        return "save".equalsIgnoreCase(operation) || "insert".equalsIgnoreCase(operation) || "create".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents a find/select operation failure
     */
    public boolean isFindFailure() {
        return "find".equalsIgnoreCase(operation) || "select".equalsIgnoreCase(operation) || "get".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents an update operation failure
     */
    public boolean isUpdateFailure() {
        return "update".equalsIgnoreCase(operation) || "modify".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents a delete operation failure
     */
    public boolean isDeleteFailure() {
        return "delete".equalsIgnoreCase(operation) || "remove".equalsIgnoreCase(operation);
    }

    /**
     * Database error type checking methods based on SQL state
     */
    
    /**
     * @return whether this represents a constraint violation (primary key, foreign key, unique, etc.)
     */
    public boolean isConstraintViolation() {
        return sqlState != null && (sqlState.startsWith("23") || sqlState.equals("21000"));
    }

    /**
     * @return whether this represents a connection/timeout issue
     */
    public boolean isConnectionIssue() {
        return sqlState != null && (sqlState.startsWith("08") || sqlState.startsWith("HY"));
    }

    /**
     * @return whether this represents a transaction rollback
     */
    public boolean isTransactionRollback() {
        return sqlState != null && sqlState.startsWith("40");
    }

    /**
     * @return whether this represents a data integrity issue
     */
    public boolean isDataIntegrityIssue() {
        return sqlState != null && (sqlState.startsWith("22") || sqlState.startsWith("23"));
    }

    /**
     * Static factory methods for common data access exceptions
     */

    /**
     * Creates an entity not found exception
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return data access exception for entity not found
     */
    public static InfrastructureDataAccessException entityNotFound(String entityType, Object entityId) {
        InfrastructureDataAccessException exception = new InfrastructureDataAccessException(
                String.format("%s with ID %s not found", entityType, entityId));
        exception.addContext("entityType", entityType);
        exception.addContext("entityId", entityId);
        exception.addContext("errorType", "ENTITY_NOT_FOUND");
        return exception;
    }

    /**
     * Creates a duplicate key/constraint violation exception
     * @param entityType the entity type
     * @param fieldName the field that violated uniqueness
     * @param fieldValue the duplicate value
     * @param cause the underlying cause
     * @return data access exception for constraint violation
     */
    public static InfrastructureDataAccessException duplicateKey(String entityType, String fieldName, 
                                                                Object fieldValue, Throwable cause) {
        InfrastructureDataAccessException exception = new InfrastructureDataAccessException(
                entityType, "save", cause);
        exception.addContext("errorType", "DUPLICATE_KEY");
        exception.addContext("duplicateField", fieldName);
        exception.addContext("duplicateValue", fieldValue);
        return exception;
    }

    /**
     * Creates an optimistic locking failure exception
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param expectedVersion the expected version
     * @param actualVersion the actual version
     * @return data access exception for optimistic locking failure
     */
    public static InfrastructureDataAccessException optimisticLockingFailure(String entityType, Object entityId, 
                                                                             Long expectedVersion, Long actualVersion) {
        InfrastructureDataAccessException exception = new InfrastructureDataAccessException(
                String.format("Optimistic locking failure for %s %s: expected version %d, actual version %d", 
                        entityType, entityId, expectedVersion, actualVersion));
        exception.addContext("entityType", entityType);
        exception.addContext("entityId", entityId);
        exception.addContext("expectedVersion", expectedVersion);
        exception.addContext("actualVersion", actualVersion);
        exception.addContext("errorType", "OPTIMISTIC_LOCKING_FAILURE");
        return exception;
    }

    /**
     * Creates a foreign key constraint violation exception
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param referencedEntity the referenced entity type
     * @param referencedId the referenced entity ID
     * @param cause the underlying cause
     * @return data access exception for foreign key violation
     */
    public static InfrastructureDataAccessException foreignKeyViolation(String entityType, Object entityId, 
                                                                        String referencedEntity, Object referencedId, 
                                                                        Throwable cause) {
        InfrastructureDataAccessException exception = new InfrastructureDataAccessException(
                entityType, "save", entityId, null, cause);
        exception.addContext("errorType", "FOREIGN_KEY_VIOLATION");
        exception.addContext("referencedEntity", referencedEntity);
        exception.addContext("referencedId", referencedId);
        return exception;
    }

    /**
     * Helper methods
     */
    
    private static String formatMessage(String entity, String operation, Object entityId) {
        StringBuilder message = new StringBuilder();
        
        if (operation != null) {
            message.append(operation.substring(0, 1).toUpperCase())
                   .append(operation.substring(1).toLowerCase())
                   .append(" operation failed");
        } else {
            message.append("Database operation failed");
        }
        
        if (entity != null) {
            message.append(" for entity: ").append(entity);
        }
        
        if (entityId != null) {
            message.append(" with ID: ").append(entityId);
        }
        
        return message.toString();
    }
    
    private static String extractSqlState(Throwable cause) {
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getSQLState();
        }
        
        // Check if cause chain contains SQLException
        Throwable current = cause;
        while (current != null && !(current instanceof SQLException)) {
            current = current.getCause();
        }
        
        if (current instanceof SQLException) {
            return ((SQLException) current).getSQLState();
        }
        
        return null;
    }
    
    private static Integer extractErrorCode(Throwable cause) {
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getErrorCode();
        }
        
        // Check if cause chain contains SQLException
        Throwable current = cause;
        while (current != null && !(current instanceof SQLException)) {
            current = current.getCause();
        }
        
        if (current instanceof SQLException) {
            return ((SQLException) current).getErrorCode();
        }
        
        return null;
    }
}
