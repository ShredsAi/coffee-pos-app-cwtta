package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedBatchRequestDTO;
import ai.shreds.shared.dtos.SharedBatchResponseDTO;

import java.util.UUID;

/**
 * Input port for batch management operations.
 * This interface defines the contract for batch-related operations
 * in the application layer.
 */
public interface ApplicationBatchInputPort {

    /**
     * Creates a new batch.
     *
     * @param request The batch creation request containing all batch details
     * @return SharedBatchResponseDTO The created batch with generated ID and timestamps
     * @throws ai.shreds.application.exceptions.ApplicationValidationException if request validation fails
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if warehouse or product not found
     */
    SharedBatchResponseDTO createBatch(SharedBatchRequestDTO request);

    /**
     * Retrieves a batch by ID.
     *
     * @param batchId The ID of the batch to retrieve
     * @return SharedBatchResponseDTO The batch details
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if batch not found
     */
    SharedBatchResponseDTO getBatch(UUID batchId);
    
    /**
     * Checks if a batch with the specified number exists for a product in a warehouse.
     *
     * @param warehouseId The warehouse ID
     * @param productId The product ID
     * @param batchNumber The batch number to check
     * @return boolean True if the batch exists, false otherwise
     */
    boolean checkBatchExists(UUID warehouseId, UUID productId, String batchNumber);
}