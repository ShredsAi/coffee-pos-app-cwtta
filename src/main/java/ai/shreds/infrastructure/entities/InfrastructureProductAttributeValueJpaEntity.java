package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * JPA entity representing a product attribute value in the database.
 * This is part of the EAV (Entity-Attribute-Value) pattern for flexible product attributes.
 */
@Entity
@Table(name = "product_attribute_value")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureProductAttributeValueJpaEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", insertable = false, updatable = false)
    private InfrastructureProductAttributeJpaEntity attribute;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "numeric_value", precision = 19, scale = 4)
    private BigDecimal numericValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "date_value")
    private LocalDate dateValue;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "attribute_value_option",
        joinColumns = @JoinColumn(name = "attribute_value_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    @Builder.Default
    private List<InfrastructureAttributeOptionJpaEntity> selectedOptions = new ArrayList<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    /**
     * Helper method to get the actual value based on attribute type
     * @return the value as Object
     */
    public Object getValue() {
        if (textValue != null) return textValue;
        if (numericValue != null) return numericValue;
        if (booleanValue != null) return booleanValue;
        if (dateValue != null) return dateValue;
        if (selectedOptions != null && !selectedOptions.isEmpty()) {
            return selectedOptions.size() == 1 
                ? selectedOptions.get(0).getValue() 
                : selectedOptions.stream().map(InfrastructureAttributeOptionJpaEntity::getValue).toList();
        }
        return null;
    }

    /**
     * Helper method to set value based on type
     * @param value the value to set
     */
    public void setValue(Object value) {
        // Clear all existing values
        clearValues();
        
        if (value == null) {
            return;
        }
        
        if (value instanceof String) {
            textValue = (String) value;
        } else if (value instanceof Number) {
            numericValue = BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            booleanValue = (Boolean) value;
        } else if (value instanceof LocalDate) {
            dateValue = (LocalDate) value;
        }
    }

    /**
     * Clears all value fields
     */
    public void clearValues() {
        textValue = null;
        numericValue = null;
        booleanValue = null;
        dateValue = null;
        if (selectedOptions != null) {
            selectedOptions.clear();
        }
    }

    /**
     * Checks if this attribute value has any value set
     * @return true if any value is set
     */
    public boolean hasValue() {
        return textValue != null || numericValue != null || booleanValue != null || 
               dateValue != null || (selectedOptions != null && !selectedOptions.isEmpty());
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
    }

    /**
     * Pre-update hook to update the updatedAt timestamp
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
