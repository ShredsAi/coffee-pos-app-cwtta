package ai.shreds.domain.exceptions;

/**
 * Domain exception thrown when an invalid or incompatible movement type is used.
 * Indicates business rule violations related to stock movement types.
 */
public class DomainInvalidMovementTypeException extends RuntimeException {
    private final String movementType;
    
    /**
     * Constructs a new exception with the specified detail message and movement type.
     *
     * @param message the detail message
     * @param movementType the invalid movement type
     */
    public DomainInvalidMovementTypeException(String message, String movementType) {
        super(message);
        this.movementType = movementType;
    }
    
    /**
     * Constructs a new exception with the specified detail message, movement type, and cause.
     *
     * @param message the detail message
     * @param movementType the invalid movement type
     * @param cause the cause of the exception
     */
    public DomainInvalidMovementTypeException(String message, String movementType, Throwable cause) {
        super(message, cause);
        this.movementType = movementType;
    }
    
    /**
     * Gets the invalid movement type.
     *
     * @return the movement type
     */
    public String getMovementType() {
        return movementType;
    }
    
    /**
     * Gets a formatted error message with the movement type.
     *
     * @return a detailed error message
     */
    public String getFormattedErrorMessage() {
        return String.format("Invalid movement type '%s': %s", movementType, getMessage());
    }
    
    @Override
    public String toString() {
        return "DomainInvalidMovementTypeException{" +
                "movementType='" + movementType + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}