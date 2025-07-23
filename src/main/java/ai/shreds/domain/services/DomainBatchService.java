package ai.shreds.domain.services;

import ai.shreds.domain.commands.DomainCreateBatchCommand;
import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainInsufficientStockException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.ports.DomainInputPortBatch;
import ai.shreds.domain.ports.DomainOutputPortBatchRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing batch business logic.
 * Handles batch creation, FIFO allocation, and batch management operations.
 */
@Service
public class DomainBatchService implements DomainInputPortBatch {
    
    private final DomainOutputPortBatchRepository batchRepository;
    private final DomainBatchAllocationService batchAllocationService;
    
    /**
     * Constructs the batch service with required dependencies.
     *
     * @param batchRepository the batch repository
     * @param batchAllocationService the batch allocation service
     */
    public DomainBatchService(
            DomainOutputPortBatchRepository batchRepository,
            DomainBatchAllocationService batchAllocationService) {
        this.batchRepository = batchRepository;
        this.batchAllocationService = batchAllocationService;
    }
    
    @Override
    public DomainBatchEntity createBatch(DomainCreateBatchCommand command) {
        if (command == null) {
            throw new DomainValidationException("Batch creation command cannot be null", "command", null);
        }
        
        validateBatchCreation(command);
        
        // Check if batch already exists
        if (batchRepository.existsByWarehouseIdAndProductIdAndBatchNumber(
                command.getWarehouseId(), 
                command.getProductId(), 
                command.getBatchNumber())) {
            throw new DomainValidationException(
                "Batch with this number already exists for the product in this warehouse",
                "batchNumber",
                command.getBatchNumber()
            );
        }
        
        // Create new batch entity
        DomainBatchEntity batch = DomainBatchEntity.createNew(
                UUID.randomUUID(),
                command.getWarehouseId(),
                command.getProductId(),
                command.getBatchNumber(),
                command.getQuantity(),
                command.getManufacturingDate(),
                command.getExpirationDate(),
                command.getSupplierId()
        );
        
        // Save and return
        return batchRepository.save(batch);
    }
    
    @Override
    public List<DomainBatchEntity> allocateForOutbound(
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue quantity) {
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null", "quantity", null);
        }
        
        if (quantity.isZero() || quantity.isNegative()) {
            throw new DomainValidationException(
                "Quantity must be positive for allocation",
                "quantity",
                quantity
            );
        }
        
        // Use the allocation service to perform FIFO allocation
        List<DomainBatchEntity> allocatedBatches = batchAllocationService.allocateFIFO(
                warehouseId, productId, quantity);
        
        // Validate that allocation was successful
        if (!validateBatchAvailability(allocatedBatches, quantity)) {
            DomainQuantityValue availableQuantity = calculateTotalAvailableQuantity(warehouseId, productId);
            throw new DomainInsufficientStockException(
                "Insufficient stock available in batches",
                warehouseId,
                productId,
                quantity,
                availableQuantity
            );
        }
        
