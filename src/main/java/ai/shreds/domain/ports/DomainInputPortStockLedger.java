package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain input port for stock ledger operations.
 * Defines the contract for movement history and reporting operations in the domain layer.
 * This port is implemented by domain services and called by application services.
 */
public interface DomainInputPortStockLedger {
    
    /**
     * Retrieves the movement history for a specific product in a warehouse.
     * Supports filtering by date range and movement type.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @param startDate the start date for the query (optional, can be null)
     * @param endDate the end date for the query (optional, can be null)
     * @param movementType the movement type filter (optional, can be null)
     * @param page the page number for pagination (0-based)
     * @param size the page size for pagination
     * @return list of stock movement entities matching the criteria
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    List<DomainStockMovementEntity> getMovementHistory(
            UUID warehouseId, 
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType,
            Integer page,
            Integer size);
    
    /**
     * Calculates the current quantities for a specific product in a warehouse.
     * Aggregates all movements to determine current inventory levels.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return map of quantity types to their values
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    Map<String, DomainQuantityValue> calculateCurrentQuantities(UUID warehouseId, DomainProductIdValue productId);
    
    /**
     * Gets the total movement count for a specific product in a warehouse.
     * Supports filtering by date range and movement type.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @param startDate the start date for the query (optional, can be null)
     * @param endDate the end date for the query (optional, can be null)
     * @param movementType the movement type filter (optional, can be null)
     * @return total count of movements matching the criteria
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    long countMovements(
            UUID warehouseId, 
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType);
    
    /**
     * Gets the last movement for a specific product in a warehouse.
     *
     * @param warehouseId the warehouse identifier
     * @param productId the product identifier
     * @return the last stock movement entity or null if none exists
     * @throws ai.shreds.domain.exceptions.DomainValidationException if parameters are invalid
     */
    DomainStockMovementEntity getLastMovement(UUID warehouseId, DomainProductIdValue productId);
}