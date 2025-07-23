package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.ports.DomainInputPortInventoryQuery;
import ai.shreds.domain.ports.DomainOutputPortInventoryItemRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Domain service implementing inventory query operations.
 * Handles read-only inventory queries and stock level calculations.
 */
@Service
public class DomainInventoryQueryService implements DomainInputPortInventoryQuery {
    
    private final DomainOutputPortInventoryItemRepository inventoryRepository;
    
    /**
     * Constructs the inventory query service with required dependencies.
     *
     * @param inventoryRepository the inventory repository
     */
    public DomainInventoryQueryService(DomainOutputPortInventoryItemRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    @Override
    public DomainInventoryItemEntity findByWarehouseAndProduct(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        DomainInventoryItemEntity inventoryItem = inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId);
        
        if (inventoryItem == null) {
            throw new DomainEntityNotFoundException(
                "Inventory item not found for warehouse and product",
                "DomainInventoryItemEntity",
                warehouseId + "/" + productId.getValue()
            );
        }
        
        return inventoryItem;
    }
    
    @Override
    public Map<String, BigDecimal> getStockLevels(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        DomainInventoryItemEntity inventoryItem = inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId);
        
        Map<String, BigDecimal> stockLevels = new HashMap<>();
        
        if (inventoryItem != null) {
            stockLevels.put("availableQty", inventoryItem.getAvailableQuantity().getValue());
            stockLevels.put("reservedQty", inventoryItem.getReservedQuantity().getValue());
            stockLevels.put("allocatedQty", inventoryItem.getAllocatedQuantity().getValue());
            stockLevels.put("totalQty", inventoryItem.getTotalQuantity().getValue());
            stockLevels.put("safetyStockLevel", inventoryItem.getSafetyStockLevel().getValue());
            stockLevels.put("reorderPoint", inventoryItem.getReorderPoint().getValue());
        } else {
            // Return zero levels if inventory item doesn't exist
            stockLevels.put("availableQty", BigDecimal.ZERO);
            stockLevels.put("reservedQty", BigDecimal.ZERO);
            stockLevels.put("allocatedQty", BigDecimal.ZERO);
            stockLevels.put("totalQty", BigDecimal.ZERO);
            stockLevels.put("safetyStockLevel", BigDecimal.ZERO);
            stockLevels.put("reorderPoint", BigDecimal.ZERO);
        }
        
        return stockLevels;
    }
    
    @Override
    public boolean existsByWarehouseAndProduct(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null || productId == null) {
            return false;
        }
        
        return inventoryRepository.existsByWarehouseIdAndProductId(warehouseId, productId);
    }
    
    @Override
    public BigDecimal getAvailableQuantity(UUID warehouseId, DomainProductIdValue productId) {
        try {
            DomainInventoryItemEntity inventoryItem = findByWarehouseAndProduct(warehouseId, productId);
            return inventoryItem.getAvailableQuantity().getValue();
        } catch (DomainEntityNotFoundException e) {
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean hasSufficientStock(UUID warehouseId, DomainProductIdValue productId, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return true; // Zero or negative requests are always satisfied
        }
        
        BigDecimal availableQty = getAvailableQuantity(warehouseId, productId);
        return availableQty.compareTo(requestedQuantity) >= 0;
    }
    
    /**
     * Checks if inventory item is at or below reorder point.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return true if at or below reorder point
     */
    public boolean isAtReorderPoint(UUID warehouseId, DomainProductIdValue productId) {
        try {
            DomainInventoryItemEntity inventoryItem = findByWarehouseAndProduct(warehouseId, productId);
            return inventoryItem.isAtReorderPoint();
        } catch (DomainEntityNotFoundException e) {
            return true; // Non-existent items are considered at reorder point
        }
    }
    
    /**
     * Checks if inventory item is below safety stock level.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return true if below safety stock level
     */
    public boolean isBelowSafetyStockLevel(UUID warehouseId, DomainProductIdValue productId) {
        try {
            DomainInventoryItemEntity inventoryItem = findByWarehouseAndProduct(warehouseId, productId);
            return inventoryItem.getAvailableQuantity().getValue().compareTo(inventoryItem.getSafetyStockLevel().getValue()) < 0;
        } catch (DomainEntityNotFoundException e) {
            return true; // Non-existent items are considered below safety stock
        }
    }
}