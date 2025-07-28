package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain Attribute Not Found Exception
 * Thrown when an attribute is not found by ID or other unique identifier
 */
public class DomainAttributeNotFoundException extends RuntimeException {
    
    private final UUID attributeId;
    private final String field;
    private final String value;
    
    /**
     * Constructor with attribute ID
     * 
     * @param id The attribute ID that was not found
     */
    public DomainAttributeNotFoundException(UUID id) {
        super(String.format("Attribute with ID '%s' not found", id));
        this.attributeId = id;
        this.field = "id";
        this.value = id != null ? id.toString() : null;
    }
    
    /**
     * Constructor with field and value
     * 
     * @param field The field name (e.g., "code")
     * @param value The field value that was not found
     */
    public DomainAttributeNotFoundException(String field, String value) {
        super(String.format("Attribute with %s '%s' not found", field, value));
        this.attributeId = null;
        this.field = field;
        this.value = value;
    }
    
    /**
     * Gets the attribute ID if the exception was thrown for an ID lookup
     * 
     * @return The attribute ID or null if not applicable
     */
    public UUID getAttributeId() {
        return attributeId;
    }
    
    /**
     * Gets the field name that was used for the lookup
     * 
     * @return The field name (e.g., "id", "code")
     */
    public String getField() {
        return field;
    }
    
    /**
     * Gets the field value that was used for the lookup
     * 
     * @return The field value
     */
    public String getValue() {
        return value;
    }
}