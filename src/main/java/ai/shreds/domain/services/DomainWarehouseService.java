package ai.shreds.domain.services;

import ai.shreds.domain.commands.DomainCreateWarehouseCommand;
import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.exceptions.DomainWarehouseInactiveException;
import ai.shreds.domain.ports.DomainInputPortWarehouse;
import ai.shreds.domain.ports.DomainOutputPortWarehouseRepository;
import ai.shreds.domain.value_objects.DomainAddressValue;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service implementing warehouse business logic.
 * Handles warehouse creation, status management, and validation.
 */
public class DomainWarehouseService implements DomainInputPortWarehouse {
    
    private final DomainOutputPortWarehouseRepository warehouseRepository;
    
    /**
     * Constructs the warehouse service with required dependencies.
     *
     * @param warehouseRepository the warehouse repository
     */
    public DomainWarehouseService(DomainOutputPortWarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }
    
    @Override
    public DomainWarehouseEntity createWarehouse(DomainCreateWarehouseCommand command) {
        if (command == null) {
            throw new DomainValidationException("Warehouse creation command cannot be null", "command", null);
        }
        
        // Check if warehouse code already exists
        if (warehouseRepository.existsByCode(command.getCode())) {
            throw new DomainValidationException(
                "Warehouse with code already exists", 
                "code", 
                command.getCode()
            );
        }
        
        // Generate new ID
        UUID newId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        // Create new warehouse entity
        DomainWarehouseEntity warehouse = new DomainWarehouseEntity(
                newId,
                command.getCode(),
                command.getName(),
                command.getAddress(),
                command.isActive(),
                now,
                now
        );
        
        // Save and return
        return warehouseRepository.save(warehouse);
    }
    
    @Override
    public DomainWarehouseEntity changeStatus(UUID warehouseId, boolean isActive) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        // Get existing warehouse
        DomainWarehouseEntity warehouse = getWarehouse(warehouseId);
        
        // Check if status change is needed
        if (warehouse.isActive() == isActive) {
            return warehouse; // No change needed
        }
        
        // Apply status change
        LocalDateTime now = LocalDateTime.now();
        if (isActive) {
            warehouse.activate(now);
        } else {
            warehouse.deactivate(now);
        }
        
        // Save and return updated warehouse
        return warehouseRepository.save(warehouse);
    }
    
    @Override
    public boolean validateActive(UUID warehouseId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        DomainWarehouseEntity warehouse = getWarehouse(warehouseId);
        
        if (!warehouse.isActive()) {
            throw new DomainWarehouseInactiveException(
                "Warehouse is inactive and cannot accept operations", 
                warehouseId
            );
        }
        
        return true;
    }
    
    @Override
    public DomainWarehouseEntity getWarehouse(UUID warehouseId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        DomainWarehouseEntity warehouse = warehouseRepository.findById(warehouseId);
        if (warehouse == null) {
            throw new DomainEntityNotFoundException(
                "Warehouse not found", 
                "DomainWarehouseEntity", 
                warehouseId.toString()
            );
        }
        
        return warehouse;
    }
    
    @Override
    public DomainWarehouseEntity updateAddress(UUID warehouseId, DomainAddressValue newAddress) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (newAddress == null) {
            throw new DomainValidationException("New address cannot be null", "newAddress", null);
        }
        
        // Get existing warehouse
        DomainWarehouseEntity warehouse = getWarehouse(warehouseId);
        
        // Update address
        warehouse.updateAddress(newAddress, LocalDateTime.now());
        
        // Save and return
        return warehouseRepository.save(warehouse);
    }
    
    @Override
    public DomainWarehouseEntity updateName(UUID warehouseId, String newName) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new DomainValidationException("New name cannot be null or empty", "newName", newName);
        }
        
        // Get existing warehouse
        DomainWarehouseEntity warehouse = getWarehouse(warehouseId);
        
        // Update name
        warehouse.updateName(newName, LocalDateTime.now());
        
        // Save and return
        return warehouseRepository.save(warehouse);
    }
    
    /**
     * Validates that a warehouse exists and is operational for stock operations.
     * 
     * @param warehouseId the warehouse ID
     * @return the warehouse entity if valid
     * @throws DomainEntityNotFoundException if warehouse not found
     * @throws DomainWarehouseInactiveException if warehouse is inactive
     */
    public DomainWarehouseEntity validateForStockOperations(UUID warehouseId) {
        DomainWarehouseEntity warehouse = getWarehouse(warehouseId);
        warehouse.validateStockOperationsAllowed();
        return warehouse;
    }
    
    /**
     * Checks if a warehouse code is available for use.
     * 
     * @param code the warehouse code to check
     * @return true if the code is available
     */
    public boolean isCodeAvailable(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return !warehouseRepository.existsByCode(code.trim().toUpperCase());
    }
}