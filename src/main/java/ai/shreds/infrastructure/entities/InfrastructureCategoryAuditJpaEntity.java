package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for storing audit trail of category changes.
 * Tracks all INSERT, UPDATE, and DELETE operations on categories.
 */
@Entity
@Table(name = "category_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureCategoryAuditJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

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
     * Creates an audit record for category insertion
     * @param categoryId the category ID
     * @param afterState the state after creation
     * @param changedBy the user who created the category
     * @return audit entity for insertion
     */
    public static InfrastructureCategoryAuditJpaEntity forInsert(UUID categoryId, String afterState, String changedBy) {
        return InfrastructureCategoryAuditJpaEntity.builder()
                .categoryId(categoryId)
                .changeType(ChangeType.INSERT)
                .beforeState(null)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for category update
     * @param categoryId the category ID
     * @param beforeState the state before update
     * @param afterState the state after update
     * @param changedBy the user who updated the category
     * @return audit entity for update
     */
    public static InfrastructureCategoryAuditJpaEntity forUpdate(UUID categoryId, String beforeState, String afterState, String changedBy) {
        return InfrastructureCategoryAuditJpaEntity.builder()
                .categoryId(categoryId)
                .changeType(ChangeType.UPDATE)
                .beforeState(beforeState)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for category deletion
     * @param categoryId the category ID
     * @param beforeState the state before deletion
     * @param changedBy the user who deleted the category
     * @return audit entity for deletion
     */
    public static InfrastructureCategoryAuditJpaEntity forDelete(UUID categoryId, String beforeState, String changedBy) {
        return InfrastructureCategoryAuditJpaEntity.builder()
                .categoryId(categoryId)
                .changeType(ChangeType.DELETE)
                .beforeState(beforeState)
                .afterState(null)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }
}
