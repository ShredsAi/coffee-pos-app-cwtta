package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing the event_outbox table for the transactional outbox pattern.
 * Stores events that need to be published to external systems in a reliable manner.
 */
@Entity
@Table(
    name = "event_outbox",
    indexes = {
        @Index(columnList = "processed, created_at", name = "idx_event_outbox_processed_created"),
        @Index(columnList = "aggregate_id", name = "idx_event_outbox_aggregate_id"),
        @Index(columnList = "aggregate_type", name = "idx_event_outbox_aggregate_type"),
        @Index(columnList = "event_type", name = "idx_event_outbox_event_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureEventOutboxJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    
    @Column(name = "aggregate_type", length = 64, nullable = false)
    private String aggregateType;
    
    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;
    
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload; // JSON string representation
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed", nullable = false)
    private Boolean processed = false;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.processed == null) {
            this.processed = false;
        }
    }
    
    /**
     * Marks this event as processed with the current timestamp.
     */
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Resets this event to unprocessed state.
     */
    public void resetToUnprocessed() {
        this.processed = false;
        this.processedAt = null;
    }
    
    /**
     * Checks if this event is processed.
     * @return true if the event has been processed
     */
    public boolean isProcessed() {
        return Boolean.TRUE.equals(this.processed);
    }
    
    /**
     * Checks if this event is expired (unprocessed for too long).
     * @param maxAgeMinutes maximum age in minutes before considering expired
     * @return true if the event is expired
     */
    public boolean isExpired(int maxAgeMinutes) {
        if (isProcessed()) {
            return false;
        }
        LocalDateTime expirationTime = this.createdAt.plusMinutes(maxAgeMinutes);
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
