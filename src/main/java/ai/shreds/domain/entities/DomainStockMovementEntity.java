package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainReferenceTypeEnum;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.exceptions.DomainInvalidMovementTypeException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a stock movement transaction in the warehouse.
 * Captures details about product movement including type, quantity, batch, reason and audit information.
 */
public class DomainStockMovementEntity {
    private final UUID id;
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final DomainStockMovementTypeEnum movementType;
    private final DomainQuantityValue quantity;
    private final String referenceId;
    private final String referenceType;
    private final UUID batchId;
    private final String reason;
    private final String performedBy;
    private final LocalDateTime performedAt;
    private final DomainMoneyValue costPerUnit;
    private final LocalDateTime createdAt;

    /**
     * Constructs a new stock movement with comprehensive validation.
     *
     * @param id the unique identifier for the movement
     * @param warehouseId the warehouse where movement occurs
     * @param productId the product being moved
     * @param movementType the type of movement (INBOUND, OUTBOUND, etc.)
     * @param quantity the quantity being moved
     * @param referenceId reference to an external document ID (optional)
     * @param referenceType type of external reference (optional)
     * @param batchId the batch affected by this movement (optional)
     * @param reason explanation for the movement (required for some types)
     * @param performedBy user or system that performed movement
     * @param performedAt when the movement occurred
     * @param costPerUnit cost information (optional)
     * @param createdAt when the record was created
     */
    public DomainStockMovementEntity(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainStockMovementTypeEnum movementType,
            DomainQuantityValue quantity,
            String referenceId,
            String referenceType,
            UUID batchId,
            String reason,
            String performedBy,
            LocalDateTime performedAt,
            DomainMoneyValue costPerUnit,
            LocalDateTime createdAt) {
        
        validateConstructorParameters(
            id, warehouseId, productId, movementType, quantity, 
            reason, performedBy, performedAt, createdAt
        );
        
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceId = referenceId; // Optional
        this.referenceType = referenceType; // Optional
        this.batchId = batchId; // Optional
        this.reason = reason;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.costPerUnit = costPerUnit; // Optional
        this.createdAt = createdAt;
    }
    
    private void validateConstructorParameters(
            UUID id, UUID warehouseId, DomainProductIdValue productId,
            DomainStockMovementTypeEnum movementType, DomainQuantityValue quantity,
            String reason, String performedBy, LocalDateTime performedAt,
            LocalDateTime createdAt) {
        
        if (id == null) {
            throw new DomainValidationException("Stock movement ID cannot be null", "id", null);
        }
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (movementType == null) {
            throw new DomainValidationException("Movement type cannot be null", "movementType", null);
        }
        
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null", "quantity", null);
        }
        
        if (quantity.isZero()) {
            throw new DomainValidationException("Quantity cannot be zero", "quantity", quantity);
        }
        
        // Validate that movement type and quantity align
        validateMovementTypeAndQuantity(movementType, quantity);
        
        // Reason is required for adjustments, optional for others
        if (movementType.requiresReason() && (reason == null || reason.trim().isEmpty())) {
            throw new DomainValidationException(
                "Reason is required for " + movementType + " movements", 
                "reason", 
                reason
            );
        }
        
        if (performedBy == null || performedBy.trim().isEmpty()) {
            throw new DomainValidationException("Performed by cannot be null or empty", "performedBy", performedBy);
        }
        
        if (performedAt == null) {
            throw new DomainValidationException("Performed at cannot be null", "performedAt", null);
        }
        
        if (performedAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("Performed at cannot be in the future", "performedAt", performedAt);
        }
        
