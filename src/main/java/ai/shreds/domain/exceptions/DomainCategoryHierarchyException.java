package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain Category Hierarchy Exception
 * Thrown when category hierarchy operations violate business rules
 */
public class DomainCategoryHierarchyException extends RuntimeException {
    
    private final UUID categoryId;
    private final String reason;
    
    /**
     * Constructor with message
     * 
     * @param message The error message
     */
    public DomainCategoryHierarchyException(String message) {
        super(message);
        this.categoryId = null;
        this.reason = message;
    }
    
    /**
     * Constructor with category ID and reason
     * 
     * @param categoryId The category ID involved in the hierarchy issue
     * @param reason The specific reason for the hierarchy violation
     */
    public DomainCategoryHierarchyException(UUID categoryId, String reason) {
        super(String.format("Category hierarchy violation for category '%s': %s", categoryId, reason));
        this.categoryId = categoryId;
        this.reason = reason;
    }
    
    /**
     * Gets the category ID involved in the hierarchy issue
     * 
     * @return The category ID or null if not specific to one category
     */
    public UUID getCategoryId() {
        return categoryId;
    }
    
    /**
     * Gets the specific reason for the hierarchy violation
     * 
     * @return The reason for the violation
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Factory method for cyclic reference errors
     * 
     * @param categoryId The category ID that would create a cycle
     * @return The exception instance
     */
    public static DomainCategoryHierarchyException cyclicReference(UUID categoryId) {
        return new DomainCategoryHierarchyException(categoryId, "Cyclic reference detected in category hierarchy");
    }
    
    /**
     * Factory method for maximum depth exceeded errors
     * 
     * @param categoryId The category ID that would exceed max depth
     * @param maxDepth The maximum allowed depth
     * @return The exception instance
     */
    public static DomainCategoryHierarchyException maxDepthExceeded(UUID categoryId, int maxDepth) {
        return new DomainCategoryHierarchyException(categoryId, 
            String.format("Category hierarchy cannot exceed %d levels", maxDepth));
    }
    
    /**
     * Factory method for self-parent reference errors
     * 
     * @param categoryId The category ID trying to be its own parent
     * @return The exception instance
     */
    public static DomainCategoryHierarchyException selfReference(UUID categoryId) {
        return new DomainCategoryHierarchyException(categoryId, "Category cannot be its own parent");
    }
    
    /**
     * Factory method for categories with children that cannot be deleted
     * 
     * @param categoryId The category ID with children
     * @param childCount The number of children
     * @return The exception instance
     */
    public static DomainCategoryHierarchyException hasChildren(UUID categoryId, int childCount) {
        return new DomainCategoryHierarchyException(categoryId, 
            String.format("Cannot delete category with %d children. Delete children first or reassign them.", childCount));
    }
}