package ai.shreds.domain.commands;

import ai.shreds.domain.enums.DomainReferenceTypeEnum;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Command object for stock movement operations.
 * Contains all necessary data for processing a stock movement in the domain layer.
 * Immutable command pattern implementation.
 */
public class DomainStockMovementCommand {
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final DomainStockMovementTypeEnum movementType;
    private final DomainQuantityValue quantity;
    private final String referenceId;
    private final DomainReferenceTypeEnum referenceType;
    private final String reason;
    private final String performedBy;
    private final DomainMoneyValue costPerUnit;
    private final String batchNumber;

    /**
     * Constructs a new stock movement command.
     *
     * @param warehouseId the warehouse where the movement occurs
     * @param productId the product being moved
     * @param movementType the type of movement (INBOUND, OUTBOUND, etc.)
     * @param quantity the quantity being moved
     * @param referenceId reference to an external document (optional)
     * @param referenceType type of external reference (optional)
     * @param reason explanation for the movement (required for some types)
     * @param performedBy user or system performing the movement
     * @param costPerUnit cost information (optional)
     * @param batchNumber specific batch number (optional)
     */
    public DomainStockMovementCommand(
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainStockMovementTypeEnum movementType,
            DomainQuantityValue quantity,
            String referenceId,
            DomainReferenceTypeEnum referenceType,
            String reason,
            String performedBy,
            DomainMoneyValue costPerUnit,
            String batchNumber) {
        
        validateConstructorParameters(warehouseId, productId, movementType, quantity, performedBy);
        
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.reason = reason;
        this.performedBy = performedBy;
        this.costPerUnit = costPerUnit;
        this.batchNumber = batchNumber;
    }
    
    private void validateConstructorParameters(
            UUID warehouseId, DomainProductIdValue productId, 
            DomainStockMovementTypeEnum movementType, DomainQuantityValue quantity, 
            String performedBy) {
        
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
        
        if (performedBy == null || performedBy.trim().isEmpty()) {
            throw new DomainValidationException("Performed by cannot be null or empty", "performedBy", performedBy);
        }
    }
    
    /**
     * Creates a builder for constructing stock movement commands.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating DomainStockMovementCommand instances.
     * Provides a fluent API for command construction.
     */
    public static class Builder {
        private UUID warehouseId;
        private DomainProductIdValue productId;
        private DomainStockMovementTypeEnum movementType;
        private DomainQuantityValue quantity;
        private String referenceId;
        private DomainReferenceTypeEnum referenceType;
        private String reason;
        private String performedBy;
        private DomainMoneyValue costPerUnit;
        private String batchNumber;
        
        public Builder warehouseId(UUID warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }
        
        public Builder productId(DomainProductIdValue productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder movementType(DomainStockMovementTypeEnum movementType) {
            this.movementType = movementType;
            return this;
        }
        
        public Builder quantity(DomainQuantityValue quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder referenceId(String referenceId) {
            this.referenceId = referenceId;
            return this;
        }
        
        public Builder referenceType(DomainReferenceTypeEnum referenceType) {
            this.referenceType = referenceType;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder performedBy(String performedBy) {
            this.performedBy = performedBy;
            return this;
        }
        
        public Builder costPerUnit(DomainMoneyValue costPerUnit) {
            this.costPerUnit = costPerUnit;
            return this;
        }
        
        public Builder batchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }
        
        /**
         * Builds the command with the specified parameters.
         *
         * @return a new DomainStockMovementCommand
         * @throws DomainValidationException if required fields are missing
         */
        public DomainStockMovementCommand build() {
            return new DomainStockMovementCommand(
                    warehouseId, productId, movementType, quantity,
                    referenceId, referenceType, reason, performedBy,
                    costPerUnit, batchNumber
            );
        }
    }
    
    /**
     * Checks if this command represents an inbound movement.
     *
     * @return true if the movement type is inbound
     */
    public boolean isInbound() {
        return movementType.isPositiveMovement() || 
               (movementType == DomainStockMovementTypeEnum.ADJUSTMENT && !quantity.isNegative());
    }
    
    /**
     * Checks if this command represents an outbound movement.
     *
     * @return true if the movement type is outbound
     */
    public boolean isOutbound() {
        return movementType.isNegativeMovement() || 
               (movementType == DomainStockMovementTypeEnum.ADJUSTMENT && quantity.isNegative());
    }
    
    /**
     * Checks if a reason is required for this movement type.
     *
     * @return true if reason is required
     */
    public boolean requiresReason() {
        return movementType.requiresReason();
    }
    
    /**
     * Validates that all required fields are present based on movement type.
     *
     * @throws DomainValidationException if validation fails
     */
    public void validate() {
        if (requiresReason() && (reason == null || reason.trim().isEmpty())) {
            throw new DomainValidationException(
                "Reason is required for " + movementType + " movements",
                "reason",
                reason
            );
        }
        
        // Validate movement type and quantity alignment
        if (movementType == DomainStockMovementTypeEnum.INBOUND && quantity.isNegative()) {
            throw new DomainValidationException(
                "INBOUND movement cannot have negative quantity",
                "quantity",
                quantity
            );
        }
        
        if ((movementType == DomainStockMovementTypeEnum.OUTBOUND || 
             movementType == DomainStockMovementTypeEnum.TRANSFER) && quantity.isNegative()) {
            throw new DomainValidationException(
                movementType + " movement cannot have negative quantity (system will apply negative effect)",
                "quantity",
                quantity
            );
        }
    }

    // Getters
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

    public DomainReferenceTypeEnum getReferenceType() {
        return referenceType;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public DomainMoneyValue getCostPerUnit() {
        return costPerUnit;
    }

    public String getBatchNumber() {
        return batchNumber;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainStockMovementCommand that = (DomainStockMovementCommand) o;
        return Objects.equals(warehouseId, that.warehouseId) &&
               Objects.equals(productId, that.productId) &&
               movementType == that.movementType &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(referenceId, that.referenceId) &&
               referenceType == that.referenceType &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(performedBy, that.performedBy) &&
               Objects.equals(costPerUnit, that.costPerUnit) &&
               Objects.equals(batchNumber, that.batchNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, productId, movementType, quantity, 
                          referenceId, referenceType, reason, performedBy, 
                          costPerUnit, batchNumber);
    }
    
    @Override
    public String toString() {
        return "DomainStockMovementCommand{" +
                "warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", performedBy='" + performedBy + '\'' +
                '}';
    }
}