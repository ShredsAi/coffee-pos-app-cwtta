package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.exceptions.DomainInsufficientStockException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.ports.DomainOutputPortBatchRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for batch allocation using FIFO (First In, First Out) algorithm.
 * Handles complex batch allocation logic for outbound movements.
 */
public class DomainBatchAllocationService {
    
    private final DomainOutputPortBatchRepository batchRepository;
    
    /**
     * Constructs the batch allocation service.
     *
     * @param batchRepository the batch repository
     */
    public DomainBatchAllocationService(DomainOutputPortBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }
    
    /**
     * Allocates batches for outbound movement using FIFO algorithm.
     * Returns list of batches and the quantities to be consumed from each.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param requestedQuantity the quantity to allocate
     * @return list of batches allocated for the quantity
     * @throws DomainInsufficientStockException if insufficient stock available
     */
    public List<DomainBatchEntity> allocateFIFO(
            UUID warehouseId, 
            DomainProductIdValue productId, 
            DomainQuantityValue requestedQuantity) {
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (requestedQuantity == null || requestedQuantity.isZero() || requestedQuantity.isNegative()) {
            throw new DomainValidationException(
                "Requested quantity must be positive", 
                "requestedQuantity", 
                requestedQuantity
            );
        }
        
        // Get available batches in FIFO order (oldest first)
        List<DomainBatchEntity> availableBatches = batchRepository
                .findByWarehouseIdAndProductIdWithAvailableQuantity(warehouseId, productId);
        
        if (availableBatches.isEmpty()) {
            throw new DomainInsufficientStockException(
                "No available batches found",
                warehouseId,
                productId,
                requestedQuantity,
                new DomainQuantityValue(0, requestedQuantity.getUnit())
            );
        }
        
        // Filter out expired or empty batches
        List<DomainBatchEntity> usableBatches = filterUsableBatches(availableBatches);
        
        if (usableBatches.isEmpty()) {
            throw new DomainInsufficientStockException(
                "No usable batches found (all expired or empty)",
                warehouseId,
                productId,
                requestedQuantity,
                new DomainQuantityValue(0, requestedQuantity.getUnit())
            );
        }
        
        // Allocate using FIFO algorithm
        List<DomainBatchEntity> allocatedBatches = new ArrayList<>();
        DomainQuantityValue remainingToAllocate = requestedQuantity;
        
        for (DomainBatchEntity batch : usableBatches) {
            if (remainingToAllocate.isZero()) {
                break; // All quantity allocated
            }
            
            DomainQuantityValue batchAvailable = batch.getQuantity();
            
            if (batchAvailable.isGreaterThan(remainingToAllocate)) {
                // Partial allocation from this batch
                batch.reduceQuantity(remainingToAllocate);
                allocatedBatches.add(batch);
                remainingToAllocate = remainingToAllocate.zero();
            } else {
                // Full allocation from this batch
                batch.reduceQuantity(batchAvailable);
                allocatedBatches.add(batch);
                remainingToAllocate = remainingToAllocate.subtract(batchAvailable);
            }
        }
        
        // Check if we allocated enough
        if (!remainingToAllocate.isZero()) {
            // Rollback the allocations
            rollbackAllocations(allocatedBatches, requestedQuantity.subtract(remainingToAllocate));
            
            DomainQuantityValue totalAvailable = calculateTotalAvailable(usableBatches);
            throw new DomainInsufficientStockException(
                "Insufficient stock available across all batches",
                warehouseId,
                productId,
                requestedQuantity,
                totalAvailable
            );
        }
        
        // Save updated batches
        for (DomainBatchEntity batch : allocatedBatches) {
            batchRepository.save(batch);
        }
        
        return allocatedBatches;
    }
    
    /**
     * Validates that batches have sufficient quantity for the requested allocation.
     *
     * @param batches the list of batches to check
     * @param requestedQuantity the quantity requested
     * @return true if batches have sufficient quantity
     */
    public boolean validateBatchAvailability(
            List<DomainBatchEntity> batches, 
            DomainQuantityValue requestedQuantity) {
        
        if (batches == null || batches.isEmpty()) {
            return false;
        }
        
        if (requestedQuantity == null || requestedQuantity.isZero()) {
            return true;
        }
        
        DomainQuantityValue totalAvailable = calculateTotalAvailable(batches);
        return totalAvailable.isGreaterThanOrEqual(requestedQuantity);
    }
    
