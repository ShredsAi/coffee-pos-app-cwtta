package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainInsufficientStockException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;
import ai.shreds.shared.dtos.SharedInventoryThresholdsDTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing the inventory information for a specific product in a specific warehouse.
 * Tracks quantities, thresholds, and manages inventory adjustments.
 */
public class DomainInventoryItemEntity {
    private final UUID id;
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private DomainQuantityValue availableQuantity;
    private DomainQuantityValue reservedQuantity;
    private DomainQuantityValue allocatedQuantity;
    private DomainQuantityValue totalQuantity;
    private DomainQuantityValue safetyStockLevel;
    private DomainQuantityValue reorderPoint;
    private LocalDateTime lastMovementAt;
    private Long version;

    /**
     * Creates a new DomainInventoryItemEntity with the specified details.
     *
     * @param id the inventory item ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param availableQuantity the quantity available for reservation
     * @param reservedQuantity the quantity reserved but not yet allocated
     * @param allocatedQuantity the quantity allocated to specific batches
     * @param totalQuantity the total physical quantity
     * @param safetyStockLevel the safety stock threshold
     * @param reorderPoint the reorder point threshold
     * @param lastMovementAt the timestamp of the last stock movement
     * @param version the entity version for optimistic locking
     * @throws DomainValidationException if any validation fails
     */
    public DomainInventoryItemEntity(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue availableQuantity,
            DomainQuantityValue reservedQuantity,
            DomainQuantityValue allocatedQuantity,
            DomainQuantityValue totalQuantity,
            DomainQuantityValue safetyStockLevel,
            DomainQuantityValue reorderPoint,
            LocalDateTime lastMovementAt,
            Long version) {
        validateConstructorParameters(id, warehouseId, productId);
        validateQuantities(availableQuantity, reservedQuantity, allocatedQuantity, totalQuantity);
        validateThresholds(safetyStockLevel, reorderPoint);
        
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.totalQuantity = totalQuantity;
        this.safetyStockLevel = safetyStockLevel;
        this.reorderPoint = reorderPoint;
        this.lastMovementAt = lastMovementAt;
        this.version = version;
    }

    /**
     * Factory method to create a new InventoryItem with initial quantities set to zero.
     *
     * @param id the inventory item ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantityUnit the unit of measure for quantities
     * @param safetyStockLevel the safety stock threshold
     * @param reorderPoint the reorder point threshold
     * @return a new DomainInventoryItemEntity
     */
    public static DomainInventoryItemEntity createNew(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            String quantityUnit,
            DomainQuantityValue safetyStockLevel,
            DomainQuantityValue reorderPoint) {
        
        DomainQuantityValue zeroQuantity = new DomainQuantityValue(0, quantityUnit);
        
        return new DomainInventoryItemEntity(
                id,
                warehouseId,
                productId,
                zeroQuantity,
                zeroQuantity,
                zeroQuantity,
                zeroQuantity,
                safetyStockLevel,
                reorderPoint,
                LocalDateTime.now(),
                0L
        );
    }
    
    private void validateConstructorParameters(UUID id, UUID warehouseId, DomainProductIdValue productId) {
        if (id == null) {
            throw new DomainValidationException("Inventory item ID cannot be null", "id", null);
        }
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
    }
    
