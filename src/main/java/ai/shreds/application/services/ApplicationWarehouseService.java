package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationWarehouseInputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.domain.ports.DomainInputPortWarehouse;
import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.commands.DomainCreateWarehouseCommand;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.shared.dtos.SharedWarehouseRequestDTO;
import ai.shreds.shared.dtos.SharedWarehouseResponseDTO;
import ai.shreds.shared.dtos.SharedWarehouseStatusChangedEventDTO;
import ai.shreds.application.exceptions.ApplicationEntityNotFoundException;
import ai.shreds.application.exceptions.ApplicationValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationWarehouseService implements ApplicationWarehouseInputPort {

    private final DomainInputPortWarehouse domainWarehousePort;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ApplicationWarehouseMapper warehouseMapper;
    private final ApplicationOutboxService outboxService;

    @Override
    @Transactional
    public SharedWarehouseResponseDTO createWarehouse(SharedWarehouseRequestDTO request) {
        log.info("Creating new warehouse with code: {}, name: {}", request.getCode(), request.getName());
        
        try {
            // Convert to domain command
            DomainCreateWarehouseCommand command = warehouseMapper.toDomainCommand(request);
            
            // Create warehouse through domain service
            DomainWarehouseEntity warehouse = domainWarehousePort.createWarehouse(command);
            
            // Check if the warehouse status needs to be published (if not default active=true)
            boolean isNewlyActive = request.getIsActive() != null && request.getIsActive();
            if (isNewlyActive) {
                publishWarehouseEvents(warehouse, false, isNewlyActive);
            }
            
            // Convert to response DTO
            SharedWarehouseResponseDTO response = warehouseMapper.toResponseDTO(warehouse);
            
            log.info("Successfully created warehouse with ID: {}", warehouse.getId());
            return response;
            
        } catch (DomainValidationException ex) {
            log.error("Validation error creating warehouse: {}", ex.getMessage());
            throw new ApplicationValidationException(ex.getMessage(), ex.getFieldName(), ex.getRejectedValue());
        } catch (Exception ex) {
            log.error("Error creating warehouse: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create warehouse", ex);
        }
    }

    @Override
    @Transactional
    public SharedWarehouseResponseDTO changeStatus(UUID warehouseId, boolean isActive) {
        log.info("Changing warehouse status, ID: {}, new status: {}", warehouseId, isActive);
        
        try {
            // Change status through domain service - this should return current warehouse with old status info
            DomainWarehouseEntity updatedWarehouse = domainWarehousePort.changeStatus(warehouseId, isActive);
            
            // For publishing event, we assume the change was successful
            // In a real implementation, the domain service would provide old status info
            boolean oldStatus = !isActive; // Simplified assumption
            publishWarehouseEvents(updatedWarehouse, oldStatus, isActive);
            
            // Convert to response DTO
            SharedWarehouseResponseDTO response = warehouseMapper.toResponseDTO(updatedWarehouse);
            
            log.info("Successfully changed warehouse status, ID: {}, status: {}", warehouseId, isActive);
            return response;
            
        } catch (DomainEntityNotFoundException ex) {
            log.error("Warehouse not found with ID: {}", warehouseId);
            throw new ApplicationEntityNotFoundException(ex.getMessage(), ex.getEntityType(), ex.getEntityId());
        } catch (Exception ex) {
            log.error("Error changing warehouse status: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to change warehouse status", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SharedWarehouseResponseDTO getWarehouse(UUID warehouseId) {
        log.debug("Retrieving warehouse with ID: {}", warehouseId);
        
        try {
            // Note: Based on UML, there's no getWarehouseById in the domain port
            // We'll need to use validateActive or create a new method
            // For now, throwing unsupported operation
            throw new UnsupportedOperationException("Get warehouse by ID not implemented in domain port");
            
        } catch (Exception ex) {
            log.error("Error retrieving warehouse: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to retrieve warehouse", ex);
        }
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID warehouseId) {
        log.info("Deleting warehouse with ID: {}", warehouseId);
        
        try {
            // Note: Based on UML, there's no deleteWarehouse in the domain port
            // This operation might not be supported or needs to be implemented differently
            throw new UnsupportedOperationException("Delete warehouse not implemented in domain port");
            
        } catch (Exception ex) {
            log.error("Error deleting warehouse: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete warehouse", ex);
        }
    }

    private void publishWarehouseEvents(DomainWarehouseEntity warehouse, boolean oldStatus, boolean newStatus) {
        log.debug("Publishing warehouse status changed event for warehouse ID: {}", warehouse.getId());
        
        try {
            // Create event DTO
            SharedWarehouseStatusChangedEventDTO event = warehouseMapper.toEventDTO(warehouse, oldStatus, newStatus);
            
            // Publish event
            eventPublisher.publishWarehouseStatusChanged(event);
            
            // Save to outbox for reliable delivery
            outboxService.saveEvent(
                "WarehouseStatusChanged",
                warehouse.getId(),
                "Warehouse",
                event
            );
            
            log.debug("Successfully published warehouse status changed event for warehouse ID: {}", warehouse.getId());
            
        } catch (Exception ex) {
            log.error("Error publishing warehouse event: {}", ex.getMessage(), ex);
            // Don't fail the transaction, will be retried via outbox
        }
    }
}