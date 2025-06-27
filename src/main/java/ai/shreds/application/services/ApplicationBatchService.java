package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationBatchInputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.application.exceptions.ApplicationEntityNotFoundException;
import ai.shreds.application.exceptions.ApplicationValidationException;
import ai.shreds.domain.ports.DomainInputPortBatch;
import ai.shreds.domain.ports.DomainInputPortWarehouse;
import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.commands.DomainCreateBatchCommand;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.exceptions.DomainWarehouseInactiveException;
import ai.shreds.shared.dtos.SharedBatchRequestDTO;
import ai.shreds.shared.dtos.SharedBatchResponseDTO;
import ai.shreds.shared.dtos.SharedBatchCreatedEventDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationBatchService implements ApplicationBatchInputPort {

    private final DomainInputPortBatch domainBatchPort;
    private final DomainInputPortWarehouse domainWarehousePort;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ApplicationBatchMapper batchMapper;
    private final ApplicationOutboxService outboxService;

    @Override
    @Transactional
    public SharedBatchResponseDTO createBatch(SharedBatchRequestDTO request) {
        log.info("Creating new batch for warehouse: {}, product: {}, batchNumber: {}", 
                request.getWarehouseId(), request.getProductId(), request.getBatchNumber());
        
        try {
            // Step 1: Validate the batch creation
            validateBatchCreation(request);
            
            // Step 2: Convert to domain command
            DomainCreateBatchCommand command = batchMapper.toDomainCommand(request);
            
            // Step 3: Create batch through domain service
            DomainBatchEntity batch = domainBatchPort.createBatch(command);
            
            // Step 4: Publish batch created event
            publishBatchCreatedEvent(batch);
            
            // Step 5: Convert to response DTO
            SharedBatchResponseDTO response = batchMapper.toResponseDTO(batch);
            
            log.info("Successfully created batch with ID: {}", batch.getId());
            return response;
            
        } catch (DomainValidationException ex) {
            log.error("Validation error creating batch: {}", ex.getMessage());
            throw new ApplicationValidationException(ex.getMessage(), ex.getFieldName(), ex.getRejectedValue());
        } catch (DomainEntityNotFoundException ex) {
            log.error("Entity not found creating batch: {}", ex.getMessage());
            throw new ApplicationEntityNotFoundException(ex.getMessage(), ex.getEntityType(), ex.getEntityId());
        } catch (DomainWarehouseInactiveException ex) {
            log.error("Warehouse inactive for batch creation: {}", ex.getMessage());
            throw new ApplicationValidationException("Warehouse is inactive", "warehouseId", request.getWarehouseId());
        } catch (Exception ex) {
            log.error("Error creating batch: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create batch", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SharedBatchResponseDTO getBatch(UUID batchId) {
        log.debug("Retrieving batch with ID: {}", batchId);
        
        try {
            // Note: Based on UML, there's no getBatchById in the domain port
            // This operation is not supported by the current domain interface
            throw new UnsupportedOperationException("Get batch by ID not implemented in domain port");
            
        } catch (Exception ex) {
            log.error("Error retrieving batch: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to retrieve batch", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkBatchExists(UUID warehouseId, UUID productId, String batchNumber) {
        log.debug("Checking if batch exists - warehouse: {}, product: {}, batchNumber: {}", 
                warehouseId, productId, batchNumber);
        
        try {
            // Note: Based on UML, there's no batchExists method in the domain port
            // This operation is not supported by the current domain interface
            // We could potentially implement this using the batch repository directly if needed
            throw new UnsupportedOperationException("Check batch exists not implemented in domain port");
            
        } catch (Exception ex) {
            log.error("Error checking batch existence: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private void validateBatchCreation(SharedBatchRequestDTO request) {
        log.debug("Validating batch creation request");
        
        // Validate warehouse exists and is active
        UUID warehouseId = request.getWarehouseId();
        if (!domainWarehousePort.validateActive(warehouseId)) {
            throw new DomainWarehouseInactiveException("Warehouse is inactive or doesn't exist", warehouseId);
        }
        
        // Validate product ID is not null
        if (request.getProductId() == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        // Validate batch number
        String batchNumber = request.getBatchNumber();
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new DomainValidationException("Batch number cannot be empty", "batchNumber", batchNumber);
        }
        
        // Validate batch quantity
        if (request.getQuantity() == null || request.getQuantity().getValue() == null || 
                request.getQuantity().getValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("Quantity must be positive", "quantity", 
                    request.getQuantity() == null ? null : request.getQuantity().getValue());
        }
        
        // Note: Cannot check if batch already exists since batchExists method doesn't exist in domain port
        // This validation would need to be moved to the domain layer
        
        // Validate dates if provided
        java.time.LocalDate now = java.time.LocalDate.now();
        
        // Manufacturing date should not be in the future
        if (request.getManufacturingDate() != null && request.getManufacturingDate().isAfter(now)) {
            throw new DomainValidationException("Manufacturing date cannot be in the future", 
                    "manufacturingDate", request.getManufacturingDate());
        }
        
        // Expiration date should be after manufacturing date if both are provided
        if (request.getManufacturingDate() != null && request.getExpirationDate() != null && 
                request.getExpirationDate().isBefore(request.getManufacturingDate())) {
            throw new DomainValidationException("Expiration date must be after manufacturing date", 
                    "expirationDate", request.getExpirationDate());
        }
    }

    private void publishBatchCreatedEvent(DomainBatchEntity batch) {
        log.debug("Publishing batch created event for batch ID: {}", batch.getId());
        
        try {
            // Create event DTO
            SharedBatchCreatedEventDTO event = batchMapper.toEventDTO(batch);
            
            // Publish event
            eventPublisher.publishBatchCreated(event);
            
            // Save to outbox for reliable delivery
            outboxService.saveEvent(
                "BatchCreated",
                batch.getId(),
                "Batch",
                event
            );
            
            log.debug("Successfully published batch created event for batch ID: {}", batch.getId());
            
        } catch (Exception ex) {
            log.error("Error publishing batch event: {}", ex.getMessage(), ex);
            // Don't fail the transaction, will be retried via outbox
        }
    }
}