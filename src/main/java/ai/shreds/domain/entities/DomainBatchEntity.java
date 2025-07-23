package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainInsufficientStockException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.shared.dtos.SharedBatchResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a batch of product inventory.
 * Tracks batch-specific information such as batch number, quantity, and expiration date.
 * Supports FIFO allocation and batch-level inventory management.
 */
public class DomainBatchEntity {
    private final UUID id;
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final String batchNumber;
    private DomainQuantityValue quantity;
    private final LocalDate manufacturingDate;
    private final LocalDate expirationDate;
    private final LocalDateTime receivedAt;
    private final UUID supplierId;
    private Long version;

    /**
     * Constructs a new batch entity with comprehensive validation.
     *
     * @param id the batch ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the unique batch number
     * @param quantity the quantity in the batch
     * @param manufacturingDate when the product was manufactured (optional)
     * @param expirationDate when the product expires (optional)
     * @param receivedAt when the batch was received
     * @param supplierId the supplier ID (optional)
     * @param version optimistic locking version
     */
    public DomainBatchEntity(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            String batchNumber,
            DomainQuantityValue quantity,
            LocalDate manufacturingDate,
            LocalDate expirationDate,
            LocalDateTime receivedAt,
            UUID supplierId,
            Long version) {
        
        validateConstructorParameters(id, warehouseId, productId, batchNumber, quantity, receivedAt);
        validateDates(manufacturingDate, expirationDate);
        
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.receivedAt = receivedAt;
        this.supplierId = supplierId; // Optional
        this.version = version;
    }
    
    private void validateConstructorParameters(
            UUID id, UUID warehouseId, DomainProductIdValue productId, 
            String batchNumber, DomainQuantityValue quantity, LocalDateTime receivedAt) {
        
        if (id == null) {
            throw new DomainValidationException("Batch ID cannot be null", "id", null);
        }
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new DomainValidationException("Batch number cannot be null or empty", "batchNumber", batchNumber);
        }
        
