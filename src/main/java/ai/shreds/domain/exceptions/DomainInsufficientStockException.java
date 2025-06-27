package ai.shreds.domain.exceptions;

import java.util.UUID;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

/**
 * Domain exception thrown when an inventory operation cannot be performed due to insufficient stock.
 * Contains detailed information about the warehouse, product, requested quantity, and available quantity
 * to help with error handling and reporting.
 */
public class DomainInsufficientStockException extends RuntimeException {
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final DomainQuantityValue requestedQuantity;
    private final DomainQuantityValue availableQuantity;

    /**
     * Constructs a new exception with the specified detail message and stock details.
     *
     * @param message the detail message
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param requestedQuantity the quantity that was requested
     * @param availableQuantity the quantity that was available
     */
    public DomainInsufficientStockException(String message, UUID warehouseId, DomainProductIdValue productId,
                                          DomainQuantityValue requestedQuantity, DomainQuantityValue availableQuantity) {
        super(message);
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    /**
     * Constructs a new exception with the specified detail message, stock details, and cause.
     *
     * @param message the detail message
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param requestedQuantity the quantity that was requested
     * @param availableQuantity the quantity that was available
     * @param cause the cause of the exception
     */
    public DomainInsufficientStockException(String message, UUID warehouseId, DomainProductIdValue productId,
                                          DomainQuantityValue requestedQuantity, DomainQuantityValue availableQuantity,
                                          Throwable cause) {
        super(message, cause);
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    /**
     * Gets a formatted error message with all the stock details.
     *
     * @return a detailed error message
     */
    public String getFormattedErrorMessage() {
        return String.format("Insufficient stock in warehouse %s for product %s. " +
                        "Requested: %s, Available: %s",
                warehouseId,
                productId.getValue(),
                requestedQuantity,
                availableQuantity);
    }
    
    /**
     * Returns the quantity shortfall (the difference between requested and available).
     *
     * @return the shortage quantity
     */
    public DomainQuantityValue getShortfall() {
        // If available is already less than requested, calculate the difference
        if (availableQuantity.isLessThan(requestedQuantity)) {
            return requestedQuantity.subtract(availableQuantity);
        }
        // Otherwise return zero with the same unit
        return new DomainQuantityValue(0, requestedQuantity.getUnit());
    }

    /**
     * Gets the warehouse ID where the insufficient stock occurred.
     *
     * @return the warehouse ID
     */
    public UUID getWarehouseId() {
        return warehouseId;
    }

    /**
     * Gets the product ID for which stock was insufficient.
     *
     * @return the product ID
     */
    public DomainProductIdValue getProductId() {
        return productId;
    }

    /**
     * Gets the quantity that was requested.
     *
     * @return the requested quantity
     */
    public DomainQuantityValue getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * Gets the quantity that was available.
     *
     * @return the available quantity
     */
    public DomainQuantityValue getAvailableQuantity() {
        return availableQuantity;
    }
    
    @Override
    public String toString() {
        return "DomainInsufficientStockException{" +
                "warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", requestedQuantity=" + requestedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
