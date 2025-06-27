package ai.shreds.application.exceptions;

/**
 * Exception thrown when validation fails for application layer operations.
 * This exception is typically thrown when request validation fails
 * or business rule validation is not satisfied.
 */
public class ApplicationValidationException extends RuntimeException {

    private final String fieldName;
    private final Object rejectedValue;

    /**
     * Constructs a new validation exception with the specified detail message, field name, and rejected value.
     *
     * @param message The detailed error message
     * @param fieldName The name of the field that failed validation
     * @param rejectedValue The value that was rejected during validation
     */
    public ApplicationValidationException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Constructs a new validation exception with just the error message.
     *
     * @param message The detailed error message
     */
    public ApplicationValidationException(String message) {
        this(message, null, null);
    }

    /**
     * Gets the name of the field that failed validation.
     *
     * @return The field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the value that was rejected during validation.
     *
     * @return The rejected value, or null if not specified
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * Creates a detailed error message including the field name and rejected value if available.
     *
     * @return Formatted error message
     */
    @Override
    public String getMessage() {
        if (fieldName != null) {
            return String.format("%s - Field: %s, Rejected Value: %s",
                    super.getMessage(), fieldName, rejectedValue);
        }
        return super.getMessage();
    }
}