package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;
import ai.shreds.shared.value_objects.SharedQuantityValue;

/**
 * Event DTO for stock movement creation.
 * Published when a new stock movement is created in the system.
 * Contains stock movement details and updated inventory quantities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SharedStockMovementCreatedEventDTO extends SharedEventDTO {

    @NotNull(message = "Movement ID must not be null")
    private UUID movementId;

    @NotNull(message = "Warehouse ID must not be null")
    private UUID warehouseId;

    @NotNull(message = "Product ID must not be null")
    private UUID productId;

    @NotNull(message = "Movement type must not be null")
    private SharedStockMovementTypeEnum movementType;

    @NotNull(message = "Quantity must not be null")
    @Valid
    private SharedQuantityValue quantity;

    @NotNull(message = "Updated quantities must not be null")
    @Valid
    private SharedInventoryQuantitiesDTO updatedQuantities;

    @NotNull(message = "Timestamp must not be null")
    private LocalDateTime timestamp;

    /**
     * Builder for creating stock movement created events.
     */
    @Builder(builderMethodName = "stockMovementCreatedEventDTOBuilder")
    public SharedStockMovementCreatedEventDTO(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            String aggregateType,
            LocalDateTime occurredAt,
            UUID movementId,
            UUID warehouseId,
            UUID productId,
            SharedStockMovementTypeEnum movementType,
            SharedQuantityValue quantity,
            SharedInventoryQuantitiesDTO updatedQuantities,
            LocalDateTime timestamp) {
        super(eventId, eventType, aggregateId, aggregateType, occurredAt);
        this.movementId = movementId;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.updatedQuantities = updatedQuantities;
        this.timestamp = timestamp;
    }

    /**
     * Creates a stock movement created event with default event metadata.
     *
     * @param movementId        ID of the created movement
     * @param warehouseId       ID of the warehouse where the movement occurred
     * @param productId         ID of the product that moved
     * @param movementType      Type of movement (INBOUND, OUTBOUND, etc.)
     * @param quantity          Quantity that moved
     * @param updatedQuantities Updated inventory quantities after the movement
     * @return A new stock movement created event
     */
    public static SharedStockMovementCreatedEventDTO create(
            UUID movementId,
            UUID warehouseId,
            UUID productId,
            SharedStockMovementTypeEnum movementType,
            SharedQuantityValue quantity,
            SharedInventoryQuantitiesDTO updatedQuantities) {

        LocalDateTime now = LocalDateTime.now();
        return SharedStockMovementCreatedEventDTO.stockMovementCreatedEventDTOBuilder()
                .eventId(UUID.randomUUID())
                .eventType("STOCK_MOVEMENT_CREATED")
                .aggregateId(movementId)
                .aggregateType("STOCK_MOVEMENT")
                .occurredAt(now)
                .movementId(movementId)
                .warehouseId(warehouseId)
                .productId(productId)
                .movementType(movementType)
                .quantity(quantity)
                .updatedQuantities(updatedQuantities)
                .timestamp(now)
                .build();
    }

    /**
     * Determines if this movement affects inventory levels significantly.
     *
     * @return true if this is an INBOUND or OUTBOUND movement type
     */
    public boolean isInventoryChangingMovement() {
        return movementType == SharedStockMovementTypeEnum.INBOUND ||
               movementType == SharedStockMovementTypeEnum.OUTBOUND;
    }
}
