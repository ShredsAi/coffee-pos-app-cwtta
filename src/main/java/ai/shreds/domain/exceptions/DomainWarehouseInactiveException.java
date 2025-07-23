package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain exception thrown when an operation is attempted on an inactive warehouse.
 * Indicates that the warehouse is not operational and cannot accept stock movements or other operations.
 */
public class DomainWarehouseInactiveException extends RuntimeException {
    private final UUID warehouseId;

    /**
     * Constructs a new exception with the specified detail message and warehouse ID.
     *
     * @param message the detail message
     * @param warehouseId the ID of the inactive warehouse
     */
    public DomainWarehouseInactiveException(String message, UUID warehouseId) {
        super(message);
        this.warehouseId = warehouseId;
    }
    
    /**
     * Constructs a new exception with the specified detail message, warehouse ID, and cause.
     *
     * @param message the detail message
     * @param warehouseId the ID of the inactive warehouse
     * @param cause the cause of the exception
     */
    public DomainWarehouseInactiveException(String message, UUID warehouseId, Throwable cause) {
        super(message, cause);
        this.warehouseId = warehouseId;
    }
    
    /**
     * Gets a formatted error message with warehouse details.
     *
     * @return a detailed error message
     */
    public String getFormattedErrorMessage() {
        return String.format("Warehouse %s is inactive: %s", warehouseId, getMessage());
    }
    
    /**
     * Creates a standard exception for when operations are attempted on inactive warehouses.
     *
     * @param warehouseId the warehouse ID
     * @return a new DomainWarehouseInactiveException
     */
    public static DomainWarehouseInactiveException forOperationAttempt(UUID warehouseId) {
        return new DomainWarehouseInactiveException(
            "Cannot perform operations on inactive warehouse", 
            warehouseId
        );
    }
    
    /**
     * Creates an exception for when stock movements are attempted on inactive warehouses.
     *
     * @param warehouseId the warehouse ID
     * @return a new DomainWarehouseInactiveException
     */
    public static DomainWarehouseInactiveException forStockMovement(UUID warehouseId) {
        return new DomainWarehouseInactiveException(
            "Cannot perform stock movements in inactive warehouse", 
            warehouseId
        );
    }

    /**
     * Gets the warehouse ID where the inactive operation was attempted.
     *
     * @return the warehouse ID
     */
    public UUID getWarehouseId() {
        return warehouseId;
    }
    
    @Override
    public String toString() {
        return "DomainWarehouseInactiveException{" +
                "warehouseId=" + warehouseId +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}