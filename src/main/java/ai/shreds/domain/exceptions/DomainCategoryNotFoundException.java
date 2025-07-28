package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain Category Not Found Exception
 * Thrown when a category is not found by ID or other unique identifier
 */
public class DomainCategoryNotFoundException extends RuntimeException {
    
    private final UUID categoryId;
    private final String field;
    private final String value;
    
    /**
     * Constructor with category ID
     * 
     * @param id The category ID that was not found
     */
    public DomainCategoryNotFoundException(UUID id) {
        super(String.format("Category with ID '%s' not found", id));
        this.categoryId = id;
        this.field = "id";
        this.value = id != null ? id.toString() : null;
    }
    
    /**
     * Constructor with field and value
     * 
     * @param field The field name (e.g., "slug")
     * @param value The field value that was not found
     */
    public DomainCategoryNotFoundException(String field, String value) {
        super(String.format("Category with %s '%s' not found", field, value));
        this.categoryId = null;
        this.field = field;
        this.value = value;
    }
    
    /**
     * Gets the category ID if the exception was thrown for an ID lookup
     * 
     * @return The category ID or null if not applicable
     */
    public UUID getCategoryId() {
        return categoryId;
    }
    
    /**
     * Gets the field name that was used for the lookup
     * 
     * @return The field name (e.g., "id", "slug")
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