    private void validateQuantities(
            DomainQuantityValue availableQuantity,
            DomainQuantityValue reservedQuantity,
            DomainQuantityValue allocatedQuantity,
            DomainQuantityValue totalQuantity) {
            
        if (availableQuantity == null) {
            throw new DomainValidationException("Available quantity cannot be null", "availableQuantity", null);
        }
        
        if (reservedQuantity == null) {
            throw new DomainValidationException("Reserved quantity cannot be null", "reservedQuantity", null);
        }
        
        if (allocatedQuantity == null) {
            throw new DomainValidationException("Allocated quantity cannot be null", "allocatedQuantity", null);
        }
        
        if (totalQuantity == null) {
            throw new DomainValidationException("Total quantity cannot be null", "totalQuantity", null);
        }
        
        // Validate units are the same
        String unit = availableQuantity.getUnit();
        if (!reservedQuantity.getUnit().equals(unit) ||
            !allocatedQuantity.getUnit().equals(unit) ||
            !totalQuantity.getUnit().equals(unit)) {
            throw new DomainValidationException(
                "All quantity units must match", 
                "quantityUnits", 
                String.format("%s, %s, %s, %s", 
                    availableQuantity.getUnit(), 
                    reservedQuantity.getUnit(),
                    allocatedQuantity.getUnit(),
                    totalQuantity.getUnit()
                )
            );
        }
        
        // Verify available + reserved + allocated = total
        DomainQuantityValue calculatedTotal = availableQuantity
                .add(reservedQuantity)
                .add(allocatedQuantity);
                
        if (!calculatedTotal.equals(totalQuantity)) {
            throw new DomainValidationException(
                "Total quantity must equal sum of available, reserved, and allocated quantities", 
                "totalQuantity", 
                totalQuantity
            );
        }
        
        // Ensure no negative quantities
        if (availableQuantity.isNegative()) {
            throw new DomainValidationException(
                "Available quantity cannot be negative", 
                "availableQuantity", 
                availableQuantity
            );
        }
        
        if (reservedQuantity.isNegative()) {
            throw new DomainValidationException(
                "Reserved quantity cannot be negative", 
                "reservedQuantity", 
                reservedQuantity
            );
        }
        
        if (allocatedQuantity.isNegative()) {
            throw new DomainValidationException(
                "Allocated quantity cannot be negative", 
                "allocatedQuantity", 
                allocatedQuantity
            );
        }
        
        if (totalQuantity.isNegative()) {
            throw new DomainValidationException(
                "Total quantity cannot be negative", 
                "totalQuantity", 
                totalQuantity
            );
        }
    }
    
    private void validateThresholds(DomainQuantityValue safetyStockLevel, DomainQuantityValue reorderPoint) {
        if (safetyStockLevel == null) {
            throw new DomainValidationException("Safety stock level cannot be null", "safetyStockLevel", null);
        }
        
        if (reorderPoint == null) {
            throw new DomainValidationException("Reorder point cannot be null", "reorderPoint", null);
        }
        
        if (safetyStockLevel.isNegative()) {
            throw new DomainValidationException(
                "Safety stock level cannot be negative", 
                "safetyStockLevel", 
                safetyStockLevel
            );
        }
        
        if (reorderPoint.isNegative()) {
            throw new DomainValidationException(
                "Reorder point cannot be negative", 
                "reorderPoint", 
                reorderPoint
            );
        }
        
        // Reorder point should be greater than or equal to safety stock level
        if (reorderPoint.isLessThan(safetyStockLevel)) {
            throw new DomainValidationException(
                "Reorder point must be greater than or equal to safety stock level", 
                "reorderPoint", 
                reorderPoint
            );
        }
    }
    
    /**
     * Checks if the inventory is at or below safety stock level.
     *
     * @return true if available quantity is at or below safety stock level
     */
    public boolean isLowStock() {
        return availableQuantity.isLessThan(safetyStockLevel) || 
               availableQuantity.equals(safetyStockLevel);
    }
    
    /**
     * Checks if the inventory is at or below reorder point.
     *
     * @return true if available quantity is at or below reorder point
     */
    public boolean isAtReorderPoint() {
        return availableQuantity.isLessThan(reorderPoint) || 
               availableQuantity.equals(reorderPoint);
    }
    
    /**
     * Checks if there is sufficient available quantity for a requested quantity.
     *
     * @param requestedQuantity the quantity requested
     * @return true if there is enough available stock
     */
    public boolean hasSufficientAvailableQuantity(DomainQuantityValue requestedQuantity) {
        if (requestedQuantity == null) {
            throw new DomainValidationException("Requested quantity cannot be null", "requestedQuantity", null);
        }
        
        return availableQuantity.isGreaterThanOrEqual(requestedQuantity);
    }

    /**
     * Checks if a quantity can be reserved from available stock.
     *
     * @param requestedQuantity the quantity to reserve
     * @return true if the quantity can be reserved
     */
    public boolean canReserve(DomainQuantityValue requestedQuantity) {
        return hasSufficientAvailableQuantity(requestedQuantity);
    }

