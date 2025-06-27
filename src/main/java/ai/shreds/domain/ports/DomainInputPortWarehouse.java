package ai.shreds.domain.ports;

import ai.shreds.domain.commands.DomainCreateWarehouseCommand;
import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.value_objects.DomainAddressValue;

import java.util.UUID;

/**
 * Input port interface for warehouse domain operations.
 * Defines the contract for warehouse-related business logic.
 */
public interface DomainInputPortWarehouse {
    
    /**
     * Creates a new warehouse based on the provided command.
     * 
     * @param command the warehouse creation command
     * @return the created warehouse entity
     * @throws ai.shreds.domain.exceptions.DomainValidationException if validation fails
     */
    DomainWarehouseEntity createWarehouse(DomainCreateWarehouseCommand command);
    
    /**
     * Changes the status of a warehouse (active/inactive).
     * 
     * @param warehouseId the warehouse ID
     * @param isActive the new status
     * @return the updated warehouse entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if status change is invalid
     */
    DomainWarehouseEntity changeStatus(UUID warehouseId, boolean isActive);
    
    /**
     * Validates that a warehouse is active and can accept operations.
     * 
     * @param warehouseId the warehouse ID to validate
     * @return true if the warehouse is active
     * @throws ai.shreds.domain.exceptions.DomainWarehouseInactiveException if warehouse is inactive
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse not found
     */
    boolean validateActive(UUID warehouseId);
    
    /**
     * Retrieves a warehouse by its ID.
     * 
     * @param warehouseId the warehouse ID
     * @return the warehouse entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse not found
     */
    DomainWarehouseEntity getWarehouse(UUID warehouseId);
    
    /**
     * Updates a warehouse's address.
     * 
     * @param warehouseId the warehouse ID
     * @param newAddress the new address
     * @return the updated warehouse entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if address is invalid
     */
    DomainWarehouseEntity updateAddress(UUID warehouseId, DomainAddressValue newAddress);
    
    /**
     * Updates a warehouse's name.
     * 
     * @param warehouseId the warehouse ID
     * @param newName the new name
     * @return the updated warehouse entity
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if warehouse not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if name is invalid
     */
    DomainWarehouseEntity updateName(UUID warehouseId, String newName);
}