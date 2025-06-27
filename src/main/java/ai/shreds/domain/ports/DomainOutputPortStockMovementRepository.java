package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.value_objects.DomainProductIdValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for stock movement repository operations.
 * Defines the contract for stock movement data persistence to be implemented by infrastructure layer.
 * Supports stock ledger, audit trail, and movement history functionality.
 * 
 * Note: This interface follows domain-driven design principles where the domain defines
 * the contract and infrastructure implements it.
 */
public interface DomainOutputPortStockMovementRepository {
    
    /**
     * Saves a stock movement entity.
     * Stock movements are immutable once created, so this is primarily for new movements.
     * 
     * @param movement the stock movement entity to save
     * @return the saved stock movement entity with any generated fields
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if save fails
     */
    DomainStockMovementEntity save(DomainStockMovementEntity movement);
    
    /**
     * Finds movements by warehouse and product within a date range and optional movement type.
     * Provides paginated results for stock ledger display and movement history.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param movementType optional movement type filter (may be null for all types)
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortDirection the sort direction ("asc" or "desc")
     * @return list of matching stock movements
     */
    List<DomainStockMovementEntity> findByWarehouseIdAndProductIdAndDateRange(
            UUID warehouseId, 
            DomainProductIdValue productId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            DomainStockMovementTypeEnum movementType, 
            int page,
            int size,
            String sortDirection);
    
    /**
     * Counts movements by warehouse and product within a date range and optional movement type.
     * Used for pagination metadata in stock ledger displays.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param movementType optional movement type filter (may be null for all types)
     * @return count of matching stock movements
     */
    long countByWarehouseIdAndProductIdAndDateRange(
            UUID warehouseId, 
            DomainProductIdValue productId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            DomainStockMovementTypeEnum movementType);
    
    /**
     * Finds a stock movement by its unique identifier.
     * 
     * @param id the movement ID
     * @return the stock movement entity, or null if not found
     */
    DomainStockMovementEntity findById(UUID id);
    
    /**
     * Finds movements by reference ID and type.
     * Used to track movements associated with a specific order or document.
     * 
     * @param referenceId the reference document ID
     * @param referenceType the reference document type
     * @return list of stock movements with the reference
     */
    List<DomainStockMovementEntity> findByReferenceIdAndType(String referenceId, String referenceType);
    
    /**
     * Finds movements for a specific batch.
     * 
     * @param batchId the batch ID
     * @return list of stock movements affecting the batch
     */
    List<DomainStockMovementEntity> findByBatchId(UUID batchId);
    
    /**
     * Finds movements performed by a specific user.
     * Used for audit and accountability.
     * 
     * @param performedBy the user identifier
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of stock movements performed by the user
     */
    List<DomainStockMovementEntity> findByPerformedBy(
            String performedBy, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
    
    /**
     * Finds the most recent movement for a product in a warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the most recent stock movement, or null if none
     */
    DomainStockMovementEntity findMostRecentByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Finds movements by type within a date range.
     * 
     * @param movementType the movement type
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of matching stock movements
     */
    List<DomainStockMovementEntity> findByMovementTypeAndDateRange(
            DomainStockMovementTypeEnum movementType, 
            LocalDateTime startDate, 
            LocalDateTime endDate,
            int page,
            int size);
    
    /**
     * Finds movements requiring reason (typically adjustments).
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of stock movements with reasons
     */
    List<DomainStockMovementEntity> findWithReason(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds movements with cost information within a date range.
     * Used for financial reporting and valuation.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of stock movements with cost information
     */
    List<DomainStockMovementEntity> findWithCostInformation(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Counts total number of movements for a warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @return count of movements
     */
    long countByWarehouse(UUID warehouseId);
    
    /**
     * Counts total number of movements for a product.
     * 
     * @param productId the product ID
     * @return count of movements
     */
    long countByProduct(DomainProductIdValue productId);
    
    /**
     * Deletes a movement by ID.
     * Note: Generally movements should not be deleted for audit purposes,
     * so this should be used only in exceptional circumstances.
     * 
     * @param id the movement ID
     */
    void deleteById(UUID id);
    
    /**
     * Finds movements by warehouse.
     * 
     * @param warehouseId the warehouse ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of movements for the warehouse
     */
    List<DomainStockMovementEntity> findByWarehouseId(UUID warehouseId, int page, int size);
    
    /**
     * Finds movements by product.
     * 
     * @param productId the product ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of movements for the product
     */
    List<DomainStockMovementEntity> findByProductId(DomainProductIdValue productId, int page, int size);
}