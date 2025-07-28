package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing the many-to-many relationship between products and categories.
 * This is a junction table entity that links products to categories with additional metadata.
 */
@Entity
@Table(name = "product_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "category")
@EqualsAndHashCode(exclude = "category")
public class InfrastructureProductCategoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private InfrastructureCategoryJpaEntity category;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    /**
     * Helper method to check if this is the primary category assignment
     * @return true if this is the primary category for the product
     */
    public boolean isPrimaryCategory() {
        return Boolean.TRUE.equals(isPrimary);
    }

    /**
     * Sets this category assignment as primary
     * Note: The calling code should ensure only one primary category per product
     */
    public void setPrimary() {
        this.isPrimary = true;
    }

    /**
     * Removes the primary flag from this category assignment
     */
    public void removePrimary() {
        this.isPrimary = false;
    }

    /**
     * Gets the category name if category is loaded
     * @return category name or null if not loaded
     */
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    /**
     * Gets the category slug if category is loaded
     * @return category slug or null if not loaded
     */
    public String getCategorySlug() {
        return category != null ? category.getSlug() : null;
    }

    /**
     * Gets the category path if category is loaded
     * @return category path or null if not loaded
     */
    public String getCategoryPath() {
        return category != null ? category.getPath() : null;
    }

    /**
     * Gets the category level if category is loaded
     * @return category level or null if not loaded
     */
    public Integer getCategoryLevel() {
        return category != null ? category.getLevel() : null;
    }

    /**
     * Validates the product-category relationship
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (productId.equals(categoryId)) {
            throw new IllegalArgumentException("Product ID and Category ID cannot be the same");
        }
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
        validate();
    }

    /**
     * Pre-update hook for validation
     */
    @PreUpdate
    public void preUpdate() {
        validate();
    }

    /**
     * Creates a new product-category association
     * @param productId the product ID
     * @param categoryId the category ID
     * @param isPrimary whether this is the primary category
     * @return new product-category entity
     */
    public static InfrastructureProductCategoryJpaEntity create(UUID productId, UUID categoryId, boolean isPrimary) {
        return InfrastructureProductCategoryJpaEntity.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .categoryId(categoryId)
                .isPrimary(isPrimary)
                .assignedAt(Instant.now())
                .build();
    }

    /**
     * Creates a new primary product-category association
     * @param productId the product ID
     * @param categoryId the category ID
     * @return new primary product-category entity
     */
    public static InfrastructureProductCategoryJpaEntity createPrimary(UUID productId, UUID categoryId) {
        return create(productId, categoryId, true);
    }

    /**
     * Creates a new secondary product-category association
     * @param productId the product ID
     * @param categoryId the category ID
     * @return new secondary product-category entity
     */
    public static InfrastructureProductCategoryJpaEntity createSecondary(UUID productId, UUID categoryId) {
        return create(productId, categoryId, false);
    }
}
