package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedStockMovementRequestDTO;
import ai.shreds.shared.dtos.SharedStockMovementResponseDTO;

/**
 * Input port for stock movement operations.
 * This interface defines the contract for processing stock movements
 * in the application layer.
 */
public interface ApplicationStockMovementInputPort {

    /**
     * Processes a stock movement request.
     * This method handles the complete stock movement workflow including:
     * - Validation of the movement request
     * - Applying the movement to inventory quantities
     * - Batch allocation for FIFO constraints
     * - Publishing domain events
     * - Recording audit trail
     *
     * @param request The stock movement request containing all movement details
     * @return SharedStockMovementResponseDTO The created stock movement with generated ID and timestamps
     * @throws ai.shreds.application.exceptions.ApplicationInsufficientStockException if not enough stock available for outbound movements
     * @throws ai.shreds.application.exceptions.ApplicationWarehouseInactiveException if warehouse is not active
     * @throws ai.shreds.application.exceptions.ApplicationValidationException if request validation fails
     */
    SharedStockMovementResponseDTO processMovement(SharedStockMovementRequestDTO request);
}