        return allocatedBatches;
    }
    
    @Override
    public DomainBatchEntity increaseBatch(
            UUID warehouseId,
            DomainProductIdValue productId,
            String batchNumber,
            DomainQuantityValue additionalQuantity) {
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new DomainValidationException(
                "Batch number cannot be null or empty",
                "batchNumber",
                batchNumber
            );
        }
        
        if (additionalQuantity == null || additionalQuantity.isZero() || additionalQuantity.isNegative()) {
            throw new DomainValidationException(
                "Additional quantity must be positive",
                "additionalQuantity",
                additionalQuantity
            );
        }
        
        // Try to find existing batch
        DomainBatchEntity existingBatch = batchRepository.findByWarehouseIdAndProductIdAndBatchNumber(
                warehouseId, productId, batchNumber);
        
        if (existingBatch != null) {
            // Update existing batch
            existingBatch.increaseQuantity(additionalQuantity);
            return batchRepository.save(existingBatch);
        } else {
            // Create new batch if it doesn't exist
            DomainBatchEntity newBatch = DomainBatchEntity.createNew(
                    UUID.randomUUID(),
                    warehouseId,
                    productId,
                    batchNumber,
                    additionalQuantity,
                    null, // No manufacturing date for new batches
                    null, // No expiration date for new batches
                    null  // No supplier for internal increases
            );
            
            return batchRepository.save(newBatch);
        }
    }
    
    @Override
    public DomainBatchEntity findOldestAvailableBatch(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        return batchRepository.findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc(warehouseId, productId);
    }
    
    @Override
    public List<DomainBatchEntity> findAvailableBatches(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        return batchRepository.findByWarehouseIdAndProductIdWithAvailableQuantity(warehouseId, productId);
    }
    
    @Override
    public DomainQuantityValue calculateTotalAvailableQuantity(UUID warehouseId, DomainProductIdValue productId) {
        List<DomainBatchEntity> availableBatches = findAvailableBatches(warehouseId, productId);
        
        if (availableBatches.isEmpty()) {
            // Return zero quantity with default unit
            return new DomainQuantityValue(0, "pieces");
        }
        
        // Sum up quantities from all available batches
        DomainQuantityValue total = availableBatches.get(0).getQuantity().zero();
        for (DomainBatchEntity batch : availableBatches) {
            if (!batch.isEmpty()) {
                total = total.add(batch.getQuantity());
            }
        }
        
        return total;
    }
    
    @Override
    public boolean validateBatchAvailability(List<DomainBatchEntity> batches, DomainQuantityValue requestedQuantity) {
        if (batches == null || batches.isEmpty()) {
            return false;
        }
        
        if (requestedQuantity == null || requestedQuantity.isZero()) {
            return true;
        }
        
        // Calculate total available quantity from provided batches
        DomainQuantityValue totalAvailable = batches.get(0).getQuantity().zero();
        for (DomainBatchEntity batch : batches) {
            if (!batch.isEmpty()) {
                totalAvailable = totalAvailable.add(batch.getQuantity());
            }
        }
        
        return totalAvailable.isGreaterThanOrEqual(requestedQuantity);
    }
    
    /**
     * Validates batch creation parameters.
     *
     * @param command the batch creation command
     * @throws DomainValidationException if validation fails
     */
    private void validateBatchCreation(DomainCreateBatchCommand command) {
        if (command.getWarehouseId() == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (command.getProductId() == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (command.getBatchNumber() == null || command.getBatchNumber().trim().isEmpty()) {
            throw new DomainValidationException(
                "Batch number cannot be null or empty", 
                "batchNumber", 
                command.getBatchNumber()
            );
        }
        
        if (command.getQuantity() == null) {
            throw new DomainValidationException("Quantity cannot be null", "quantity", null);
        }
        
        if (command.getQuantity().isZero() || command.getQuantity().isNegative()) {
            throw new DomainValidationException(
                "Quantity must be positive", 
                "quantity", 
                command.getQuantity()
            );
        }
        
        // Validate dates if provided
        if (command.getManufacturingDate() != null && command.getExpirationDate() != null) {
            if (command.getManufacturingDate().isAfter(command.getExpirationDate())) {
                throw new DomainValidationException(
                    "Manufacturing date cannot be after expiration date",
                    "manufacturingDate",
                    command.getManufacturingDate()
                );
            }
        }
        
        if (command.getExpirationDate() != null && command.getExpirationDate().isBefore(LocalDate.now())) {
            throw new DomainValidationException(
                "Cannot create batch with past expiration date",
                "expirationDate",
                command.getExpirationDate()
            );
        }
        
        if (command.getManufacturingDate() != null && command.getManufacturingDate().isAfter(LocalDate.now())) {
            throw new DomainValidationException(
                "Manufacturing date cannot be in the future",
                "manufacturingDate",
                command.getManufacturingDate()
            );
        }
    }
    
    /**
     * Reduces quantity from a specific batch.
     *
     * @param batchId the batch ID
     * @param quantity the quantity to reduce
     * @return the updated batch
     * @throws DomainEntityNotFoundException if batch not found
     * @throws DomainInsufficientStockException if insufficient quantity in batch
     */
    public DomainBatchEntity reduceBatchQuantity(UUID batchId, DomainQuantityValue quantity) {
        if (batchId == null) {
            throw new DomainValidationException("Batch ID cannot be null", "batchId", null);
        }
        
        DomainBatchEntity batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new DomainEntityNotFoundException(
                "Batch not found",
                "DomainBatchEntity",
                batchId.toString()
            );
        }
        
        batch.reduceQuantity(quantity);
        return batchRepository.save(batch);
    }
    
    /**
     * Finds expired batches that need attention.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID (optional)
     * @return list of expired batches
     */
    public List<DomainBatchEntity> findExpiredBatches(UUID warehouseId, DomainProductIdValue productId) {
        List<DomainBatchEntity> batches;
        
        if (productId != null) {
            batches = findAvailableBatches(warehouseId, productId);
        } else {
            batches = batchRepository.findByWarehouseId(warehouseId);
        }
        
        return batches.stream()
                .filter(DomainBatchEntity::isExpired)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds batches nearing expiration.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID (optional)
     * @return list of batches nearing expiration
     */
    public List<DomainBatchEntity> findBatchesNearingExpiration(UUID warehouseId, DomainProductIdValue productId) {
        List<DomainBatchEntity> batches;
        
        if (productId != null) {
            batches = findAvailableBatches(warehouseId, productId);
        } else {
            batches = batchRepository.findByWarehouseId(warehouseId);
        }
        
        return batches.stream()
                .filter(DomainBatchEntity::isNearingExpiration)
                .collect(Collectors.toList());
    }
}