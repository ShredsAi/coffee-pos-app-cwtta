package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;

import java.util.UUID;

/**
 * Domain input port for inventory management operations.
 * Defines the contract for inventory-related operations in the domain layer.
 * This port is implemented by domain services and called by application services.
 */
public interface DomainInputPortInventory {
    
    /**
     * Retrieves an inventory item for a specific warehouse and product.
     * Creates a new inventory item if it doesn't exist.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return the inventory item entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse doesn't exist
     */
    DomainInventoryItemEntity getItem(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Updates the quantities of an inventory item.
     * This method handles quantity adjustments and validates business rules.
     *
     * @param item the inventory item to update
     * @return the updated inventory item entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if the update violates business rules
     * @throws ai.shreds.domain.exceptions.DomainOptimisticLockException if the item version is outdated
     */
    DomainInventoryItemEntity updateQuantities(DomainInventoryItemEntity item);
    
    /**
     * Checks if an inventory item has low stock levels.
     * Evaluates against safety stock level and reorder point thresholds.
     *
     * @param item the inventory item to check
     * @return true if the item has low stock and requires attention
     * @throws ai.shreds.domain.exceptions.DomainValidationException if the item is invalid
     */
    boolean checkLowStock(DomainInventoryItemEntity item);
}