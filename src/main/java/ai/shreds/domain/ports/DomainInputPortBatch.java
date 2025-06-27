package ai.shreds.domain.ports;

import ai.shreds.domain.commands.DomainCreateBatchCommand;
import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.util.List;
import java.util.UUID;

/**
 * Input port interface for batch domain operations.
 * Defines the contract for batch-related business logic and FIFO allocation.
 */
public interface DomainInputPortBatch {
    
    /**
     * Creates a new batch based on the provided command.
     * 
     * @param command the batch creation command
     * @return the created batch entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if validation fails
     */
    DomainBatchEntity createBatch(DomainCreateBatchCommand command);
    
    /**
     * Allocates batches for outbound movement using FIFO algorithm.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity the quantity to allocate
     * @return list of allocated batches
     * @throws ai.shreds.domain.exceptions.DomainInsufficientStockException if insufficient stock
     */
    List<DomainBatchEntity> allocateForOutbound(UUID warehouseId, DomainProductIdValue productId, DomainQuantityValue quantity);
    
    /**
     * Increases the quantity in an existing batch or creates a new one.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @param quantity the quantity to add
     * @return the updated or new batch entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if validation fails
     */
    DomainBatchEntity increaseBatch(UUID warehouseId, DomainProductIdValue productId, String batchNumber, DomainQuantityValue quantity);
    
    /**
     * Finds the oldest batch with available quantity for FIFO allocation.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the oldest available batch or null if none found
     */
    DomainBatchEntity findOldestAvailableBatch(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Finds all batches with available quantity for a product in a warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of available batches ordered by received date (FIFO)
     */
    List<DomainBatchEntity> findAvailableBatches(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Calculates total available quantity across all batches for a product.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the total available quantity
     */
    DomainQuantityValue calculateTotalAvailableQuantity(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Validates batch availability for a requested quantity.
     * 
     * @param batches the list of batches to check
     * @param requestedQuantity the requested quantity
     * @return true if batches have sufficient quantity
     */
    boolean validateBatchAvailability(List<DomainBatchEntity> batches, DomainQuantityValue requestedQuantity);
}