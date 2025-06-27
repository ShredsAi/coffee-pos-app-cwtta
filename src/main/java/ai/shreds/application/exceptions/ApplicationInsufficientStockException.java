package ai.shreds.application.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Exception thrown when there is insufficient stock to fulfill a stock movement request.
 * This exception is typically thrown during outbound or transfer movements when
 * the requested quantity exceeds the available quantity.
 */
public class ApplicationInsufficientStockException extends RuntimeException {

    private final UUID warehouseId;
    private final String productId;
    private final BigDecimal requestedQuantity;
    private final BigDecimal availableQuantity;

    public ApplicationInsufficientStockException(String message, UUID warehouseId, String productId, 
            BigDecimal requestedQuantity, BigDecimal availableQuantity) {
        super(message);
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Gets the warehouse ID where the insufficient stock occurred.
     *
     * @return The warehouse UUID
     */
    public UUID getWarehouseId() {
        return warehouseId;
    }

    /**
     * Gets the product ID that doesn't have sufficient stock.
     *
     * @return The product ID as string
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Gets the quantity that was requested.
     *
     * @return The requested quantity
     */
    public BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * Gets the available quantity at the time of the request.
     *
     * @return The available quantity
     */
    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Creates a detailed error message including all relevant information.
     *
     * @return Formatted error message
     */
    @Override
    public String getMessage() {
        return String.format("%s - Warehouse: %s, Product: %s, Requested: %s, Available: %s",
                super.getMessage(), warehouseId, productId, requestedQuantity, availableQuantity);
    }
}