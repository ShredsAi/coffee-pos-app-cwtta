package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing an attribute option in the database.
 * Used for select-type attributes to define the available choices.
 */
@Entity
@Table(name = "attribute_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "attribute")
@EqualsAndHashCode(exclude = "attribute")
public class InfrastructureAttributeOptionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", insertable = false, updatable = false)
    private InfrastructureProductAttributeJpaEntity attribute;

    @Column(name = "option_value", nullable = false)
    private String value;

    @Column(length = 100)
    private String code;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

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
     * Gets the display value for this option.
     * Returns the value field as it's the human-readable display text.
     * @return the display value
     */
    public String getDisplayValue() {
        return value;
    }

    /**
     * Gets the technical code for this option.
     * If no code is set, returns the value as fallback.
     * @return the technical code or value as fallback
     */
    public String getCodeOrValue() {
        return code != null && !code.trim().isEmpty() ? code : value;
    }

    /**
     * Checks if this option has a separate code different from its value
     * @return true if code is set and different from value
     */
    public boolean hasCustomCode() {
        return code != null && !code.trim().isEmpty() && !code.equals(value);
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
        if (sortOrder == null) {
            sortOrder = 0;
        }
        // If no code is provided, generate one from value
        if (code == null || code.trim().isEmpty()) {
            code = generateCodeFromValue(value);
        }
    }

    /**
     * Pre-update hook to update the updatedAt timestamp
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Generates a technical code from the display value
     * @param value the display value
     * @return generated code
     */
    private String generateCodeFromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "OPTION_" + System.currentTimeMillis();
        }
        
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase();
    }

    /**
     * Validates this option
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Option value cannot be null or empty");
        }
        if (attributeId == null) {
            throw new IllegalArgumentException("Attribute ID cannot be null");
        }
    }
}