package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.ports.DomainOutputPortInventoryItemRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.infrastructure.repositories.entities.InfrastructureInventoryItemJpaEntity;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the inventory item repository port.
 * Handles inventory quantity management with optimistic locking.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureInventoryItemRepositoryImpl implements DomainOutputPortInventoryItemRepository {

    private final InfrastructureInventoryItemSpringDataRepository springDataRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainInventoryItemEntity save(DomainInventoryItemEntity item) {
        try {
            log.debug("Saving inventory item with ID: {}", item.getId());
            InfrastructureInventoryItemJpaEntity jpaEntity = entityMapper.toJpaInventoryItem(item);
            InfrastructureInventoryItemJpaEntity saved = springDataRepository.save(jpaEntity);
            DomainInventoryItemEntity result = entityMapper.toDomainInventoryItem(saved);
            log.debug("Successfully saved inventory item with ID: {}", result.getId());
            return result;
        } catch (OptimisticLockingFailureException ex) {
            log.error("Optimistic locking failure when saving inventory item with ID: {}", item.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Optimistic locking failure when saving inventory item", 
                "DomainInventoryItemEntity", 
                "save", 
                ex
            );
        } catch (DataAccessException ex) {
            log.error("Failed to save inventory item with ID: {}", item.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to save inventory item", 
                "DomainInventoryItemEntity", 
                "save", 
                ex
            );
        }
    }

    @Override
    public DomainInventoryItemEntity findByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Finding inventory item by warehouseId: {} and productId: {}", warehouseId, productId.getValue());
            return springDataRepository.findByWarehouseIdAndProductId(warehouseId, productId.getValue())
                    .map(entityMapper::toDomainInventoryItem)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory item by warehouseId: {} and productId: {}", warehouseId, productId.getValue(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory item by warehouse and product", 
                "DomainInventoryItemEntity", 
                "findByWarehouseIdAndProductId", 
                ex
            );
        }
    }

    @Override
    public DomainInventoryItemEntity lockById(UUID id) {
        try {
            log.debug("Locking inventory item by ID: {}", id);
            return springDataRepository.lockById(id)
                    .map(entityMapper::toDomainInventoryItem)
                    .orElseThrow(() -> new DomainEntityNotFoundException(
                        "Inventory item not found for locking: " + id,
                        "DomainInventoryItemEntity",
                        id.toString()
                    ));
        } catch (DataAccessException ex) {
            log.error("Failed to lock inventory item by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to lock inventory item by ID",
                "DomainInventoryItemEntity",
                "lockById",
                ex
            );
        }
    }

    @Override
    public DomainInventoryItemEntity lockByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Locking inventory item by warehouseId: {} and productId: {}", warehouseId, productId.getValue());
            return springDataRepository.lockByWarehouseIdAndProductId(warehouseId, productId.getValue())
                    .map(entityMapper::toDomainInventoryItem)
                    .orElseThrow(() -> new DomainEntityNotFoundException(
                        String.format("Inventory item not found for locking: warehouseId=%s, productId=%s", 
                                     warehouseId, productId.getValue()),
                        "DomainInventoryItemEntity",
                        warehouseId + "-" + productId.getValue()
                    ));
        } catch (DataAccessException ex) {
            log.error("Failed to lock inventory item by warehouseId: {} and productId: {}", warehouseId, productId.getValue(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to lock inventory item by warehouse and product",
                "DomainInventoryItemEntity",
                "lockByWarehouseIdAndProductId",
                ex
            );
        }
    }

    @Override
    public DomainInventoryItemEntity findById(UUID id) {
        try {
            log.debug("Finding inventory item by ID: {}", id);
            return springDataRepository.findById(id)
                    .map(entityMapper::toDomainInventoryItem)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory item by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory item by ID",
                "DomainInventoryItemEntity",
                "findById",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findByWarehouseId(UUID warehouseId) {
        try {
            log.debug("Finding inventory items by warehouseId: {}", warehouseId);
            return springDataRepository.findByWarehouseId(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items by warehouseId: {}", warehouseId, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items by warehouse",
                "DomainInventoryItemEntity",
                "findByWarehouseId",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findByProductId(DomainProductIdValue productId) {
        try {
            log.debug("Finding inventory items by productId: {}", productId.getValue());
            return springDataRepository.findByProductId(productId.getValue())
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items by productId: {}", productId.getValue(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items by product",
                "DomainInventoryItemEntity",
                "findByProductId",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findBelowSafetyStockLevel(UUID warehouseId) {
        try {
            log.debug("Finding inventory items below safety stock level for warehouseId: {}", warehouseId);
            return springDataRepository.findBelowSafetyStockLevel(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items below safety stock level", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items below safety stock level",
                "DomainInventoryItemEntity",
                "findBelowSafetyStockLevel",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findBelowReorderPoint(UUID warehouseId) {
        try {
            log.debug("Finding inventory items below reorder point for warehouseId: {}", warehouseId);
            return springDataRepository.findBelowReorderPoint(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items below reorder point", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items below reorder point",
                "DomainInventoryItemEntity",
                "findBelowReorderPoint",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findOutOfStock(UUID warehouseId) {
        try {
            log.debug("Finding out-of-stock inventory items for warehouseId: {}", warehouseId);
            return springDataRepository.findOutOfStock(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find out-of-stock inventory items", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find out-of-stock inventory items",
                "DomainInventoryItemEntity",
                "findOutOfStock",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findWithNoStock(UUID warehouseId) {
        try {
            log.debug("Finding inventory items with no stock for warehouseId: {}", warehouseId);
            return springDataRepository.findWithNoStock(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items with no stock", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items with no stock",
                "DomainInventoryItemEntity",
                "findWithNoStock",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findWithMovementsAfter(UUID warehouseId, LocalDateTime sinceDate) {
        try {
            log.debug("Finding inventory items with movements after: {} for warehouseId: {}", sinceDate, warehouseId);
            return springDataRepository.findWithMovementsAfter(warehouseId, sinceDate)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items with movements after date", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items with movements after date",
                "DomainInventoryItemEntity",
                "findWithMovementsAfter",
                ex
            );
        }
    }

    @Override
    public List<DomainInventoryItemEntity> findWithNoMovementsAfter(UUID warehouseId, LocalDateTime sinceDate) {
        try {
            log.debug("Finding inventory items with no movements after: {} for warehouseId: {}", sinceDate, warehouseId);
            return springDataRepository.findWithNoMovementsAfter(warehouseId, sinceDate)
                    .stream()
                    .map(entityMapper::toDomainInventoryItem)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find inventory items with no movements after date", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find inventory items with no movements after date",
                "DomainInventoryItemEntity",
                "findWithNoMovementsAfter",
                ex
            );
        }
    }

    @Override
    public long countByWarehouse(UUID warehouseId) {
        try {
            log.debug("Counting inventory items by warehouseId: {}", warehouseId);
            return springDataRepository.countByWarehouseId(warehouseId);
        } catch (DataAccessException ex) {
            log.error("Failed to count inventory items by warehouse", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count inventory items by warehouse",
                "DomainInventoryItemEntity",
                "countByWarehouse",
                ex
            );
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            log.debug("Deleting inventory item by ID: {}", id);
            springDataRepository.deleteById(id);
            log.debug("Successfully deleted inventory item with ID: {}", id);
        } catch (DataAccessException ex) {
            log.error("Failed to delete inventory item by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to delete inventory item by ID",
                "DomainInventoryItemEntity",
                "deleteById",
                ex
            );
        }
    }

    @Override
    public boolean existsByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Checking existence of inventory item by warehouseId: {} and productId: {}", warehouseId, productId.getValue());
            return springDataRepository.existsByWarehouseIdAndProductId(warehouseId, productId.getValue());
        } catch (DataAccessException ex) {
            log.error("Failed to check inventory item existence", ex);
            throw new InfrastructurePersistenceException(
                "Failed to check inventory item existence",
                "DomainInventoryItemEntity",
                "existsByWarehouseIdAndProductId",
                ex
            );
        }
    }

    @Override
    @Transactional
    public DomainInventoryItemEntity updateThresholds(UUID id, DomainQuantityValue safetyStockLevel, DomainQuantityValue reorderPoint) {
        try {
            log.debug("Updating thresholds for inventory item with ID: {}", id);
            springDataRepository.updateThresholds(id, safetyStockLevel.getValue(), reorderPoint.getValue());
            return findById(id);
        } catch (DataAccessException ex) {
            log.error("Failed to update thresholds for inventory item with ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to update inventory item thresholds",
                "DomainInventoryItemEntity",
                "updateThresholds",
                ex
            );
        }
    }
}
