package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ai.shreds.shared.value_objects.SharedQuantityValue;

/**
 * Event DTO for batch creation.
 * Published when a new batch is created in the system.
 * Contains batch details including FIFO ordering information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SharedBatchCreatedEventDTO extends SharedEventDTO {

    @NotNull(message = "Batch ID must not be null")
    private UUID batchId;
    
    @NotNull(message = "Warehouse ID must not be null")
    private UUID warehouseId;
    
    @NotNull(message = "Product ID must not be null")
    private UUID productId;
    
    @NotBlank(message = "Batch number must not be blank")
    private String batchNumber;
    
    @NotNull(message = "Quantity must not be null")
    @Valid
    private SharedQuantityValue quantity;
    
    private LocalDate expirationDate;
    
    @NotNull(message = "FIFO order must not be null")
    private Integer fifoOrder;
    
    @NotNull(message = "Timestamp must not be null")
    private LocalDateTime timestamp;
    
    /**
     * Builder for creating batch created events.
     */
    @Builder
    public SharedBatchCreatedEventDTO(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            String aggregateType,
            LocalDateTime occurredAt,
            UUID batchId,
            UUID warehouseId,
            UUID productId,
            String batchNumber,
            SharedQuantityValue quantity,
            LocalDate expirationDate,
            Integer fifoOrder,
            LocalDateTime timestamp) {
        super(eventId, eventType, aggregateId, aggregateType, occurredAt);
        this.batchId = batchId;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.fifoOrder = fifoOrder;
        this.timestamp = timestamp;
    }
    
    /**
     * Creates a batch created event with default event metadata.
     *
     * @param batchId ID of the created batch
     * @param warehouseId ID of the warehouse where the batch is located
     * @param productId ID of the product in the batch
     * @param batchNumber Business identifier for the batch
     * @param quantity Initial quantity in the batch
     * @param expirationDate Expiration date of the batch (optional)
     * @param fifoOrder FIFO order for allocation priority
     * @return A new batch created event
     */
    public static SharedBatchCreatedEventDTO create(
            UUID batchId,
            UUID warehouseId,
            UUID productId,
            String batchNumber,
            SharedQuantityValue quantity,
            LocalDate expirationDate,
            Integer fifoOrder) {
        
        LocalDateTime now = LocalDateTime.now();
        return SharedBatchCreatedEventDTO.builder()
            .eventId(UUID.randomUUID())
            .eventType("BATCH_CREATED")
            .aggregateId(batchId)
            .aggregateType("BATCH")
            .occurredAt(now)
            .batchId(batchId)
            .warehouseId(warehouseId)
            .productId(productId)
            .batchNumber(batchNumber)
            .quantity(quantity)
            .expirationDate(expirationDate)
            .fifoOrder(fifoOrder)
            .timestamp(now)
            .build();
    }
    
    /**
     * Checks if this batch has an expiration date.
     *
     * @return true if the batch has an expiration date
     */
    public boolean hasExpirationDate() {
        return expirationDate != null;
    }
    
    /**
     * Checks if this batch will expire soon (within 30 days).
     *
     * @return true if the batch expires within 30 days
     */
    public boolean isExpiringSoon() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now().plusDays(30));
    }
    
    /**
     * Checks if this batch is already expired.
     *
     * @return true if the batch is expired
     */
    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now());
    }
}