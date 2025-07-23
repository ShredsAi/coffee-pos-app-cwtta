package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Output port for batch repository operations.
 * Defines the contract for batch data persistence to be implemented by infrastructure layer.
 * 
 * Note: This interface follows domain-driven design principles where the domain defines
 * the contract and infrastructure implements it. It supports FIFO batch allocation.
 */
public interface DomainOutputPortBatchRepository {
    
    /**
     * Saves a batch entity (create or update).
     * Handles both new batch creation and existing batch updates.
     * 
     * @param batch the batch entity to save
     * @return the saved batch entity with any generated/updated fields
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if save fails
     */
    DomainBatchEntity save(DomainBatchEntity batch);
    
    /**
     * Finds the oldest received batch for a product in a warehouse (FIFO order).
     * Will only return batches that have not expired and have quantity > 0.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the oldest batch, or null if none found
     */
    DomainBatchEntity findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Checks if a batch exists with the specified warehouse, product and batch number.
     * Used to enforce uniqueness of batch numbers within a product-warehouse context.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @return true if a batch with these criteria exists
     */
    boolean existsByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, DomainProductIdValue productId, String batchNumber);
    
    /**
     * Finds batches with available quantity (quantity > 0) for a product in a warehouse.
     * Returns batches in FIFO order (oldest received first) for allocation.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of batches with available quantity in FIFO order
     */
    List<DomainBatchEntity> findByWarehouseIdAndProductIdWithAvailableQuantity(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Finds a batch by warehouse, product and batch number.
     * Used when adding to existing batches during inbound processing.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @return the batch entity, or null if not found
     */
    DomainBatchEntity findByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, DomainProductIdValue productId, String batchNumber);
    
    /**
     * Finds a batch by its unique identifier.
     * 
     * @param id the batch ID
     * @return the batch entity, or null if not found
     */
    DomainBatchEntity findById(UUID id);
    
    /**
     * Finds all batches for a specific warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @return list of all batches in the warehouse
     */
    List<DomainBatchEntity> findByWarehouseId(UUID warehouseId);
    
    /**
     * Finds all batches for a specific product across all warehouses.
     * 
     * @param productId the product ID
     * @return list of batches for the product
     */
    List<DomainBatchEntity> findByProductId(DomainProductIdValue productId);
    
    /**
     * Finds batches with specific batch number across all warehouses.
     * Used for batch tracing and recall scenarios.
     * 
     * @param batchNumber the batch number to search for
     * @return list of batches with the specified batch number
     */
    List<DomainBatchEntity> findByBatchNumber(String batchNumber);
    
    /**
     * Finds batches that have expired (expiration date in past).
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of expired batches
     */
    List<DomainBatchEntity> findExpiredBatches(UUID warehouseId);
    
    /**
     * Finds batches expiring before the specified date.
     * Used for expiration management and planning.
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @param expirationDate the date to check expiration before
     * @return list of batches expiring before the date
     */
    List<DomainBatchEntity> findBatchesExpiringBefore(UUID warehouseId, LocalDate expirationDate);
    
    /**
     * Finds batches for a specific supplier.
     * 
     * @param supplierId the supplier ID
     * @return list of batches from the supplier
     */
    List<DomainBatchEntity> findBySupplier(UUID supplierId);
    
    /**
     * Finds batches with zero quantity (empty batches).
     * These can potentially be cleaned up if they're no longer needed.
     * 
     * @param warehouseId the warehouse ID (optional, may be null for all warehouses)
     * @return list of empty batches
     */
    List<DomainBatchEntity> findEmptyBatches(UUID warehouseId);
    
    /**
     * Deletes a batch by ID.
     * Note: Should be used carefully as it's generally better to keep history.
     * 
     * @param id the batch ID
     */
    void deleteById(UUID id);
    
    /**
     * Locks a batch by ID using pessimistic locking for concurrent operations.
     * 
     * @param id the batch ID
     * @return the locked batch entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if batch not found
     */
    DomainBatchEntity lockById(UUID id);
    
    /**
     * Counts batches for a warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @return count of batches
     */
    long countByWarehouse(UUID warehouseId);
    
    /**
     * Counts batches for a product.
     * 
     * @param productId the product ID
     * @return count of batches
     */
    long countByProduct(DomainProductIdValue productId);
    
    /**
     * Finds batches with manufacturing date in the specified range.
     * Used for quality control and tracking.
     * 
     * @param startDate the start manufacturing date (inclusive)
     * @param endDate the end manufacturing date (inclusive)
     * @return list of batches manufactured in the date range
     */
    List<DomainBatchEntity> findByManufacturingDateBetween(LocalDate startDate, LocalDate endDate);
}