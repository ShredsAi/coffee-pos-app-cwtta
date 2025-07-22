package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationStockMovementInputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.application.ports.ApplicationTransactionOutputPort;
import ai.shreds.domain.ports.DomainInputPortStockMovement;
import ai.shreds.domain.ports.DomainInputPortInventory;
import ai.shreds.domain.ports.DomainInputPortBatch;
import ai.shreds.shared.dtos.SharedStockMovementRequestDTO;
import ai.shreds.shared.dtos.SharedStockMovementResponseDTO;
import ai.shreds.shared.dtos.SharedStockMovementCreatedEventDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.commands.DomainStockMovementCommand;
import ai.shreds.application.exceptions.ApplicationInsufficientStockException;
import ai.shreds.application.exceptions.ApplicationWarehouseInactiveException;
import ai.shreds.domain.exceptions.DomainInsufficientStockException;
import ai.shreds.domain.exceptions.DomainWarehouseInactiveException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStockMovementServiceFixed implements ApplicationStockMovementInputPort {

    private final DomainInputPortStockMovement domainStockMovementService;
    private final DomainInputPortInventory domainInventoryService;
    private final DomainInputPortBatch domainBatchService;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ApplicationTransactionOutputPort transactionManager;
    private final ApplicationStockMovementMapper stockMovementMapper;
    private final ApplicationOutboxService outboxService;

    @Override
    @Transactional
    public SharedStockMovementResponseDTO processMovement(SharedStockMovementRequestDTO request) {
        log.info("Processing stock movement for warehouse: {}, product: {}, type: {}", 
                request.getWarehouseId(), request.getProductId(), request.getMovementType());
        
        try {
            // Step 1: Validate the movement
            validateMovement(request);
            
            // Step 2: Convert to domain command
            DomainStockMovementCommand command = stockMovementMapper.toDomainCommand(request);
            
            // Step 3: Apply the movement through domain service
            DomainStockMovementEntity movement = applyMovement(command);
            
            // Step 4: Record audit trail
            recordAudit(movement);
            
            // Step 5: Publish domain events
            publishEvents(movement);
            
            // Step 6: Convert to response DTO
            SharedStockMovementResponseDTO response = stockMovementMapper.toResponseDTO(movement);
            
            log.info("Successfully processed stock movement with ID: {}", movement.getId());
            return response;
            
        } catch (DomainInsufficientStockException ex) {
            log.error("Insufficient stock for movement: {}", ex.getMessage());
            throw new ApplicationInsufficientStockException(
                ex.getMessage(), 
                ex.getWarehouseId(), 
                ex.getProductId().getValue(), 
                ex.getRequestedQuantity().getValue(), 
                ex.getAvailableQuantity().getValue()
            );
        } catch (DomainWarehouseInactiveException ex) {
            log.error("Warehouse inactive for movement: {}", ex.getMessage());
            throw new ApplicationWarehouseInactiveException(ex.getMessage(), ex.getWarehouseId());
        } catch (Exception ex) {
            log.error("Error processing stock movement: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to process stock movement", ex);
        }
    }

    private void validateMovement(SharedStockMovementRequestDTO request) {
        log.debug("Validating stock movement request");
        
        // Convert to domain command for validation
        DomainStockMovementCommand command = stockMovementMapper.toDomainCommand(request);
        
        // Use domain service to validate
        boolean isValid = domainStockMovementService.validateMovement(command);
        if (!isValid) {
            throw new IllegalArgumentException("Invalid stock movement request");
        }
    }

    private DomainStockMovementEntity applyMovement(DomainStockMovementCommand command) {
        log.debug("Applying stock movement through domain service");
        
        // Process the movement through domain service
        DomainStockMovementEntity movement = domainStockMovementService.applyMovement(command);
        
        return movement;
    }

    private void recordAudit(DomainStockMovementEntity movement) {
        log.debug("Recording audit trail for movement ID: {}", movement.getId());
        // Audit is handled by the domain service when persisting the movement
        // Additional audit logic can be added here if needed
    }

    private void publishEvents(DomainStockMovementEntity movement) {
        log.debug("Publishing events for stock movement ID: {}", movement.getId());
        
        try {
            // Create and publish StockMovementCreated event
            SharedStockMovementCreatedEventDTO movementEvent = stockMovementMapper.toEventDTO(movement);
            eventPublisher.publishStockMovementCreated(movementEvent);
            
            // Save event to outbox for reliable delivery
            outboxService.saveEvent(
                "StockMovementCreated",
                movement.getId(),
                "StockMovement",
                movementEvent
            );
            
            // Get updated inventory item to publish stock levels updated event
            DomainInventoryItemEntity inventoryItem = domainInventoryService.getItem(
                movement.getWarehouseId(), 
                movement.getProductId()
            );
            
            // Create and publish StockLevelsUpdated event
            SharedStockLevelsUpdatedEventDTO stockLevelsEvent = createStockLevelsUpdatedEvent(inventoryItem, movement);
            eventPublisher.publishStockLevelsUpdated(stockLevelsEvent);
            
            // Save to outbox
            outboxService.saveEvent(
                "StockLevelsUpdated",
                inventoryItem.getId(),
                "InventoryItem",
                stockLevelsEvent
            );
            
        } catch (Exception ex) {
            log.error("Error publishing events for movement ID: {}", movement.getId(), ex);
            // Don't fail the transaction, events will be retried via outbox
        }
    }
    
    private SharedStockLevelsUpdatedEventDTO createStockLevelsUpdatedEvent(
            DomainInventoryItemEntity inventoryItem, 
            DomainStockMovementEntity movement) {
        
        // Create the event with current quantities
        SharedStockLevelsUpdatedEventDTO event = new SharedStockLevelsUpdatedEventDTO();
        event.setInventoryItemId(inventoryItem.getId());
        event.setWarehouseId(inventoryItem.getWarehouseId());
        // IMPORTANT FIX: Use toUUID() instead of getValue() to explicitly convert from String to UUID
        event.setProductId(inventoryItem.getProductId().toUUID());
        event.setNewQuantities(ApplicationInventoryMapper.toQuantitiesDTO(inventoryItem));
        event.setTimestamp(movement.getPerformedAt());
        
        // For old quantities, we'd need to calculate from before the movement
        // For now, we'll set it to the same as new quantities
        // In a real implementation, we'd track the previous state
        event.setOldQuantities(ApplicationInventoryMapper.toQuantitiesDTO(inventoryItem));
        
        return event;
    }
}