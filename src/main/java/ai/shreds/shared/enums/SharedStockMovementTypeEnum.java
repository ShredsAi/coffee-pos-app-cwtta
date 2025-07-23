package ai.shreds.shared.enums;

/**
 * Enum representing different types of inventory stock movements.
 * Each movement type has specific behavior regarding quantity effects and requirements.
 */
public enum SharedStockMovementTypeEnum {
    INBOUND,    // Adds to inventory (receiving, returns)
    OUTBOUND,   // Removes from inventory (shipping, sales)
    TRANSFER,   // Moves between warehouses
    ADJUSTMENT;  // Manual corrections/adjustments
    
    /**
     * Determines if this movement type increases inventory quantity.
     * Inbound movements add to inventory while others typically subtract or reallocate.
     *
     * @return true for movement types that add to inventory
     */
    public boolean isPositiveMovement() {
        return this == INBOUND;
    }
    
    /**
     * Determines if this movement type decreases inventory quantity.
     * Outbound and Transfer movements subtract from source inventory.
     *
     * @return true for movement types that subtract from inventory
     */
    public boolean isNegativeMovement() {
        return this == OUTBOUND || this == TRANSFER;
    }
    
    /**
     * Determines if this movement type requires a reason to be specified.
     * Adjustments and transfers typically require documentation of why they occurred.
     *
     * @return true if a reason is mandatory for this movement type
     */
    public boolean requiresReason() {
        return this == ADJUSTMENT || this == TRANSFER;
    }
    
    /**
     * Checks if the movement is customer-facing (relevant for business operations).
     *
     * @return true if the movement directly relates to customer orders
     */
    public boolean isCustomerFacing() {
        return this == OUTBOUND;
    }
    
    /**
     * Checks if the movement is for inventory rebalancing purposes.
     *
     * @return true if the movement is used to correct or redistribute inventory
     */
    public boolean isRebalancingMovement() {
        return this == ADJUSTMENT || this == TRANSFER;
    }
    
    /**
     * Checks if batch allocation is required for this movement type.
     * Typically, outbound movements need to allocate from specific batches for FIFO.
     *
     * @return true if batch allocation is required
     */
    public boolean requiresBatchAllocation() {
        return this == OUTBOUND;
    }
}