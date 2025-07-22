package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event DTO for warehouse status changes.
 * Published when a warehouse's active status changes in the system.
 * Contains both old and new status values for comparison.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SharedWarehouseStatusChangedEventDTO extends SharedEventDTO {

    @NotNull(message = "Warehouse ID must not be null")
    private UUID warehouseId;
    
    @NotNull(message = "Old status must not be null")
    private Boolean oldStatus;
    
    @NotNull(message = "New status must not be null")
    private Boolean newStatus;
    
    @NotNull(message = "Timestamp must not be null")
    private LocalDateTime timestamp;
    
    /**
     * Creates a warehouse status changed event with default event metadata.
     *
     * @param warehouseId ID of the warehouse whose status changed
     * @param oldStatus Status before the change
     * @param newStatus Status after the change
     * @return A new warehouse status changed event
     */
    public static SharedWarehouseStatusChangedEventDTO create(
            UUID warehouseId,
            Boolean oldStatus,
            Boolean newStatus) {
        
        LocalDateTime now = LocalDateTime.now();
        return SharedWarehouseStatusChangedEventDTO.builder()
            .eventId(UUID.randomUUID())
            .eventType("WAREHOUSE_STATUS_CHANGED")
            .aggregateId(warehouseId)
            .aggregateType("WAREHOUSE")
            .occurredAt(now)
            .warehouseId(warehouseId)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .timestamp(now)
            .build();
    }
    
    /**
     * Checks if this event represents a warehouse activation.
     *
     * @return true if the warehouse was activated
     */
    public boolean isWarehouseActivated() {
        return Boolean.TRUE.equals(newStatus) && Boolean.FALSE.equals(oldStatus);
    }
    
    /**
     * Checks if this event represents a warehouse deactivation.
     *
     * @return true if the warehouse was deactivated
     */
    public boolean isWarehouseDeactivated() {
        return Boolean.FALSE.equals(newStatus) && Boolean.TRUE.equals(oldStatus);
    }
    
    /**
     * Checks if this event represents a status change.
     *
     * @return true if the status actually changed
     */
    public boolean isStatusChanged() {
        return !oldStatus.equals(newStatus);
    }
}