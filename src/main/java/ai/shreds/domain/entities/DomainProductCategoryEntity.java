package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedCategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Product Category Entity
 * Represents the many-to-many relationship between products and categories
 * Junction table entity with additional metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainProductCategoryEntity {
    
    private UUID id;
    private UUID productId;
    private UUID categoryId;
    private DomainCategoryEntity category; // Reference to the actual category
    private Boolean isPrimary;
    private Instant assignedAt;
    
    /**
     * Validates the product-category association
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        
        if (isPrimary == null) {
            isPrimary = false;
        }
        
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
    
    /**
     * Sets this category as the primary category for the product
     */
    public void setPrimary() {
        this.isPrimary = true;
    }
    
    /**
     * Removes the primary flag from this category association
     */
    public void removePrimary() {
        this.isPrimary = false;
    }
    
    /**
     * Checks if this is the primary category for the product
     */
    public boolean isPrimaryCategory() {
        return Boolean.TRUE.equals(this.isPrimary);
    }
    
    /**
     * Gets the category name (if category entity is loaded)
     */
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
    
    /**
     * Gets the category path (if category entity is loaded)
     */
    public String getCategoryPath() {
        return category != null ? category.getPath() : null;
    }
    
    /**
     * Gets the category level (if category entity is loaded)
     */
    public Integer getCategoryLevel() {
        return category != null ? category.getLevel() : null;
    }
    
    /**
     * Checks if the associated category is active (if category entity is loaded)
     */
    public boolean isCategoryActive() {
        return category != null && Boolean.TRUE.equals(category.getIsActive());
    }
    
    /**
     * Updates the category reference
     */
    public void updateCategory(DomainCategoryEntity newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (!newCategory.getId().equals(this.categoryId)) {
            throw new IllegalArgumentException("Category ID mismatch");
        }
        
        this.category = newCategory;
    }
    
    /**
     * Converts to SharedCategoryDTO (using the category reference)
     */
    public SharedCategoryDTO toDTO() {
        return category != null ? category.toDTO() : null;
    }
    
    /**
     * Factory method to create a new product-category association
     */
    public static DomainProductCategoryEntity create(UUID productId, UUID categoryId, Boolean isPrimary) {
        DomainProductCategoryEntity association = DomainProductCategoryEntity.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .categoryId(categoryId)
            .isPrimary(isPrimary != null ? isPrimary : false)
            .assignedAt(Instant.now())
            .build();
        
        association.validate();
        return association;
    }
    
    /**
     * Factory method to create with category entity reference
     */
    public static DomainProductCategoryEntity create(UUID productId, DomainCategoryEntity category, Boolean isPrimary) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        DomainProductCategoryEntity association = DomainProductCategoryEntity.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .categoryId(category.getId())
            .category(category)
            .isPrimary(isPrimary != null ? isPrimary : false)
            .assignedAt(Instant.now())
            .build();
        
        association.validate();
        return association;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DomainProductCategor­yEntity that = (DomainProductCategoryEntity) obj;
        return productId.equals(that.productId) && categoryId.equals(that.categoryId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(productId, categoryId);
    }
}