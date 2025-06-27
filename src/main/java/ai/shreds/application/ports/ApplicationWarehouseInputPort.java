package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedWarehouseRequestDTO;
import ai.shreds.shared.dtos.SharedWarehouseResponseDTO;

import java.util.UUID;

/**
 * Input port for warehouse management operations.
 * This interface defines the contract for warehouse-related operations
 * in the application layer.
 */
public interface ApplicationWarehouseInputPort {

    /**
     * Creates a new warehouse.
     *
     * @param request The warehouse creation request containing all warehouse details
     * @return SharedWarehouseResponseDTO The created warehouse with generated ID and timestamps
     * @throws ai.shreds.application.exceptions.ApplicationValidationException if request validation fails
     */
    SharedWarehouseResponseDTO createWarehouse(SharedWarehouseRequestDTO request);

    /**
     * Changes the active status of a warehouse.
     *
     * @param warehouseId The ID of the warehouse to update
     * @param isActive The new active status (true for active, false for inactive)
     * @return SharedWarehouseResponseDTO The updated warehouse with new status
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if warehouse not found
     */
    SharedWarehouseResponseDTO changeStatus(UUID warehouseId, boolean isActive);
    
    /**
     * Retrieves a warehouse by ID.
     *
     * @param warehouseId The ID of the warehouse to retrieve
     * @return SharedWarehouseResponseDTO The warehouse details
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if warehouse not found
     */
    SharedWarehouseResponseDTO getWarehouse(UUID warehouseId);
    
    /**
     * Deletes a warehouse by ID.
     *
     * @param warehouseId The ID of the warehouse to delete
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if warehouse not found
     */
    void deleteWarehouse(UUID warehouseId);
}