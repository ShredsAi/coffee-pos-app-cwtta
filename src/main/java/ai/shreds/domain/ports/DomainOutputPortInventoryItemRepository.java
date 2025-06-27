package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for inventory item repository operations.
 * Defines the contract for inventory data persistence to be implemented by infrastructure layer.
 * 
 * Note: This interface follows domain-driven design principles where the domain defines
 * the contract and infrastructure implements it.
 */
public interface DomainOutputPortInventoryItemRepository {
    
    /**
     * Saves an inventory item entity (create or update).
     * Handles both new inventory item creation and existing item updates.
     * 
     * @param item the inventory item entity to save
     * @return the saved inventory item entity with any generated/updated fields
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if save fails
     */
    DomainInventoryItemEntity save(DomainInventoryItemEntity item);
    
    /**
     * Finds an inventory item by warehouse and product.
     * This is the primary lookup method for inventory operations.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the inventory item entity, or null if not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if any parameter is null
     */
    DomainInventoryItemEntity findByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Locks an inventory item by ID using pessimistic locking (SELECT FOR UPDATE).
     * Used for critical stock operations to prevent concurrent modifications.
     * 
     * @param id the inventory item ID
     * @return the locked inventory item entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if item not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if lock fails
     */
    DomainInventoryItemEntity lockById(UUID id);
    
    /**
     * Locks an inventory item by warehouse and product using pessimistic locking.
     * Used for critical stock operations to prevent concurrent modifications.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the locked inventory item entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if item not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if lock fails
     */
    DomainInventoryItemEntity lockByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Finds an inventory item by its unique identifier.
     * 
     * @param id the inventory item ID
     * @return the inventory item entity, or null if not found
     */
    DomainInventoryItemEntity findById(UUID id);
    
    /**
     * Finds all inventory items for a specific warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @return list of inventory items in the warehouse
     */
    List<DomainInventoryItemEntity> findByWarehouseId(UUID warehouseId);
    
    /**
     * Finds all inventory items for a specific product across all warehouses.
     * 
     * @param productId the product ID
     * @return list of inventory items for the product
     */
    List<DomainInventoryItemEntity> findByProductId(DomainProductIdValue productId);
    
    /**
     * Finds inventory items with available quantity below safety stock level.
     * Used for low stock reporting and alerts.
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of inventory items below safety stock level
     */
    List<DomainInventoryItemEntity> findBelowSafetyStockLevel(UUID warehouseId);
    
    /**
     * Finds inventory items with available quantity below reorder point.
     * Used for reordering reports and purchase recommendations.
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of inventory items below reorder point
     */
    List<DomainInventoryItemEntity> findBelowReorderPoint(UUID warehouseId);
    
    /**
     * Finds inventory items with zero available quantity (out of stock).
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of out-of-stock inventory items
     */
    List<DomainInventoryItemEntity> findOutOfStock(UUID warehouseId);
    
    /**
     * Finds inventory items with zero total quantity (no physical inventory).
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of inventory items with no physical stock
     */
    List<DomainInventoryItemEntity> findWithNoStock(UUID warehouseId);
    
    /**
     * Finds inventory items with movements after the specified date.
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @param sinceDate the date to check movements after
     * @return list of inventory items with recent movements
     */
    List<DomainInventoryItemEntity> findWithMovementsAfter(UUID warehouseId, LocalDateTime sinceDate);
    
    /**
     * Finds inventory items with no movements after the specified date (stagnant inventory).
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @param sinceDate the date to check for no movements after
     * @return list of inventory items with no recent movements
     */
    List<DomainInventoryItemEntity> findWithNoMovementsAfter(UUID warehouseId, LocalDateTime sinceDate);
    
    /**
     * Counts inventory items for a warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @return count of inventory items
     */
    long countByWarehouse(UUID warehouseId);
    
    /**
     * Deletes an inventory item.
     * Note: Should be used carefully as it's generally better to keep history.
     * 
     * @param id the inventory item ID to delete
     */
    void deleteById(UUID id);
    
    /**
     * Checks if an inventory item exists for the given warehouse and product.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return true if inventory item exists
     */
    boolean existsByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Updates thresholds (safety stock level and reorder point) for an inventory item.
     * 
     * @param id the inventory item ID
     * @param safetyStockLevel the new safety stock level
     * @param reorderPoint the new reorder point
     * @return the updated inventory item
     */
    DomainInventoryItemEntity updateThresholds(UUID id, DomainQuantityValue safetyStockLevel, DomainQuantityValue reorderPoint);
}