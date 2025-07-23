package ai.shreds.domain.ports;

import ai.shreds.domain.commands.DomainStockMovementCommand;
import ai.shreds.domain.entities.DomainStockMovementEntity;

/**
 * Domain input port for stock movement operations.
 * Defines the contract for processing stock movements in the warehouse.
 * This port is implemented by domain services and called by application services.
 */
public interface DomainInputPortStockMovement {
    
    /**
     * Processes a stock movement command, creating a movement entity and updating inventory.
     * This method handles the complete workflow of stock movement including validation,
     * inventory updates, and batch allocations.
     *
     * @param command the stock movement command containing all movement details
     * @return the created stock movement entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainInsufficientStockException if there's not enough stock for outbound movements
     * @throws ai.shreds.domain.exceptions.DomainWarehouseInactiveException if the warehouse is inactive
     */
    DomainStockMovementEntity processMovement(DomainStockMovementCommand command);
    
    /**
     * Validates a stock movement command without actually executing it.
     * Useful for pre-validation in UI or API layers.
     *
     * @param command the stock movement command to validate
     * @return true if the movement is valid and can be processed
     * @throws ai.shreds.domain.exceptions.DomainValidationException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainInsufficientStockException if there's not enough stock for outbound movements
     * @throws ai.shreds.domain.exceptions.DomainWarehouseInactiveException if the warehouse is inactive
     */
    boolean validateMovement(DomainStockMovementCommand command);
    
    /**
     * Applies a stock movement command, creating the movement entity and updating inventory.
     * This is the core method that handles the business logic of stock movements.
     *
     * @param command the stock movement command to apply
     * @return the created stock movement entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainInsufficientStockException if there's not enough stock for outbound movements
     * @throws ai.shreds.domain.exceptions.DomainWarehouseInactiveException if the warehouse is inactive
     */
    DomainStockMovementEntity applyMovement(DomainStockMovementCommand command);
}