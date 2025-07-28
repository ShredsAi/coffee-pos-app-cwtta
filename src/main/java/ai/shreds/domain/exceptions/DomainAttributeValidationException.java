package ai.shreds.domain.exceptions;

import java.util.Map;

/**
 * Domain Attribute Validation Exception
 * Thrown when attribute validation fails
 */
public class DomainAttributeValidationException extends RuntimeException {
    
    private final String attributeCode;
    private final Map<String, String> validationErrors;
    
    /**
     * Constructor with attribute code and message
     * 
     * @param attributeCode The attribute code that failed validation
     * @param message The validation error message
     */
    public DomainAttributeValidationException(String attributeCode, String message) {
        super(String.format("Attribute validation failed for '%s': %s", attributeCode, message));
        this.attributeCode = attributeCode;
        this.validationErrors = Map.of(attributeCode, message);
    }
    
    /**
     * Constructor with multiple validation errors
     * 
     * @param errors Map of attribute codes to error messages
     */
    public DomainAttributeValidationException(Map<String, String> errors) {
        super("Attribute validation failed: " + errors.toString());
        this.attributeCode = null;
        this.validationErrors = errors;
    }
    
    /**
     * Gets the attribute code that failed validation
     * 
     * @return The attribute code or null if multiple attributes failed
     */
    public String getAttributeCode() {
        return attributeCode;
    }
    
    /**
     * Gets all validation errors
     * 
     * @return Map of attribute codes to error messages
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
    
    /**
     * Factory method for required attribute missing value
     * 
     * @param attributeCode The required attribute code
     * @return The exception instance
     */
    public static DomainAttributeValidationException requiredValueMissing(String attributeCode) {
        return new DomainAttributeValidationException(attributeCode, "Required attribute must have a value");
    }
    
    /**
     * Factory method for invalid attribute type
     * 
     * @param attributeCode The attribute code
     * @param expectedType The expected type
     * @param actualType The actual type
     * @return The exception instance
     */
    public static DomainAttributeValidationException invalidType(String attributeCode, String expectedType, String actualType) {
        return new DomainAttributeValidationException(attributeCode, 
            String.format("Expected type %s but got %s", expectedType, actualType));
    }
    
    /**
     * Factory method for invalid option selection
     * 
     * @param attributeCode The attribute code
     * @param optionValue The invalid option value
     * @return The exception instance
     */
    public static DomainAttributeValidationException invalidOption(String attributeCode, String optionValue) {
        return new DomainAttributeValidationException(attributeCode, 
            String.format("Option '%s' is not valid for this attribute", optionValue));
    }
    
    /**
     * Factory method for too many options selected on single-select attribute
     * 
     * @param attributeCode The attribute code
     * @return The exception instance
     */
    public static DomainAttributeValidationException tooManyOptionsSelected(String attributeCode) {
        return new DomainAttributeValidationException(attributeCode, 
            "Single-select attribute cannot have multiple options selected");
    }
}