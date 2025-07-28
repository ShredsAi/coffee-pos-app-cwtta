package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for storing audit trail of product media changes.
 * Tracks all INSERT, UPDATE, and DELETE operations on product media.
 */
@Entity
@Table(name = "media_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureMediaAuditJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

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
     * Creates an audit record for media insertion
     * @param mediaId the media ID
     * @param afterState the state after creation
     * @param changedBy the user who created the media
     * @return audit entity for insertion
     */
    public static InfrastructureMediaAuditJpaEntity forInsert(UUID mediaId, String afterState, String changedBy) {
        return InfrastructureMediaAuditJpaEntity.builder()
                .mediaId(mediaId)
                .changeType(ChangeType.INSERT)
                .beforeState(null)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for media update
     * @param mediaId the media ID
     * @param beforeState the state before update
     * @param afterState the state after update
     * @param changedBy the user who updated the media
     * @return audit entity for update
     */
    public static InfrastructureMediaAuditJpaEntity forUpdate(UUID mediaId, String beforeState, String afterState, String changedBy) {
        return InfrastructureMediaAuditJpaEntity.builder()
                .mediaId(mediaId)
                .changeType(ChangeType.UPDATE)
                .beforeState(beforeState)
                .afterState(afterState)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates an audit record for media deletion
     * @param mediaId the media ID
     * @param beforeState the state before deletion
     * @param changedBy the user who deleted the media
     * @return audit entity for deletion
     */
    public static InfrastructureMediaAuditJpaEntity forDelete(UUID mediaId, String beforeState, String changedBy) {
        return InfrastructureMediaAuditJpaEntity.builder()
                .mediaId(mediaId)
                .changeType(ChangeType.DELETE)
                .beforeState(beforeState)
                .afterState(null)
                .changedAt(Instant.now())
                .changedBy(changedBy)
                .build();
    }
}
