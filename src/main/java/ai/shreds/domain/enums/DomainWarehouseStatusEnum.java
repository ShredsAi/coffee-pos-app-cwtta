package ai.shreds.domain.enums;

/**
 * Enumeration representing the operational status of a warehouse.
 * Used to control whether inventory operations can be performed in a warehouse.
 */
public enum DomainWarehouseStatusEnum {
    ACTIVE,
    INACTIVE;
    
    /**
     * Determines if the warehouse is operational and can accept stock operations
     * @return true if the warehouse is active and operational
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }
    
    /**
     * Determines if the warehouse can accept inbound stock movements
     * @return true if the warehouse can receive stock
     */
    public boolean canReceiveStock() {
        return this == ACTIVE;
    }
    
    /**
     * Determines if the warehouse can process outbound stock movements
     * @return true if the warehouse can ship stock
     */
    public boolean canShipStock() {
        return this == ACTIVE;
    }
}