        // Additional batch number validation
        if (batchNumber.trim().length() > 64) {
            throw new DomainValidationException(
                "Batch number cannot exceed 64 characters", 
                "batchNumber", 
                batchNumber
            );
        }
        
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null", "quantity", null);
        }
        
        if (quantity.isNegative()) {
            throw new DomainValidationException("Quantity cannot be negative", "quantity", quantity);
        }
        
        if (receivedAt == null) {
            throw new DomainValidationException("Received at cannot be null", "receivedAt", null);
        }
        
        if (receivedAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("Received at cannot be in the future", "receivedAt", receivedAt);
        }
    }
    
    private void validateDates(LocalDate manufacturingDate, LocalDate expirationDate) {
        // Manufacturing and expiration dates are optional, but if provided, must be valid
        if (manufacturingDate != null && expirationDate != null) {
            if (manufacturingDate.isAfter(expirationDate)) {
                throw new DomainValidationException(
                    "Manufacturing date cannot be after expiration date",
                    "manufacturingDate",
                    manufacturingDate
                );
            }
            
            if (expirationDate.isBefore(LocalDate.now())) {
                throw new DomainValidationException(
                    "Expiration date cannot be in the past",
                    "expirationDate",
                    expirationDate
                );
            }
        }
        
        if (manufacturingDate != null && manufacturingDate.isAfter(LocalDate.now())) {
            throw new DomainValidationException(
                "Manufacturing date cannot be in the future",
                "manufacturingDate",
                manufacturingDate
            );
        }
    }
    
    /**
     * Factory method to create a new batch.
     *
     * @param id the batch ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @param quantity the initial quantity
     * @param manufacturingDate the manufacturing date (optional)
     * @param expirationDate the expiration date (optional) 
     * @param supplierId the supplier ID (optional)
     * @return a new batch entity
     */
    public static DomainBatchEntity createNew(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            String batchNumber,
            DomainQuantityValue quantity,
            LocalDate manufacturingDate,
            LocalDate expirationDate,
            UUID supplierId) {
        
        return new DomainBatchEntity(
                id,
                warehouseId,
                productId,
                batchNumber,
                quantity,
                manufacturingDate,
                expirationDate,
                LocalDateTime.now(),
                supplierId,
                0L
        );
    }

    /**
     * Reduces the quantity in this batch.
     * Used for outbound movements following FIFO allocation.
     *
     * @param amount the amount to reduce
     * @throws DomainInsufficientStockException if there's not enough quantity in the batch
     */
    public void reduceQuantity(DomainQuantityValue amount) {
        if (amount == null) {
            throw new DomainValidationException("Amount cannot be null", "amount", null);
        }
        
        if (amount.isNegative()) {
            throw new DomainValidationException("Amount cannot be negative", "amount", amount);
        }
        
        if (amount.isZero()) {
            return; // No change needed
        }
        
        if (!hasAvailableQuantity(amount)) {
            throw new DomainInsufficientStockException(
                "Insufficient quantity in batch",
                warehouseId,
                productId,
                amount,
                quantity
            );
        }
        
        quantity = quantity.subtract(amount);
        version++;
    }

    /**
     * Increases the quantity in this batch.
     * Used for returns or adjustments to existing batches.
     *
     * @param amount the amount to add
     */
    public void increaseQuantity(DomainQuantityValue amount) {
        if (amount == null) {
            throw new DomainValidationException("Amount cannot be null", "amount", null);
        }
        
        if (amount.isNegative()) {
            throw new DomainValidationException("Amount cannot be negative", "amount", amount);
        }
        
        if (amount.isZero()) {
            return; // No change needed
        }
        
        quantity = quantity.add(amount);
        version++;
    }

    /**
     * Checks if the batch is expired based on the expiration date.
     *
     * @return true if the batch has an expiration date and it's in the past
     */
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if the batch has sufficient quantity available.
     *
     * @param requested the requested quantity
     * @return true if the batch has enough quantity available
     */
    public boolean hasAvailableQuantity(DomainQuantityValue requested) {
        return quantity.isGreaterThanOrEqual(requested);
    }
    
    /**
     * Checks if this batch is empty (zero quantity).
     *
     * @return true if the batch has zero quantity
     */
    public boolean isEmpty() {
        return quantity.isZero();
    }
    
    /**
     * Gets the remaining shelf life in days from today until expiration.
     *
     * @return the number of days until expiration, or null if no expiration date
     */
    public Long getRemainingShelfLifeDays() {
        if (expirationDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }
    
    /**
     * Checks if the batch is nearing expiration (within 30 days).
     *
     * @return true if the batch will expire within 30 days
     */
    public boolean isNearingExpiration() {
        Long remainingDays = getRemainingShelfLifeDays();
        return remainingDays != null && remainingDays <= 30 && remainingDays > 0;
    }

    /**
     * Converts this domain entity to a DTO for the application layer.
     *
     * @return a SharedBatchResponseDTO representation
     */
    public SharedBatchResponseDTO toDTO() {
        return SharedBatchResponseDTO.builder()
                .id(id)
                .warehouseId(warehouseId)
                .productId(productId.toUUID())
                .batchNumber(batchNumber)
                .quantity(quantity.toSharedQuantity())
                .manufacturingDate(manufacturingDate)
                .expirationDate(expirationDate)
                .receivedAt(receivedAt)
                .supplierId(supplierId)
                .version(version)
                .build();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public DomainProductIdValue getProductId() {
        return productId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public DomainQuantityValue getQuantity() {
        return quantity;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainBatchEntity that = (DomainBatchEntity) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DomainBatchEntity{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", batchNumber='" + batchNumber + '\'' +
                ", quantity=" + quantity +
                ", expirationDate=" + expirationDate +
                ", receivedAt=" + receivedAt +
                '}';
    }
}