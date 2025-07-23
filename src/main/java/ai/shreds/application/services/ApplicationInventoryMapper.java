package ai.shreds.application.services;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;
import ai.shreds.shared.dtos.SharedInventoryThresholdsDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Mapper(componentModel = "spring", 
        builder = @Builder(disableBuilder = true),
        uses = {})
public interface ApplicationInventoryMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "warehouseId", source = "warehouseId")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantities", ignore = true)
    @Mapping(target = "thresholds", ignore = true)
    @Mapping(target = "lastMovementAt", source = "lastMovementAt")
    @Mapping(target = "version", source = "version")
    SharedInventoryItemResponseDTO toResponseDTO(DomainInventoryItemEntity item);
    
    // After mapping method to set quantities and thresholds
    default SharedInventoryItemResponseDTO toResponseDTOComplete(DomainInventoryItemEntity item) {
        SharedInventoryItemResponseDTO dto = toResponseDTO(item);
        if (dto != null) {
            dto.setQuantities(toQuantitiesDTO(item));
            dto.setThresholds(toThresholdsDTO(item));
        }
        return dto;
    }

    // Static method for quantities conversion
    static SharedInventoryQuantitiesDTO toQuantitiesDTO(DomainInventoryItemEntity item) {
        if (item == null) return null;

        SharedInventoryQuantitiesDTO quantities = new SharedInventoryQuantitiesDTO();
        quantities.setAvailableQty(item.getAvailableQuantity().getValue());
        quantities.setReservedQty(item.getReservedQuantity().getValue());
        quantities.setAllocatedQty(item.getAllocatedQuantity().getValue());
        quantities.setTotalQty(item.getTotalQuantity().getValue());
        quantities.setQtyUnit(item.getAvailableQuantity().getUnit());
        return quantities;
    }

    // Static method for thresholds conversion
    static SharedInventoryThresholdsDTO toThresholdsDTO(DomainInventoryItemEntity item) {
        if (item == null) return null;

        SharedInventoryThresholdsDTO thresholds = new SharedInventoryThresholdsDTO();
        thresholds.setSafetyStockLevel(item.getSafetyStockLevel().getValue());
        thresholds.setReorderPoint(item.getReorderPoint().getValue());
        return thresholds;
    }

    @Mapping(target = "inventoryItemId", source = "oldItem.id")
    @Mapping(target = "warehouseId", source = "oldItem.warehouseId")
    @Mapping(target = "productId", source = "oldItem.productId.value")
    @Mapping(target = "oldQuantities", ignore = true)
    @Mapping(target = "newQuantities", ignore = true)
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "eventType", constant = "STOCK_LEVELS_UPDATED")
    @Mapping(target = "aggregateId", source = "oldItem.id")
    @Mapping(target = "aggregateType", constant = "INVENTORY_ITEM")
    @Mapping(target = "occurredAt", source = "timestamp")
    SharedStockLevelsUpdatedEventDTO toEventDTO(
        DomainInventoryItemEntity oldItem,
        DomainInventoryItemEntity newItem,
        LocalDateTime timestamp
    );
    
    // After mapping method to set old and new quantities
    default SharedStockLevelsUpdatedEventDTO toEventDTOComplete(
        DomainInventoryItemEntity oldItem,
        DomainInventoryItemEntity newItem,
        LocalDateTime timestamp) {
        
        SharedStockLevelsUpdatedEventDTO dto = toEventDTO(oldItem, newItem, timestamp);
        if (dto != null) {
            dto.setOldQuantities(toQuantitiesDTO(oldItem));
            dto.setNewQuantities(toQuantitiesDTO(newItem));
        }
        return dto;
    }

    // Overloaded method for single item (when we don't have old state)
    default SharedStockLevelsUpdatedEventDTO toEventDTO(DomainInventoryItemEntity item) {
        if (item == null) return null;

        SharedStockLevelsUpdatedEventDTO event = new SharedStockLevelsUpdatedEventDTO();
        event.setInventoryItemId(item.getId());
        event.setWarehouseId(item.getWarehouseId());
        event.setProductId(item.getProductId().toUUID());
        event.setNewQuantities(toQuantitiesDTO(item));
        event.setOldQuantities(toQuantitiesDTO(item)); // Same as new when we don't have old state
        event.setTimestamp(LocalDateTime.now());
        event.setEventId(UUID.randomUUID());
        event.setEventType("STOCK_LEVELS_UPDATED");
        event.setAggregateId(item.getId());
        event.setAggregateType("INVENTORY_ITEM");
        event.setOccurredAt(LocalDateTime.now());
        return event;
    }

    // Helper method to create event with old and new quantities
    default SharedStockLevelsUpdatedEventDTO createStockLevelsUpdatedEvent(
            UUID inventoryItemId,
            UUID warehouseId,
            UUID productId, // This parameter is already UUID, so no conversion needed
            SharedInventoryQuantitiesDTO oldQuantities,
            SharedInventoryQuantitiesDTO newQuantities,
            LocalDateTime timestamp) {

        SharedStockLevelsUpdatedEventDTO event = new SharedStockLevelsUpdatedEventDTO();
        event.setInventoryItemId(inventoryItemId);
        event.setWarehouseId(warehouseId);
        event.setProductId(productId);
        event.setOldQuantities(oldQuantities);
        event.setNewQuantities(newQuantities);
        event.setTimestamp(timestamp);
        event.setEventId(UUID.randomUUID());
        event.setEventType("STOCK_LEVELS_UPDATED");
        event.setAggregateId(inventoryItemId);
        event.setAggregateType("INVENTORY_ITEM");
        event.setOccurredAt(timestamp);
        return event;
    }
}