    /**
     * Reserves quantity in batches without actually consuming it.
     * Useful for temporary allocations that might be cancelled.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param requestedQuantity the quantity to reserve
     * @return list of batches with reserved quantities
     */
    public List<DomainBatchEntity> reserveQuantityFIFO(
            UUID warehouseId, 
            DomainProductIdValue productId, 
            DomainQuantityValue requestedQuantity) {
        
        // This is a simulation of allocation without actually reducing quantities
        List<DomainBatchEntity> availableBatches = batchRepository
                .findByWarehouseIdAndProductIdWithAvailableQuantity(warehouseId, productId);
        
        List<DomainBatchEntity> usableBatches = filterUsableBatches(availableBatches);
        
        if (!validateBatchAvailability(usableBatches, requestedQuantity)) {
            throw new DomainInsufficientStockException(
                "Insufficient stock for reservation",
                warehouseId,
                productId,
                requestedQuantity,
                calculateTotalAvailable(usableBatches)
            );
        }
        
        List<DomainBatchEntity> reservedBatches = new ArrayList<>();
        DomainQuantityValue remainingToReserve = requestedQuantity;
        
        for (DomainBatchEntity batch : usableBatches) {
            if (remainingToReserve.isZero()) {
                break;
            }
            
            DomainQuantityValue batchAvailable = batch.getQuantity();
            
            if (batchAvailable.isGreaterThanOrEqual(remainingToReserve)) {
                reservedBatches.add(batch);
                remainingToReserve = remainingToReserve.zero();
            } else {
                reservedBatches.add(batch);
                remainingToReserve = remainingToReserve.subtract(batchAvailable);
            }
        }
        
        return reservedBatches;
    }
    
    /**
     * Filters batches to include only usable ones (not expired, not empty).
     *
     * @param batches the list of batches to filter
     * @return filtered list of usable batches
     */
    private List<DomainBatchEntity> filterUsableBatches(List<DomainBatchEntity> batches) {
        List<DomainBatchEntity> usableBatches = new ArrayList<>();
        
        for (DomainBatchEntity batch : batches) {
            if (!batch.isEmpty() && !batch.isExpired()) {
                usableBatches.add(batch);
            }
        }
        
        return usableBatches;
    }
    
    /**
     * Calculates total available quantity from a list of batches.
     *
     * @param batches the list of batches
     * @return total available quantity
     */
    private DomainQuantityValue calculateTotalAvailable(List<DomainBatchEntity> batches) {
        if (batches.isEmpty()) {
            return new DomainQuantityValue(0, "pieces");
        }
        
        DomainQuantityValue total = batches.get(0).getQuantity().zero();
        
        for (DomainBatchEntity batch : batches) {
            if (!batch.isEmpty()) {
                total = total.add(batch.getQuantity());
            }
        }
        
        return total;
    }
    
    /**
     * Rollback allocations in case of failure.
     *
     * @param allocatedBatches the batches that were allocated
     * @param allocatedQuantity the total quantity that was allocated
     */
    private void rollbackAllocations(
            List<DomainBatchEntity> allocatedBatches, 
            DomainQuantityValue allocatedQuantity) {
        
        DomainQuantityValue remainingToRollback = allocatedQuantity;
        
        // Rollback in reverse order
        for (int i = allocatedBatches.size() - 1; i >= 0; i--) {
            DomainBatchEntity batch = allocatedBatches.get(i);
            
            if (remainingToRollback.isZero()) {
                break;
            }
            
            // Calculate how much was taken from this batch
            DomainQuantityValue originalQuantity = batch.getQuantity();
            
            if (remainingToRollback.isGreaterThanOrEqual(originalQuantity)) {
                // This entire batch quantity was allocated
                batch.increaseQuantity(originalQuantity);
                remainingToRollback = remainingToRollback.subtract(originalQuantity);
            } else {
                // Partial allocation from this batch
                batch.increaseQuantity(remainingToRollback);
                remainingToRollback = remainingToRollback.zero();
            }
        }
    }
    
