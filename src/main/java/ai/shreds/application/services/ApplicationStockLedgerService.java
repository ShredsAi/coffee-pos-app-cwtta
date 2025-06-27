package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationStockLedgerInputPort;
import ai.shreds.domain.ports.DomainInputPortStockLedger;
import ai.shreds.domain.ports.DomainInputPortInventoryQuery;
import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.shared.dtos.SharedStockLedgerResponseDTO;
import ai.shreds.shared.dtos.SharedStockLedgerQueryParams;
import ai.shreds.shared.dtos.SharedStockMovementResponseDTO;
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;
import ai.shreds.shared.dtos.SharedPaginationDTO;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;
import ai.shreds.application.exceptions.ApplicationEntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStockLedgerService implements ApplicationStockLedgerInputPort {

    private final DomainInputPortStockLedger domainStockLedgerPort;
    private final DomainInputPortInventoryQuery domainInventoryQueryPort;
    private final ApplicationStockMovementMapper stockMovementMapper;
    private final ApplicationInventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public SharedStockLedgerResponseDTO getStockLedger(
            UUID warehouseId, 
            UUID productId, 
            SharedStockLedgerQueryParams queryParams) {
        
        log.debug("Retrieving stock ledger for warehouse: {}, product: {}, params: {}", 
                warehouseId, productId, queryParams);
        
        try {
            // Convert productId to domain value object
            DomainProductIdValue domainProductId = new DomainProductIdValue(productId.toString());
            
            // Set default values if not provided
            int page = queryParams.getPage() != null ? queryParams.getPage() : 0;
            int size = queryParams.getSize() != null ? queryParams.getSize() : 20;
            
            // Convert movement type if present
            DomainStockMovementTypeEnum movementType = null;
            if (queryParams.getMovementType() != null) {
                movementType = convertToDomainMovementType(queryParams.getMovementType());
            }
            
            // Get current inventory quantities
            DomainInventoryItemEntity inventoryItem = null;
            SharedInventoryQuantitiesDTO currentQuantities = null;
            
            try {
                inventoryItem = domainInventoryQueryPort.findByWarehouseAndProduct(warehouseId, domainProductId);
                
                if (inventoryItem != null) {
                    currentQuantities = ApplicationInventoryMapper.toQuantitiesDTO(inventoryItem);
                } else {
                    // Create empty quantities if inventory item doesn't exist yet
                    currentQuantities = createEmptyQuantities();
                }
                
            } catch (Exception ex) {
                log.warn("Could not retrieve current inventory quantities: {}", ex.getMessage());
                currentQuantities = createEmptyQuantities();
            }
            
            // Get movement history with pagination
            List<DomainStockMovementEntity> movements = domainStockLedgerPort.getMovementHistory(
                warehouseId,
                domainProductId,
                queryParams.getStartDate(),
                queryParams.getEndDate(),
                movementType,
                page,
                size
            );
            
            // Note: countMovementHistory doesn't exist in domain port based on UML
            // We'll estimate total count from the returned list size for pagination
            // This is a limitation - proper implementation would require the domain port to support count
            long totalElements = movements.size();
            boolean hasNext = movements.size() == size; // Simple estimation - may have more if we got full page
            
            // Map movements to DTOs
            List<SharedStockMovementResponseDTO> movementDtos = mapMovementsToDTO(movements);
            
            // Create pagination info with limited information
            int totalPages = hasNext ? page + 2 : page + 1; // Simple estimation
            SharedPaginationDTO pagination = new SharedPaginationDTO();
            pagination.setPage(page);
            pagination.setSize(size);
            pagination.setTotalElements(totalElements); // This won't be accurate without count method
            pagination.setTotalPages(totalPages); // This won't be accurate either
            pagination.setHasNext(hasNext);
            pagination.setHasPrevious(page > 0);
            
            // Create response
            SharedStockLedgerResponseDTO response = new SharedStockLedgerResponseDTO();
            response.setWarehouseId(warehouseId);
            response.setProductId(productId);
            response.setCurrentQuantities(currentQuantities);
            response.setMovements(movementDtos);
            response.setPagination(pagination);
            
            log.debug("Successfully retrieved stock ledger with {} movements for warehouse: {}, product: {}", 
                     movementDtos.size(), warehouseId, productId);
            return response;
            
        } catch (DomainEntityNotFoundException ex) {
            log.warn("Entity not found retrieving stock ledger: {}", ex.getMessage());
            throw new ApplicationEntityNotFoundException(
                ex.getMessage(),
                ex.getEntityType(),
                ex.getEntityId()
            );
        } catch (Exception ex) {
            log.error("Error retrieving stock ledger for warehouse: {} and product: {}", 
                     warehouseId, productId, ex);
            throw new RuntimeException("Failed to retrieve stock ledger", ex);
        }
    }

    private List<SharedStockMovementResponseDTO> mapMovementsToDTO(List<DomainStockMovementEntity> movements) {
        return movements.stream()
                .map(stockMovementMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    private SharedInventoryQuantitiesDTO createEmptyQuantities() {
        SharedInventoryQuantitiesDTO quantities = new SharedInventoryQuantitiesDTO();
        quantities.setAvailableQty(java.math.BigDecimal.ZERO);
        quantities.setReservedQty(java.math.BigDecimal.ZERO);
        quantities.setAllocatedQty(java.math.BigDecimal.ZERO);
        quantities.setTotalQty(java.math.BigDecimal.ZERO);
        quantities.setQtyUnit("pieces"); // Default unit
        return quantities;
    }
    
    private DomainStockMovementTypeEnum convertToDomainMovementType(SharedStockMovementTypeEnum sharedType) {
        if (sharedType == null) return null;
        
        switch (sharedType) {
            case INBOUND: return DomainStockMovementTypeEnum.INBOUND;
            case OUTBOUND: return DomainStockMovementTypeEnum.OUTBOUND;
            case TRANSFER: return DomainStockMovementTypeEnum.TRANSFER;
            case ADJUSTMENT: return DomainStockMovementTypeEnum.ADJUSTMENT;
            default: throw new IllegalArgumentException("Unknown movement type: " + sharedType);
        }
    }
}
