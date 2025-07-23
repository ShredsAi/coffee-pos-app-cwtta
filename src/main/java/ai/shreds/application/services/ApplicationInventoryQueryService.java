package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationInventoryQueryInputPort;
import ai.shreds.domain.ports.DomainInputPortInventoryQuery;
import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;
import ai.shreds.application.exceptions.ApplicationEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationInventoryQueryService implements ApplicationInventoryQueryInputPort {

    private final DomainInputPortInventoryQuery domainInventoryQueryPort;
    private final ApplicationInventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public SharedInventoryItemResponseDTO getItem(UUID warehouseId, UUID productId) {
        log.debug("Retrieving inventory item for warehouse: {} and product: {}", warehouseId, productId);
        
        try {
            // Convert productId to domain value object
            DomainProductIdValue domainProductId = new DomainProductIdValue(productId.toString());
            
            // Get inventory item from domain service
            DomainInventoryItemEntity inventoryItem = domainInventoryQueryPort.findByWarehouseAndProduct(
                warehouseId, domainProductId
            );
            
            if (inventoryItem == null) {
                log.warn("Inventory item not found for warehouse: {} and product: {}", warehouseId, productId);
                throw new ApplicationEntityNotFoundException(
                    "Inventory item not found for warehouse: " + warehouseId + " and product: " + productId,
                    "InventoryItem",
                    warehouseId + "-" + productId
                );
            }
            
            // Convert to response DTO
            SharedInventoryItemResponseDTO response = inventoryMapper.toResponseDTO(inventoryItem);
            
            log.debug("Successfully retrieved inventory item with ID: {}", inventoryItem.getId());
            return response;
            
        } catch (DomainEntityNotFoundException ex) {
            log.warn("Inventory item not found: {}", ex.getMessage());
            throw new ApplicationEntityNotFoundException(
                ex.getMessage(),
                ex.getEntityType(),
                ex.getEntityId()
            );
        } catch (Exception ex) {
            log.error("Error retrieving inventory item for warehouse: {} and product: {}", 
                     warehouseId, productId, ex);
            throw new RuntimeException("Failed to retrieve inventory item", ex);
        }
    }

    /**
     * Gets current stock levels for a warehouse-product combination
     * This is a convenience method that returns just the quantities
     */
    public java.util.Map<String, java.math.BigDecimal> getStockLevels(UUID warehouseId, UUID productId) {
        log.debug("Retrieving stock levels for warehouse: {} and product: {}", warehouseId, productId);
        
        try {
            // Convert productId to domain value object
            DomainProductIdValue domainProductId = new DomainProductIdValue(productId.toString());
            
            // Get stock levels from domain service
            java.util.Map<String, java.math.BigDecimal> stockLevels = domainInventoryQueryPort.getStockLevels(
                warehouseId, domainProductId
            );
            
            log.debug("Successfully retrieved stock levels for warehouse: {} and product: {}", 
                     warehouseId, productId);
            return stockLevels;
            
        } catch (Exception ex) {
            log.error("Error retrieving stock levels for warehouse: {} and product: {}", 
                     warehouseId, productId, ex);
            throw new RuntimeException("Failed to retrieve stock levels", ex);
        }
    }

    /**
     * Checks if an inventory item exists for the given warehouse and product
     */
    public boolean existsInventoryItem(UUID warehouseId, UUID productId) {
        log.debug("Checking existence of inventory item for warehouse: {} and product: {}", 
                 warehouseId, productId);
        
        try {
            DomainProductIdValue domainProductId = new DomainProductIdValue(productId.toString());
            DomainInventoryItemEntity item = domainInventoryQueryPort.findByWarehouseAndProduct(
                warehouseId, domainProductId
            );
            
            boolean exists = item != null;
            log.debug("Inventory item exists: {} for warehouse: {} and product: {}", 
                     exists, warehouseId, productId);
            return exists;
            
        } catch (Exception ex) {
            log.error("Error checking inventory item existence for warehouse: {} and product: {}", 
                     warehouseId, productId, ex);
            return false;
        }
    }
}