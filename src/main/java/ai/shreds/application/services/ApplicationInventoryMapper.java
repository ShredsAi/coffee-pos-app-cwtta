package ai.shreds.application.services;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;
import ai.shreds.shared.dtos.SharedInventoryThresholdsDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Mapper(componentModel = "spring")
public interface ApplicationInventoryMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "warehouseId", source = "warehouseId")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantities", expression = "java(toQuantitiesDTO(item))")
    @Mapping(target = "thresholds", expression = "java(toThresholdsDTO(item))")
    @Mapping(target = "lastMovementAt", source = "lastMovementAt")
    @Mapping(target = "version", source = "version")
    SharedInventoryItemResponseDTO toResponseDTO(DomainInventoryItemEntity item);

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

    @Mapping(target = "inventoryItemId", source = "oldQuantities.id")
    @Mapping(target = "warehouseId", source = "oldQuantities.warehouseId")
    @Mapping(target = "productId", source = "oldQuantities.productId.value")
    @Mapping(target = "oldQuantities", expression = "java(toQuantitiesDTO(oldItem))")
    @Mapping(target = "newQuantities", expression = "java(toQuantitiesDTO(newItem))")
    @Mapping(target = "timestamp", source = "timestamp")
    SharedStockLevelsUpdatedEventDTO toEventDTO(
        DomainInventoryItemEntity oldItem, 
        DomainInventoryItemEntity newItem, 
        LocalDateTime timestamp
    );

    // Overloaded method for single item (when we don't have old state)
    default SharedStockLevelsUpdatedEventDTO toEventDTO(DomainInventoryItemEntity item) {
        if (item == null) return null;
        
        SharedStockLevelsUpdatedEventDTO event = new SharedStockLevelsUpdatedEventDTO();
        event.setInventoryItemId(item.getId());
        event.setWarehouseId(item.getWarehouseId());
        event.setProductId(item.getProductId().getValue());
        event.setNewQuantities(toQuantitiesDTO(item));
        event.setOldQuantities(toQuantitiesDTO(item)); // Same as new when we don't have old state
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    // Helper method to create event with old and new quantities
    default SharedStockLevelsUpdatedEventDTO createStockLevelsUpdatedEvent(
            UUID inventoryItemId,
            UUID warehouseId, 
            String productId,
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
        return event;
    }
}