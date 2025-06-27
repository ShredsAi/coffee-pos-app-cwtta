package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Domain input port for inventory query operations.
 * Defines the contract for read-only inventory operations in the domain layer.
 * Separate from DomainInputPortInventory to follow CQRS pattern principles.
 */
public interface DomainInputPortInventoryQuery {
    
    /**
     * Finds an inventory item by warehouse and product identifiers.
     * Returns null if the item doesn't exist.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return the inventory item entity or null if not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    DomainInventoryItemEntity findByWarehouseAndProduct(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Gets current stock levels for a specific warehouse and product.
     * Returns a map with stock level details including available, reserved, allocated quantities.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return map containing stock level information with keys: "available", "reserved", "allocated", "total"
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if inventory item doesn't exist
     */
    Map<String, BigDecimal> getStockLevels(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Checks if an inventory item exists for the given warehouse and product.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return true if the inventory item exists
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    boolean existsByWarehouseAndProduct(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Gets the current available quantity for a specific warehouse and product.
     * Returns zero if the inventory item doesn't exist.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return the available quantity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    BigDecimal getAvailableQuantity(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Checks if there is sufficient available stock for a requested quantity.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @param requestedQuantity the quantity to check availability for
     * @return true if sufficient stock is available
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    boolean hasSufficientStock(UUID warehouseId, DomainProductIdValue productId, BigDecimal requestedQuantity);
}