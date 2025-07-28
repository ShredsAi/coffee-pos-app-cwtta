package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain Product Not Found Exception
 * Thrown when a product is not found by ID or other unique identifier
 */
public class DomainProductNotFoundException extends RuntimeException {
    
    private final UUID productId;
    private final String field;
    private final String value;
    
    /**
     * Constructor with product ID
     * 
     * @param id The product ID that was not found
     */
    public DomainProductNotFoundException(UUID id) {
        super(String.format("Product with ID '%s' not found", id));
        this.productId = id;
        this.field = "id";
        this.value = id != null ? id.toString() : null;
    }
    
    /**
     * Constructor with field and value
     * 
     * @param field The field name (e.g., "sku", "slug")
     * @param value The field value that was not found
     */
    public DomainProductNotFoundException(String field, String value) {
        super(String.format("Product with %s '%s' not found", field, value));
        this.productId = null;
        this.field = field;
        this.value = value;
    }
    
    /**
     * Gets the product ID if the exception was thrown for an ID lookup
     * 
     * @return The product ID or null if not applicable
     */
    public UUID getProductId() {
        return productId;
    }
    
    /**
     * Gets the field name that was used for the lookup
     * 
     * @return The field name (e.g., "id", "sku", "slug")
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