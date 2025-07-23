package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainAllocationStatusEnum;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a stock allocation.
 * Allocations track inventory that has been allocated to specific batches for fulfillment.
 * Can transition through various states: PENDING -> CONFIRMED -> SHIPPED or CANCELLED
 */
public class DomainAllocationEntity {
    private final UUID id;
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final DomainQuantityValue quantity;
    private final String allocatedTo;
    private DomainAllocationStatusEnum status;
    private final UUID batchId;
    private final LocalDateTime allocatedAt;
    private LocalDateTime expectedShipmentAt;

    /**
     * Constructs a new allocation entity.
     *
     * @param id the allocation ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the allocated quantity
     * @param allocatedTo identifier of who/what the stock is allocated to
     * @param status the allocation status
     * @param batchId the batch the stock is allocated from
     * @param allocatedAt when the allocation was created
     * @param expectedShipmentAt when the shipment is expected
     */
    public DomainAllocationEntity(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String allocatedTo,
            DomainAllocationStatusEnum status,
            UUID batchId,
            LocalDateTime allocatedAt,
            LocalDateTime expectedShipmentAt) {
        
        validateConstructorParameters(id, warehouseId, productId, quantity, 
                allocatedTo, status, batchId, allocatedAt);
        
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.quantity = quantity;
        this.allocatedTo = allocatedTo;
        this.status = status;
        this.batchId = batchId;
        this.allocatedAt = allocatedAt;
        this.expectedShipmentAt = expectedShipmentAt;
    }
    
    private void validateConstructorParameters(
            UUID id, UUID warehouseId, DomainProductIdValue productId, 
            DomainQuantityValue quantity, String allocatedTo, 
            DomainAllocationStatusEnum status, UUID batchId, 
            LocalDateTime allocatedAt) {
        
        if (id == null) {
            throw new DomainValidationException("Allocation ID cannot be null", "id", null);
        }
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null", "quantity", null);
        }
        
        if (quantity.isNegative() || quantity.isZero()) {
            throw new DomainValidationException("Allocation quantity must be positive", "quantity", quantity);
        }
        
        if (allocatedTo == null || allocatedTo.trim().isEmpty()) {
            throw new DomainValidationException("Allocated to cannot be null or empty", "allocatedTo", allocatedTo);
        }
        
        if (status == null) {
            throw new DomainValidationException("Status cannot be null", "status", null);
        }
        
        if (batchId == null) {
            throw new DomainValidationException("Batch ID cannot be null", "batchId", null);
        }
        
        if (allocatedAt == null) {
            throw new DomainValidationException("Allocated at cannot be null", "allocatedAt", null);
        }
        