    /**
     * Adjusts inventory quantities based on a stock movement.
     * Updates available, total, and last movement timestamp.
     *
     * @param movement the stock movement entity
     * @throws DomainInsufficientStockException if there's not enough stock for outbound movements
     */
    public void adjustQuantity(DomainStockMovementEntity movement) {
        if (movement == null) {
            throw new DomainValidationException("Stock movement cannot be null", "movement", null);
        }
        
        DomainQuantityValue movementQuantity = movement.getQuantity();
        
        // Validate units match
        if (!movementQuantity.getUnit().equals(availableQuantity.getUnit())) {
            throw new DomainValidationException(
                "Movement quantity unit must match inventory quantity unit",
                "unit",
                movementQuantity.getUnit()
            );
        }
        
        if (movement.isInbound()) {
            // For inbound movements, simply add to available and total
            availableQuantity = availableQuantity.add(movementQuantity);
            totalQuantity = totalQuantity.add(movementQuantity);
        } else if (movement.isOutbound()) {
            // For outbound, check if enough stock is available
            if (!hasSufficientAvailableQuantity(movementQuantity)) {
                throw new DomainInsufficientStockException(
                    "Insufficient available stock for outbound movement",
                    warehouseId,
                    productId,
                    movementQuantity,
                    availableQuantity
                );
            }
            
            // Subtract from available and total
            availableQuantity = availableQuantity.subtract(movementQuantity);
            totalQuantity = totalQuantity.subtract(movementQuantity);
        }
        
        // Update the last movement timestamp
        lastMovementAt = movement.getPerformedAt();
        
        // Increment version for optimistic locking
        version = version + 1;
    }

    /**
     * Calculates the available quantity based on total, reserved, and allocated quantities.
     *
     * @return the calculated available quantity
     */
    public DomainQuantityValue calculateAvailableQuantity() {
        return totalQuantity
                .subtract(reservedQuantity)
                .subtract(allocatedQuantity);
    }

    /**
     * Updates the reserved quantity, adjusting available quantity accordingly.
     *
     * @param newReservedQuantity the new reserved quantity
     */
    public void updateReservedQuantity(DomainQuantityValue newReservedQuantity) {
        if (newReservedQuantity == null) {
            throw new DomainValidationException("New reserved quantity cannot be null", "newReservedQuantity", null);
        }
        
        if (!newReservedQuantity.getUnit().equals(reservedQuantity.getUnit())) {
            throw new DomainValidationException(
                "New reserved quantity unit must match current unit",
                "unit",
                newReservedQuantity.getUnit()
            );
        }
        
        if (newReservedQuantity.isNegative()) {
            throw new DomainValidationException(
                "Reserved quantity cannot be negative",
                "newReservedQuantity",
                newReservedQuantity
            );
        }
        
        // Calculate the difference from current reserved quantity
        DomainQuantityValue reservedDifference = newReservedQuantity.subtract(reservedQuantity);
        
        // If increasing reserved quantity, ensure enough available
        if (reservedDifference.isPositive()) {
            if (!hasSufficientAvailableQuantity(reservedDifference)) {
                throw new DomainInsufficientStockException(
                    "Insufficient available stock for reservation",
                    warehouseId,
                    productId,
                    reservedDifference,
                    availableQuantity
                );
            }
            
            // Decrease available by the difference
            availableQuantity = availableQuantity.subtract(reservedDifference);
        } else if (reservedDifference.isNegative()) {
            // If decreasing reserved, add back to available
            availableQuantity = availableQuantity.add(reservedDifference.abs());
        }
        
        // Update reserved quantity
        reservedQuantity = newReservedQuantity;
        
        // Increment version
        version = version + 1;
    }

