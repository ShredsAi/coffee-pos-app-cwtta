package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.ports.DomainOutputPortBatchRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.infrastructure.repositories.entities.InfrastructureBatchJpaEntity;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the batch repository port.
 * Handles FIFO batch operations and batch quantity management.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureBatchRepositoryImpl implements DomainOutputPortBatchRepository {

    private final InfrastructureBatchSpringDataRepository springDataRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainBatchEntity save(DomainBatchEntity batch) {
        try {
            log.debug("Saving batch with ID: {}", batch.getId());
            InfrastructureBatchJpaEntity jpaEntity = entityMapper.toJpaBatch(batch);
            InfrastructureBatchJpaEntity saved = springDataRepository.save(jpaEntity);
            DomainBatchEntity result = entityMapper.toDomainBatch(saved);
            log.debug("Successfully saved batch with ID: {}", result.getId());
            return result;
        } catch (DataAccessException ex) {
            log.error("Failed to save batch with ID: {}", batch.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to save batch", 
                "DomainBatchEntity", 
                "save", 
                ex
            );
        }
    }

    @Override
    public DomainBatchEntity findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Finding first batch by FIFO for warehouseId: {} and productId: {}", warehouseId, productId.getValue());
            return springDataRepository.findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc(warehouseId, productId.getValue())
                    .map(entityMapper::toDomainBatch)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find first batch by FIFO", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find first batch by FIFO",
                "DomainBatchEntity",
                "findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc",
                ex
            );
        }
    }

    @Override
    public boolean existsByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, DomainProductIdValue productId, String batchNumber) {
        try {
            log.debug("Checking existence of batch: {} for warehouseId: {} and productId: {}", batchNumber, warehouseId, productId.getValue());
            return springDataRepository.existsByWarehouseIdAndProductIdAndBatchNumber(warehouseId, productId.getValue(), batchNumber);
        } catch (DataAccessException ex) {
            log.error("Failed to check batch existence", ex);
            throw new InfrastructurePersistenceException(
                "Failed to check batch existence",
                "DomainBatchEntity",
                "existsByWarehouseIdAndProductIdAndBatchNumber",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findByWarehouseIdAndProductIdWithAvailableQuantity(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Finding available batches for warehouseId: {} and productId: {}", warehouseId, productId.getValue());
            return springDataRepository.findAvailableBatchesByFifoOrder(warehouseId, productId.getValue())
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find available batches", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find available batches",
                "DomainBatchEntity",
                "findByWarehouseIdAndProductIdWithAvailableQuantity",
                ex
            );
        }
    }

    @Override
    public DomainBatchEntity findByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, DomainProductIdValue productId, String batchNumber) {
        try {
            log.debug("Finding batch by batchNumber: {} for warehouseId: {} and productId: {}", batchNumber, warehouseId, productId.getValue());
            return springDataRepository.findByWarehouseIdAndProductIdAndBatchNumber(warehouseId, productId.getValue(), batchNumber)
                    .map(entityMapper::toDomainBatch)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find batch by batch number", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batch by batch number",
                "DomainBatchEntity",
                "findByWarehouseIdAndProductIdAndBatchNumber",
                ex
            );
        }
    }

    @Override
    public DomainBatchEntity findById(UUID id) {
        try {
            log.debug("Finding batch by ID: {}", id);
            return springDataRepository.findById(id)
                    .map(entityMapper::toDomainBatch)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find batch by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batch by ID",
                "DomainBatchEntity",
                "findById",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findByWarehouseId(UUID warehouseId) {
        try {
            log.debug("Finding batches by warehouseId: {}", warehouseId);
            return springDataRepository.findByWarehouseId(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches by warehouse", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches by warehouse",
                "DomainBatchEntity",
                "findByWarehouseId",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findByProductId(DomainProductIdValue productId) {
        try {
            log.debug("Finding batches by productId: {}", productId.getValue());
            return springDataRepository.findByProductId(productId.getValue())
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches by product", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches by product",
                "DomainBatchEntity",
                "findByProductId",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findByBatchNumber(String batchNumber) {
        try {
            log.debug("Finding batches by batchNumber: {}", batchNumber);
            return springDataRepository.findByBatchNumber(batchNumber)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches by batch number", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches by batch number",
                "DomainBatchEntity",
                "findByBatchNumber",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findExpiredBatches(UUID warehouseId) {
        try {
            log.debug("Finding expired batches for warehouseId: {}", warehouseId);
            return springDataRepository.findExpiredBatches(warehouseId, LocalDate.now())
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find expired batches", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find expired batches",
                "DomainBatchEntity",
                "findExpiredBatches",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findBatchesExpiringBefore(UUID warehouseId, LocalDate expirationDate) {
        try {
            log.debug("Finding batches expiring before: {} for warehouseId: {}", expirationDate, warehouseId);
            return springDataRepository.findBatchesExpiringBefore(warehouseId, expirationDate)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches expiring before date", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches expiring before date",
                "DomainBatchEntity",
                "findBatchesExpiringBefore",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findBySupplier(UUID supplierId) {
        try {
            log.debug("Finding batches by supplierId: {}", supplierId);
            return springDataRepository.findBySupplierId(supplierId)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches by supplier", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches by supplier",
                "DomainBatchEntity",
                "findBySupplier",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findEmptyBatches(UUID warehouseId) {
        try {
            log.debug("Finding empty batches for warehouseId: {}", warehouseId);
            return springDataRepository.findEmptyBatches(warehouseId)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find empty batches", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find empty batches",
                "DomainBatchEntity",
                "findEmptyBatches",
                ex
            );
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            log.debug("Deleting batch by ID: {}", id);
            springDataRepository.deleteById(id);
            log.debug("Successfully deleted batch with ID: {}", id);
        } catch (DataAccessException ex) {
            log.error("Failed to delete batch by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to delete batch by ID",
                "DomainBatchEntity",
                "deleteById",
                ex
            );
        }
    }

    @Override
    public DomainBatchEntity lockById(UUID id) {
        try {
            log.debug("Locking batch by ID: {}", id);
            return springDataRepository.lockById(id)
                    .map(entityMapper::toDomainBatch)
                    .orElseThrow(() -> new DomainEntityNotFoundException(
                        "Batch not found for locking: " + id,
                        "DomainBatchEntity",
                        id.toString()
                    ));
        } catch (DataAccessException ex) {
            log.error("Failed to lock batch by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to lock batch by ID",
                "DomainBatchEntity",
                "lockById",
                ex
            );
        }
    }

    @Override
    public long countByWarehouse(UUID warehouseId) {
        try {
            log.debug("Counting batches by warehouseId: {}", warehouseId);
            return springDataRepository.countByWarehouseId(warehouseId);
        } catch (DataAccessException ex) {
            log.error("Failed to count batches by warehouse", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count batches by warehouse",
                "DomainBatchEntity",
                "countByWarehouse",
                ex
            );
        }
    }

    @Override
    public long countByProduct(DomainProductIdValue productId) {
        try {
            log.debug("Counting batches by productId: {}", productId.getValue());
            return springDataRepository.countByProductId(productId.getValue());
        } catch (DataAccessException ex) {
            log.error("Failed to count batches by product", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count batches by product",
                "DomainBatchEntity",
                "countByProduct",
                ex
            );
        }
    }

    @Override
    public List<DomainBatchEntity> findByManufacturingDateBetween(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Finding batches by manufacturing date between: {} and {}", startDate, endDate);
            return springDataRepository.findByManufacturingDateBetween(startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainBatch)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find batches by manufacturing date range", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find batches by manufacturing date range",
                "DomainBatchEntity",
                "findByManufacturingDateBetween",
                ex
            );
        }
    }
}