        if (allocatedAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException(
                "Allocated at cannot be in the future", 
                "allocatedAt", 
                allocatedAt
            );
        }
    }
    
    /**
     * Factory method to create a new pending allocation.
     *
     * @param id the allocation ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the quantity to allocate
     * @param allocatedTo identifier of who/what the stock is allocated to
     * @param batchId the batch the stock is allocated from
     * @param expectedShipmentAt when the shipment is expected
     * @return a new pending allocation
     */
    public static DomainAllocationEntity createNew(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String allocatedTo,
            UUID batchId,
            LocalDateTime expectedShipmentAt) {
        
        LocalDateTime now = LocalDateTime.now();
        
        return new DomainAllocationEntity(
                id,
                warehouseId,
                productId,
                quantity,
                allocatedTo,
                DomainAllocationStatusEnum.PENDING,
                batchId,
                now,
                expectedShipmentAt
        );
    }
    
    /**
     * Factory method to create a confirmed allocation directly.
     * Typically used when converting from a reservation.
     *
     * @param id the allocation ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the quantity to allocate
     * @param allocatedTo identifier of who/what the stock is allocated to
     * @param batchId the batch the stock is allocated from
     * @param expectedShipmentAt when the shipment is expected
     * @return a new confirmed allocation
     */
    public static DomainAllocationEntity createConfirmed(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String allocatedTo,
            UUID batchId,
            LocalDateTime expectedShipmentAt) {
        
        LocalDateTime now = LocalDateTime.now();
        
        return new DomainAllocationEntity(
                id,
                warehouseId,
                productId,
                quantity,
                allocatedTo,
                DomainAllocationStatusEnum.CONFIRMED,
                batchId,
                now,
                expectedShipmentAt
        );
    }

    /**
     * Updates the expected shipment date.
     * Can only update pending or confirmed allocations.
     *
     * @param newExpectedShipmentAt the new expected shipment date
     */
    public void updateExpectedShipmentDate(LocalDateTime newExpectedShipmentAt) {
        if (!status.isHoldingStock()) {
            throw new DomainValidationException(
                "Cannot update expected shipment date for allocation in status: " + status,
                "status",
                status
            );
        }
        
        if (newExpectedShipmentAt == null) {
            throw new DomainValidationException("Expected shipment date cannot be null", "newExpectedShipmentAt", null);
        }
        
        if (newExpectedShipmentAt.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException(
                "Expected shipment date cannot be in the past",
                "newExpectedShipmentAt",
                newExpectedShipmentAt
            );
        }
        
        this.expectedShipmentAt = newExpectedShipmentAt;
    }

    /**
     * Confirms the allocation, indicating that picking/packing has started.
     * Can only confirm pending allocations.
     */
    public void confirm() {
        if (!status.canBeConfirmed()) {
            throw new DomainValidationException(
                String.format("Cannot confirm allocation with status %s", status),
                "status",
                status
            );
        }
        
        this.status = DomainAllocationStatusEnum.CONFIRMED;
    }

    /**
     * Marks the allocation as shipped, indicating that the goods have left the warehouse.
     * Can only ship confirmed allocations.
     */
    public void ship() {
        if (!status.canBeShipped()) {
            throw new DomainValidationException(
                String.format("Cannot ship allocation with status %s", status),
                "status",
                status
            );
        }
        
        this.status = DomainAllocationStatusEnum.SHIPPED;
    }

    /**
     * Cancels the allocation, releasing the allocated stock.
     * Can only cancel pending or confirmed allocations.
     */
    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new DomainValidationException(
                String.format("Cannot cancel allocation with status %s", status),
                "status",
                status
            );
        }
        
        this.status = DomainAllocationStatusEnum.CANCELLED;
    }
    
    /**
     * Checks if the allocation is overdue for shipment.
     *
     * @return true if the allocation is confirmed and past its expected shipment date
     */
    public boolean isOverdueForShipment() {
        return status == DomainAllocationStatusEnum.CONFIRMED && 
               expectedShipmentAt != null && 
               LocalDateTime.now().isAfter(expectedShipmentAt);
    }
    
    /**
     * Checks if the allocation has been pending for too long.
     *
     * @param maxPendingHours maximum hours an allocation should remain pending
     * @return true if the allocation has been pending for longer than maxPendingHours
     */
    public boolean isPendingTooLong(int maxPendingHours) {
        if (status != DomainAllocationStatusEnum.PENDING) {
            return false;
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusHours(maxPendingHours);
        return allocatedAt.isBefore(threshold);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public DomainProductIdValue getProductId() {
        return productId;
    }

    public DomainQuantityValue getQuantity() {
        return quantity;
    }

    public String getAllocatedTo() {
        return allocatedTo;
    }

    public DomainAllocationStatusEnum getStatus() {
        return status;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public LocalDateTime getExpectedShipmentAt() {
        return expectedShipmentAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainAllocationEntity that = (DomainAllocationEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DomainAllocationEntity{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", allocatedTo='" + allocatedTo + '\'' +
                ", status=" + status +
                ", batchId=" + batchId +
                ", allocatedAt=" + allocatedAt +
                '}';
    }
}