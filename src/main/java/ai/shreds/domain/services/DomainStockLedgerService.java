package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.ports.DomainInputPortStockLedger;
import ai.shreds.domain.ports.DomainOutputPortStockMovementRepository;
import ai.shreds.domain.ports.DomainOutputPortInventoryItemRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Domain service implementing stock ledger operations.
 * Handles stock movement history queries and current quantity calculations.
 */
@Service
public class DomainStockLedgerService implements DomainInputPortStockLedger {
    
    private final DomainOutputPortStockMovementRepository movementRepository;
    private final DomainOutputPortInventoryItemRepository inventoryRepository;
    
    /**
     * Constructs the stock ledger service with required dependencies.
     *
     * @param movementRepository the movement repository
     * @param inventoryRepository the inventory repository
     */
    public DomainStockLedgerService(
            DomainOutputPortStockMovementRepository movementRepository,
            DomainOutputPortInventoryItemRepository inventoryRepository) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
    }
    
    @Override
    public List<DomainStockMovementEntity> getMovementHistory(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType,
            Integer page,
            Integer size) {
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (page == null || page < 0) {
            page = 0;
        }
        
        if (size == null || size <= 0) {
            size = 20;
        }
        
        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30); // Default to last 30 days
        }
        
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new DomainValidationException(
                "Start date cannot be after end date", 
                "startDate", 
                startDate
            );
        }
        
        String movementTypeString = movementType != null ? movementType.name() : null;
        
        return movementRepository.findByWarehouseIdAndProductIdAndDateRange(
                warehouseId, 
                productId.toUUID(), 
                startDate, 
                endDate, 
                movementTypeString, 
                page, 
                size, 
                "DESC" // Most recent first
        );
    }
    
    @Override
    public Map<String, DomainQuantityValue> calculateCurrentQuantities(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        Map<String, DomainQuantityValue> quantities = new HashMap<>();
        
        // Try to get current quantities from inventory item
        try {
            var inventoryItem = inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId);
            
            if (inventoryItem != null) {
                quantities.put("available", inventoryItem.getAvailableQuantity());
                quantities.put("reserved", inventoryItem.getReservedQuantity());
                quantities.put("allocated", inventoryItem.getAllocatedQuantity());
                quantities.put("total", inventoryItem.getTotalQuantity());
            } else {
                // If no inventory item exists, return zero quantities
                DomainQuantityValue zero = new DomainQuantityValue(0, "pieces");
                quantities.put("available", zero);
                quantities.put("reserved", zero);
                quantities.put("allocated", zero);
                quantities.put("total", zero);
            }
        } catch (Exception e) {
            // Fallback to zero quantities
            DomainQuantityValue zero = new DomainQuantityValue(0, "pieces");
            quantities.put("available", zero);
            quantities.put("reserved", zero);
            quantities.put("allocated", zero);
            quantities.put("total", zero);
        }
        
        return quantities;
    }
    
    @Override
    public long countMovements(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType) {
        
        if (warehouseId == null || productId == null) {
            return 0L;
        }
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        String movementTypeString = movementType != null ? movementType.name() : null;
        
        return movementRepository.countByWarehouseIdAndProductIdAndDateRange(
                warehouseId, 
                productId.toUUID(), 
                startDate, 
                endDate, 
                movementTypeString
        );
    }
    
    @Override
    public DomainStockMovementEntity getLastMovement(UUID warehouseId, DomainProductIdValue productId) {
        if (warehouseId == null || productId == null) {
            return null;
        }
        
        return movementRepository.findMostRecentByWarehouseIdAndProductId(warehouseId, productId);
    }
    
    /**
     * Calculates total inbound quantity for a period.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date
     * @param endDate the end date
     * @return total inbound quantity
     */
    public DomainQuantityValue calculateTotalInbound(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        List<DomainStockMovementEntity> inboundMovements = getMovementHistory(
                warehouseId, productId, startDate, endDate, 
                DomainStockMovementTypeEnum.INBOUND, 0, Integer.MAX_VALUE
        );
        
        if (inboundMovements.isEmpty()) {
            return new DomainQuantityValue(0, "pieces");
        }
        
        DomainQuantityValue total = inboundMovements.get(0).getQuantity().zero();
        for (DomainStockMovementEntity movement : inboundMovements) {
            total = total.add(movement.getQuantity());
        }
        
        return total;
    }
    
    /**
     * Calculates total outbound quantity for a period.
     * 
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date
     * @param endDate the end date
     * @return total outbound quantity
     */
    public DomainQuantityValue calculateTotalOutbound(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        List<DomainStockMovementEntity> outboundMovements = getMovementHistory(
                warehouseId, productId, startDate, endDate, 
                DomainStockMovementTypeEnum.OUTBOUND, 0, Integer.MAX_VALUE
        );
        
        if (outboundMovements.isEmpty()) {
            return new DomainQuantityValue(0, "pieces");
        }
        
        DomainQuantityValue total = outboundMovements.get(0).getQuantity().zero();
        for (DomainStockMovementEntity movement : outboundMovements) {
            total = total.add(movement.getQuantity());
        }
        
        return total;
    }
}