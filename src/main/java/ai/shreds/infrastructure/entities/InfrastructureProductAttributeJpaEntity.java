package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

/**
 * JPA entity representing a product attribute definition in the database.
 * This defines the schema/structure of attributes that can be assigned to products.
 */
@Entity
@Table(name = "product_attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "options")
@EqualsAndHashCode(exclude = "options")
public class InfrastructureProductAttributeJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "attribute_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttributeType attributeType;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "is_filterable")
    @Builder.Default
    private Boolean isFilterable = false;

    @Column(name = "is_searchable")
    @Builder.Default
    private Boolean isSearchable = false;

    @Column(length = 50)
    private String unit;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<InfrastructureAttributeOptionJpaEntity> options = new ArrayList<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long version;

    /**
     * Enumeration for attribute types
     */
    public enum AttributeType {
        TEXT,
        NUMBER,
        BOOLEAN,
        DATE,
        SELECT_SINGLE,
        SELECT_MULTIPLE
    }

    /**
     * Helper method to check if this attribute is a select type
     * @return true if this is a select type attribute
     */
    public boolean isSelectType() {
        return attributeType == AttributeType.SELECT_SINGLE || attributeType == AttributeType.SELECT_MULTIPLE;
    }

    /**
     * Helper method to check if this attribute is numeric
     * @return true if this is a numeric attribute
     */
    public boolean isNumericType() {
        return attributeType == AttributeType.NUMBER;
    }

    /**
     * Helper method to check if this attribute supports multiple values
     * @return true if multiple values are supported
     */
    public boolean supportsMultipleValues() {
        return attributeType == AttributeType.SELECT_MULTIPLE;
    }

    /**
     * Adds an option to this attribute
     * @param option the option to add
     */
    public void addOption(InfrastructureAttributeOptionJpaEntity option) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
        option.setAttribute(this);
        option.setAttributeId(this.id);
    }

    /**
     * Removes an option from this attribute
     * @param option the option to remove
     */
    public void removeOption(InfrastructureAttributeOptionJpaEntity option) {
        if (options != null) {
            options.remove(option);
            option.setAttribute(null);
        }
    }

    /**
     * Gets active options only
     * @return list of active options
     */
    public List<InfrastructureAttributeOptionJpaEntity> getActiveOptions() {
        if (options == null) {
            return new ArrayList<>();
        }
        return options.stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsActive()))
                .toList();
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isRequired == null) {
            isRequired = false;
        }
        if (isFilterable == null) {
            isFilterable = false;
        }
        if (isSearchable == null) {
            isSearchable = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    /**
     * Pre-update hook to update the updatedAt timestamp
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