    /**
     * Updates the allocated quantity, adjusting available quantity accordingly.
     *
     * @param newAllocatedQuantity the new allocated quantity
     */
    public void updateAllocatedQuantity(DomainQuantityValue newAllocatedQuantity) {
        if (newAllocatedQuantity == null) {
            throw new DomainValidationException("New allocated quantity cannot be null", "newAllocatedQuantity", null);
        }
        
        if (!newAllocatedQuantity.getUnit().equals(allocatedQuantity.getUnit())) {
            throw new DomainValidationException(
                "New allocated quantity unit must match current unit",
                "unit",
                newAllocatedQuantity.getUnit()
            );
        }
        
        if (newAllocatedQuantity.isNegative()) {
            throw new DomainValidationException(
                "Allocated quantity cannot be negative",
                "newAllocatedQuantity",
                newAllocatedQuantity
            );
        }
        
        // Calculate the difference from current allocated quantity
        DomainQuantityValue allocatedDifference = newAllocatedQuantity.subtract(allocatedQuantity);
        
        // If increasing allocated quantity, ensure enough available
        if (allocatedDifference.isPositive()) {
            if (!hasSufficientAvailableQuantity(allocatedDifference)) {
                throw new DomainInsufficientStockException(
                    "Insufficient available stock for allocation",
                    warehouseId,
                    productId,
                    allocatedDifference,
                    availableQuantity
                );
            }
            
            // Decrease available by the difference
            availableQuantity = availableQuantity.subtract(allocatedDifference);
        } else if (allocatedDifference.isNegative()) {
            // If decreasing allocated, add back to available
            availableQuantity = availableQuantity.add(allocatedDifference.abs());
        }
        
        // Update allocated quantity
        allocatedQuantity = newAllocatedQuantity;
        
        // Increment version
        version = version + 1;
    }
    
    /**
     * Updates the safety stock level and reorder point.
     *
     * @param newSafetyStockLevel the new safety stock level
     * @param newReorderPoint the new reorder point
     */
    public void updateThresholds(DomainQuantityValue newSafetyStockLevel, DomainQuantityValue newReorderPoint) {
        validateThresholds(newSafetyStockLevel, newReorderPoint);
        
        this.safetyStockLevel = newSafetyStockLevel;
        this.reorderPoint = newReorderPoint;
        
        // Increment version
        version = version + 1;
    }

    /**
     * Converts this domain entity to a DTO for the application layer.
     *
     * @return a SharedInventoryItemResponseDTO representation
     */
    public SharedInventoryItemResponseDTO toDTO() {
        return SharedInventoryItemResponseDTO.builder()
                .id(id)
                .warehouseId(warehouseId)
                .productId(productId.toUUID())
                .quantities(createQuantitiesDTO())
                .thresholds(createThresholdsDTO())
                .lastMovementAt(lastMovementAt)
                .version(version)
                .build();
    }
    
    private SharedInventoryQuantitiesDTO createQuantitiesDTO() {
        return SharedInventoryQuantitiesDTO.builder()
                .availableQty(availableQuantity.getValue())
                .reservedQty(reservedQuantity.getValue())
                .allocatedQty(allocatedQuantity.getValue())
                .totalQty(totalQuantity.getValue())
                .qtyUnit(availableQuantity.getUnit())
                .build();
    }
    
    private SharedInventoryThresholdsDTO createThresholdsDTO() {
        return SharedInventoryThresholdsDTO.builder()
                .safetyStockLevel(safetyStockLevel.getValue())
                .reorderPoint(reorderPoint.getValue())
                .build();
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

    public DomainQuantityValue getAvailableQuantity() {
        return availableQuantity;
    }

    public DomainQuantityValue getReservedQuantity() {
        return reservedQuantity;
    }

    public DomainQuantityValue getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public DomainQuantityValue getTotalQuantity() {
        return totalQuantity;
    }

    public DomainQuantityValue getSafetyStockLevel() {
        return safetyStockLevel;
    }

    public DomainQuantityValue getReorderPoint() {
        return reorderPoint;
    }

    public LocalDateTime getLastMovementAt() {
        return lastMovementAt;
    }

    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainInventoryItemEntity that = (DomainInventoryItemEntity) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "DomainInventoryItemEntity{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", available=" + availableQuantity +
                ", reserved=" + reservedQuantity +
                ", allocated=" + allocatedQuantity +
                ", total=" + totalQuantity +
                ", version=" + version +
                '}';
    }
}