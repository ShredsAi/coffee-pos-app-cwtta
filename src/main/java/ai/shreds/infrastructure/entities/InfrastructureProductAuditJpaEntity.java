package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for storing audit trail of product changes.
 * Tracks all INSERT, UPDATE, and DELETE operations on products.
 */
@Entity
@Table(name = "product_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureProductAuditJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "change_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    /**
     * Enumeration for change types
     */
    public enum ChangeType {
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * Helper method to check if this is an insert operation
     * @return true if this is an insert audit record
     */
    public boolean isInsert() {
        return changeType == ChangeType.INSERT;
    }

    /**
     * Helper method to check if this is an update operation
     * @return true if this is an update audit record
     */
    public boolean isUpdate() {
        return changeType == ChangeType.UPDATE;
    }

    /**
     * Helper method to check if this is a delete operation
     * @return true if this is a delete audit record
     */
    public boolean isDelete() {
        return changeType == ChangeType.DELETE;
    }

    /**
     * Gets the user who made the change, or 'system' if unknown
     * @return the user who made the change
     */
    public String getChangedByOrSystem() {
        return changedBy != null && !changedBy.trim().isEmpty() ? changedBy : "system";
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
        if (changedBy == null || changedBy.trim().isEmpty()) {
            changedBy = "system";
        }
    }

    /**
     * Creates an audit record for product insertion
     * @param productId the product ID
     * @param afterState the state after creation
     * @param changedBy the user who created the product
     * @return audit entity for insertion
     */
    public static InfrastructureProductAuditJpaEntity forInsert(UUID productId, String afterState, String changedBy) {
        return InfrastructureProductAuditJpaEntity.builder()
                .productId(productId)
                .changeType(ChangeType.INSERT)
                .beforeState(null)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for product update
     * @param productId the product ID
     * @param beforeState the state before update
     * @param afterState the state after update
     * @param changedBy the user who updated the product
     * @return audit entity for update
     */
    public static InfrastructureProductAuditJpaEntity forUpdate(UUID productId, String beforeState, String afterState, String changedBy) {
        return InfrastructureProductAuditJpaEntity.builder()
                .productId(productId)
                .changeType(ChangeType.UPDATE)
                .beforeState(beforeState)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for product deletion
     * @param productId the product ID
     * @param beforeState the state before deletion
     * @param changedBy the user who deleted the product
     * @return audit entity for deletion
     */
    public static InfrastructureProductAuditJpaEntity forDelete(UUID productId, String beforeState, String changedBy) {
        return InfrastructureProductAuditJpaEntity.builder()
                .productId(productId)
                .changeType(ChangeType.DELETE)
                .beforeState(beforeState)
                .afterState(null)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Validates the audit record
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (changeType == null) {
            throw new IllegalArgumentException("Change type cannot be null");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Changed at timestamp cannot be null");
        }
        
        // Validate state presence based on operation type
        switch (changeType) {
            case INSERT:
                if (afterState == null || afterState.trim().isEmpty()) {
                    throw new IllegalArgumentException("After state is required for INSERT operations");
                }
                break;
            case UPDATE:
                if (beforeState == null || beforeState.trim().isEmpty() || 
                    afterState == null || afterState.trim().isEmpty()) {
                    throw new IllegalArgumentException("Both before and after states are required for UPDATE operations");
                }
                break;
            case DELETE:
                if (beforeState == null || beforeState.trim().isEmpty()) {
                    throw new IllegalArgumentException("Before state is required for DELETE operations");
                }
                break;
        }
    }
}
