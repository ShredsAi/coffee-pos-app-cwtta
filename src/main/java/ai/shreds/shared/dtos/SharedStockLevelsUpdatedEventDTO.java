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
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;

/**
 * Event DTO for stock level updates.
 * Published when inventory quantities change in the system.
 * Contains both old and new quantities for comparison.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SharedStockLevelsUpdatedEventDTO extends SharedEventDTO {

    @NotNull(message = "Inventory item ID must not be null")
    private UUID inventoryItemId;
    
    @NotNull(message = "Warehouse ID must not be null")
    private UUID warehouseId;
    
    @NotNull(message = "Product ID must not be null")
    private UUID productId;
    
    @NotNull(message = "Old quantities must not be null")
    @Valid
    private SharedInventoryQuantitiesDTO oldQuantities;
    
    @NotNull(message = "New quantities must not be null")
    @Valid
    private SharedInventoryQuantitiesDTO newQuantities;
    
    @NotNull(message = "Timestamp must not be null")
    private LocalDateTime timestamp;
    
    /**
     * Builder for creating stock levels updated events.
     */
    @Builder
    public SharedStockLevelsUpdatedEventDTO(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            String aggregateType,
            LocalDateTime occurredAt,
            UUID inventoryItemId,
            UUID warehouseId,
            UUID productId,
            SharedInventoryQuantitiesDTO oldQuantities,
            SharedInventoryQuantitiesDTO newQuantities,
            LocalDateTime timestamp) {
        super(eventId, eventType, aggregateId, aggregateType, occurredAt);
        this.inventoryItemId = inventoryItemId;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.oldQuantities = oldQuantities;
        this.newQuantities = newQuantities;
        this.timestamp = timestamp;
    }
    
    /**
     * Creates a stock levels updated event with default event metadata.
     *
     * @param inventoryItemId ID of the inventory item that changed
     * @param warehouseId ID of the warehouse where the inventory item is located
     * @param productId ID of the product whose levels changed
     * @param oldQuantities Quantities before the update
     * @param newQuantities Quantities after the update
     * @return A new stock levels updated event
     */
    public static SharedStockLevelsUpdatedEventDTO create(
            UUID inventoryItemId,
            UUID warehouseId,
            UUID productId,
            SharedInventoryQuantitiesDTO oldQuantities,
            SharedInventoryQuantitiesDTO newQuantities) {
        
        LocalDateTime now = LocalDateTime.now();
        return SharedStockLevelsUpdatedEventDTO.builder()
            .eventId(UUID.randomUUID())
            .eventType("STOCK_LEVELS_UPDATED")
            .aggregateId(inventoryItemId)
            .aggregateType("INVENTORY_ITEM")
            .occurredAt(now)
            .inventoryItemId(inventoryItemId)
            .warehouseId(warehouseId)
            .productId(productId)
            .oldQuantities(oldQuantities)
            .newQuantities(newQuantities)
            .timestamp(now)
            .build();
    }
    
    /**
     * Checks if the available quantity increased.
     *
     * @return true if available quantity increased
     */
    public boolean isAvailableQuantityIncreased() {
        return newQuantities.getAvailableQty().compareTo(oldQuantities.getAvailableQty()) > 0;
    }
    
    /**
     * Checks if the available quantity decreased.
     *
     * @return true if available quantity decreased
     */
    public boolean isAvailableQuantityDecreased() {
        return newQuantities.getAvailableQty().compareTo(oldQuantities.getAvailableQty()) < 0;
    }
    
    /**
     * Checks if the total quantity changed significantly.
     *
     * @return true if total quantity changed
     */
    public boolean isTotalQuantityChanged() {
        return newQuantities.getTotalQty().compareTo(oldQuantities.getTotalQty()) != 0;
    }
    
    /**
     * Gets the difference in available quantity.
     *
     * @return the difference (new - old) in available quantity
     */
    public java.math.BigDecimal getAvailableQuantityDifference() {
        return newQuantities.getAvailableQty().subtract(oldQuantities.getAvailableQty());
    }
}