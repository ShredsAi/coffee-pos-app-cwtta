package ai.shreds.domain.exceptions;

/**
 * Domain exception thrown when validation rules are violated.
 * Contains information about the specific field that failed validation
 * and the value that was rejected.
 */
public class DomainValidationException extends RuntimeException {
    private final String fieldName;
    private final Object rejectedValue;

    /**
     * Constructs a new validation exception with the specified detail message,
     * field name, and rejected value.
     *
     * @param message the detail message explaining the validation failure
     * @param fieldName the name of the field that failed validation
     * @param rejectedValue the value that was rejected during validation
     */
    public DomainValidationException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Constructs a new validation exception with the specified detail message,
     * field name, rejected value, and cause.
     *
     * @param message the detail message explaining the validation failure
     * @param fieldName the name of the field that failed validation
     * @param rejectedValue the value that was rejected during validation
     * @param cause the cause of the validation failure
     */
    public DomainValidationException(String message, String fieldName, Object rejectedValue, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Gets the name of the field that failed validation.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the value that was rejected during validation.
     *
     * @return the rejected value
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * Creates a formatted error message that includes field name and rejected value
     * for better debugging and logging.
     *
     * @return a detailed error message
     */
    public String getDetailedMessage() {
        return String.format(
            "Validation failed for field '%s' with value '%s': %s",
            fieldName != null ? fieldName : "unknown",
            rejectedValue != null ? rejectedValue.toString() : "null",
            getMessage()
        );
    }

    @Override
    public String toString() {
        return "DomainValidationException{" +
                "fieldName='" + fieldName + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}