        if (createdAt == null) {
            throw new DomainValidationException("Created at cannot be null", "createdAt", null);
        }
        
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("Created at cannot be in the future", "createdAt", createdAt);
        }
    }
    
    private void validateMovementTypeAndQuantity(DomainStockMovementTypeEnum movementType, DomainQuantityValue quantity) {
        // For INBOUND movements, quantity must be positive
        if (movementType == DomainStockMovementTypeEnum.INBOUND && quantity.isNegative()) {
            throw new DomainInvalidMovementTypeException(
                "INBOUND movement must have positive quantity", 
                movementType.name()
            );
        }
        
        // For OUTBOUND movements, quantity must be positive (system will apply negative effect)
        if (movementType == DomainStockMovementTypeEnum.OUTBOUND && quantity.isNegative()) {
            throw new DomainInvalidMovementTypeException(
                "OUTBOUND movement must have positive quantity (system will negate for calculation)", 
                movementType.name()
            );
        }
        
        // For TRANSFER movements, quantity must be positive (system will apply negative effect) 
        if (movementType == DomainStockMovementTypeEnum.TRANSFER && quantity.isNegative()) {
            throw new DomainInvalidMovementTypeException(
                "TRANSFER movement must have positive quantity (system will negate for calculation)", 
                movementType.name()
            );
        }
        
        // For ADJUSTMENT, quantity can be either positive or negative (directly applies effect)
        // No validation needed
    }
    
    /**
     * Factory method for creating an inbound stock movement.
     *
     * @param id the movement ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the inbound quantity (must be positive)
     * @param referenceId optional reference document ID 
     * @param referenceType optional reference document type
     * @param batchId optional batch ID
     * @param performedBy who performed the movement
     * @param costPerUnit optional cost per unit
     * @param now current timestamp
     * @return a new inbound stock movement entity
     */
    public static DomainStockMovementEntity createInbound(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String referenceId,
            DomainReferenceTypeEnum referenceType,
            UUID batchId,
            String performedBy,
            DomainMoneyValue costPerUnit,
            LocalDateTime now) {
        
        if (quantity.isNegative() || quantity.isZero()) {
            throw new DomainValidationException(
                "Inbound quantity must be positive",
                "quantity",
                quantity
            );
        }
        
        return new DomainStockMovementEntity(
                id,
                warehouseId,
                productId,
                DomainStockMovementTypeEnum.INBOUND,
                quantity,
                referenceId,
                referenceType != null ? referenceType.name() : null,
                batchId,
                null, // Reason not required for inbound
                performedBy,
                now,
                costPerUnit,
                now
        );
    }
    
    /**
     * Factory method for creating an outbound stock movement.
     *
     * @param id the movement ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the outbound quantity (must be positive, system will apply negative effect)
     * @param referenceId optional reference document ID
     * @param referenceType optional reference document type
     * @param batchId optional batch ID
     * @param performedBy who performed the movement
     * @param now current timestamp
     * @return a new outbound stock movement entity
     */
    public static DomainStockMovementEntity createOutbound(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String referenceId,
            DomainReferenceTypeEnum referenceType,
            UUID batchId,
            String performedBy,
            LocalDateTime now) {
        
        if (quantity.isNegative() || quantity.isZero()) {
            throw new DomainValidationException(
                "Outbound quantity must be positive (system will negate for calculation)",
                "quantity",
                quantity
            );
        }
        
        return new DomainStockMovementEntity(
                id,
                warehouseId,
                productId,
                DomainStockMovementTypeEnum.OUTBOUND,
                quantity,
                referenceId,
                referenceType != null ? referenceType.name() : null,
                batchId,
                null, // Reason not required for outbound
                performedBy,
                now,
                null, // Cost not typically tracked for outbound
                now
        );
    }
    
    /**
     * Factory method for creating a transfer stock movement.
     *
     * @param id the movement ID
     * @param sourceWarehouseId the source warehouse ID
     * @param productId the product ID
     * @param quantity the transfer quantity (must be positive, system will apply negative effect)
     * @param referenceId optional reference document ID
     * @param referenceType optional reference document type
     * @param batchId optional batch ID
     * @param performedBy who performed the movement
     * @param now current timestamp
     * @return a new transfer stock movement entity
     */
    public static DomainStockMovementEntity createTransfer(
            UUID id,
            UUID sourceWarehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String referenceId,
            String performedBy,
            UUID batchId,
            LocalDateTime now) {
        
        if (quantity.isNegative() || quantity.isZero()) {
            throw new DomainValidationException(
                "Transfer quantity must be positive (system will negate for calculation)",
                "quantity",
                quantity
            );
        }
        
        return new DomainStockMovementEntity(
                id,
                sourceWarehouseId,
                productId,
                DomainStockMovementTypeEnum.TRANSFER,
                quantity,
                referenceId,
                DomainReferenceTypeEnum.TRANSFER_ORDER.name(),
                batchId,
                null, // Reason not required for transfer
                performedBy,
                now,
                null, // Cost not typically tracked for transfer
                now
        );
    }
    
    /**
     * Factory method for creating an adjustment stock movement.
     *
     * @param id the movement ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the adjustment quantity (can be positive or negative)
     * @param reason the required reason for adjustment
     * @param performedBy who performed the adjustment
     * @param now current timestamp
     * @return a new adjustment stock movement entity
     */
    public static DomainStockMovementEntity createAdjustment(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity,
            String reason,
            String performedBy,
            DomainMoneyValue costPerUnit,
            LocalDateTime now) {
        
        if (quantity.isZero()) {
            throw new DomainValidationException(
                "Adjustment quantity cannot be zero",
                "quantity",
                quantity
            );
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new DomainValidationException(
                "Reason is required for adjustment",
                "reason",
                reason
            );
        }
        
        return new DomainStockMovementEntity(
                id,
                warehouseId,
                productId,
                DomainStockMovementTypeEnum.ADJUSTMENT,
                quantity,
                null, // Reference ID not typically used for adjustments
                DomainReferenceTypeEnum.ADJUSTMENT_ORDER.name(),
                null, // Batch ID not typically tracked for adjustments
                reason,
                performedBy,
                now,
                costPerUnit,
                now
        );
    }
    
    /**
     * Checks if this movement is an inbound movement.
     *
     * @return true if it's an inbound movement (increases stock)
     */
    public boolean isInbound() {
        return this.movementType == DomainStockMovementTypeEnum.INBOUND ||
               (this.movementType == DomainStockMovementTypeEnum.ADJUSTMENT && 
                !this.quantity.isNegative());
    }

    /**
     * Checks if this movement is an outbound movement.
     *
     * @return true if it's an outbound movement (decreases stock)
     */
    public boolean isOutbound() {
        return this.movementType == DomainStockMovementTypeEnum.OUTBOUND ||
               this.movementType == DomainStockMovementTypeEnum.TRANSFER ||
               (this.movementType == DomainStockMovementTypeEnum.ADJUSTMENT && 
                this.quantity.isNegative());
    }
    
    /**
     * Gets the effective quantity with sign based on movement type.
     * For outbound and transfer movements, returns the negative of the quantity.
     *
     * @return the effective signed quantity
     */
    public DomainQuantityValue getEffectiveQuantity() {
        // For outbound and transfer, negate the quantity
        if (movementType == DomainStockMovementTypeEnum.OUTBOUND || 
            movementType == DomainStockMovementTypeEnum.TRANSFER) {
            return quantity.multiply(java.math.BigDecimal.valueOf(-1));
        }
        // For inbound and adjustment, use as is
        return quantity;
    }
    
    /**
     * Calculates the total cost of this movement.
     *
     * @return the total cost, or null if cost per unit is not available
     */
    public DomainMoneyValue calculateTotalCost() {
        if (costPerUnit == null) {
            return null;
        }
        return costPerUnit.multiply(quantity.getValue().abs());
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

    public DomainStockMovementTypeEnum getMovementType() {
        return movementType;
    }

    public DomainQuantityValue getQuantity() {
        return quantity;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public DomainMoneyValue getCostPerUnit() {
        return costPerUnit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainStockMovementEntity that = (DomainStockMovementEntity) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DomainStockMovementEntity{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", referenceType='" + referenceType + '\'' +
                ", performedBy='" + performedBy + '\'' +
                ", performedAt=" + performedAt +
                '}';
    }
}