    /**
     * Gets the optimal allocation strategy for a given quantity.
     * Returns information about how batches would be allocated without actually allocating.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param requestedQuantity the quantity to allocate
     * @return allocation strategy information
     */
    public AllocationStrategy getOptimalAllocationStrategy(
            UUID warehouseId, 
            DomainProductIdValue productId, 
            DomainQuantityValue requestedQuantity) {
        
        List<DomainBatchEntity> availableBatches = batchRepository
                .findByWarehouseIdAndProductIdWithAvailableQuantity(warehouseId, productId);
        
        List<DomainBatchEntity> usableBatches = filterUsableBatches(availableBatches);
        
        return AllocationStrategy.builder()
                .totalAvailableQuantity(calculateTotalAvailable(usableBatches))
                .numberOfBatchesNeeded(calculateNumberOfBatchesNeeded(usableBatches, requestedQuantity))
                .canFulfill(validateBatchAvailability(usableBatches, requestedQuantity))
                .oldestBatchAge(getOldestBatchAge(usableBatches))
                .build();
    }
    
    private int calculateNumberOfBatchesNeeded(
            List<DomainBatchEntity> batches, 
            DomainQuantityValue requestedQuantity) {
        
        int batchesNeeded = 0;
        DomainQuantityValue remaining = requestedQuantity;
        
        for (DomainBatchEntity batch : batches) {
            if (remaining.isZero()) {
                break;
            }
            
            batchesNeeded++;
            DomainQuantityValue batchQuantity = batch.getQuantity();
            
            if (batchQuantity.isGreaterThanOrEqual(remaining)) {
                remaining = remaining.zero();
            } else {
                remaining = remaining.subtract(batchQuantity);
            }
        }
        
        return batchesNeeded;
    }
    
    private Long getOldestBatchAge(List<DomainBatchEntity> batches) {
        if (batches.isEmpty()) {
            return null;
        }
        
        // Return days since the oldest batch was received
        DomainBatchEntity oldestBatch = batches.get(0); // Assuming FIFO order
        return java.time.temporal.ChronoUnit.DAYS.between(
                oldestBatch.getReceivedAt().toLocalDate(), 
                java.time.LocalDate.now()
        );
    }
    
    /**
     * Inner class representing allocation strategy information.
     */
    public static class AllocationStrategy {
        private final DomainQuantityValue totalAvailableQuantity;
        private final int numberOfBatchesNeeded;
        private final boolean canFulfill;
        private final Long oldestBatchAge;
        
        private AllocationStrategy(Builder builder) {
            this.totalAvailableQuantity = builder.totalAvailableQuantity;
            this.numberOfBatchesNeeded = builder.numberOfBatchesNeeded;
            this.canFulfill = builder.canFulfill;
            this.oldestBatchAge = builder.oldestBatchAge;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public DomainQuantityValue getTotalAvailableQuantity() { return totalAvailableQuantity; }
        public int getNumberOfBatchesNeeded() { return numberOfBatchesNeeded; }
        public boolean canFulfill() { return canFulfill; }
        public Long getOldestBatchAge() { return oldestBatchAge; }
        
        public static class Builder {
            private DomainQuantityValue totalAvailableQuantity;
            private int numberOfBatchesNeeded;
            private boolean canFulfill;
            private Long oldestBatchAge;
            
            public Builder totalAvailableQuantity(DomainQuantityValue totalAvailableQuantity) {
                this.totalAvailableQuantity = totalAvailableQuantity;
                return this;
            }
            
            public Builder numberOfBatchesNeeded(int numberOfBatchesNeeded) {
                this.numberOfBatchesNeeded = numberOfBatchesNeeded;
                return this;
            }
            
            public Builder canFulfill(boolean canFulfill) {
                this.canFulfill = canFulfill;
                return this;
            }
            
            public Builder oldestBatchAge(Long oldestBatchAge) {
                this.oldestBatchAge = oldestBatchAge;
                return this;
            }
            
            public AllocationStrategy build() {
                return new AllocationStrategy(this);
            }
        }
    }
}