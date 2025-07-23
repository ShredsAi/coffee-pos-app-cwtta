package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.ports.DomainOutputPortStockMovementRepository;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.infrastructure.repositories.entities.InfrastructureStockMovementJpaEntity;
import ai.shreds.infrastructure.utilities.InfrastructureSQLQueryBuilder;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the stock movement repository port.
 * Handles movement history storage and retrieval with pagination support.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureStockMovementRepositoryImpl implements DomainOutputPortStockMovementRepository {

    private final InfrastructureStockMovementSpringDataRepository springDataRepository;
    private final InfrastructureEntityMapper entityMapper;
    private final InfrastructureSQLQueryBuilder sqlQueryBuilder;

    @Override
    public DomainStockMovementEntity save(DomainStockMovementEntity movement) {
        try {
            log.debug("Saving stock movement with ID: {}", movement.getId());
            InfrastructureStockMovementJpaEntity jpaEntity = entityMapper.toJpaStockMovement(movement);
            InfrastructureStockMovementJpaEntity saved = springDataRepository.save(jpaEntity);
            DomainStockMovementEntity result = entityMapper.toDomainStockMovement(saved);
            log.debug("Successfully saved stock movement with ID: {}", result.getId());
            return result;
        } catch (DataAccessException ex) {
            log.error("Failed to save stock movement with ID: {}", movement.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to save stock movement",
                "DomainStockMovementEntity",
                "save",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByWarehouseIdAndProductIdAndDateRange(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType,
            int page,
            int size,
            String sortDirection) {
        try {
            log.debug("Finding movements for warehouse: {}, product: {}", warehouseId, productId.getValue());
            
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "performedAt"));
            
            String movementTypeStr = movementType != null ? movementType.name() : null;
            
            Page<InfrastructureStockMovementJpaEntity> pageResult = springDataRepository.findByFilters(
                    warehouseId,
                    productId.getValue(),
                    startDate,
                    endDate,
                    movementTypeStr,
                    pageable
            );
            
            return pageResult.getContent().stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by criteria", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by criteria",
                "DomainStockMovementEntity",
                "findByWarehouseIdAndProductIdAndDateRange",
                ex
            );
        }
    }

    @Override
    public long countByWarehouseIdAndProductIdAndDateRange(
            UUID warehouseId,
            DomainProductIdValue productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            DomainStockMovementTypeEnum movementType) {
        try {
            log.debug("Counting movements for warehouse: {}, product: {}", warehouseId, productId.getValue());
            
            String movementTypeStr = movementType != null ? movementType.name() : null;
            
            return springDataRepository.countByFilters(
                    warehouseId,
                    productId.getValue(),
                    startDate,
                    endDate,
                    movementTypeStr
            );
        } catch (DataAccessException ex) {
            log.error("Failed to count movements by criteria", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count movements by criteria",
                "DomainStockMovementEntity",
                "countByWarehouseIdAndProductIdAndDateRange",
                ex
            );
        }
    }

    @Override
    public DomainStockMovementEntity findById(UUID id) {
        try {
            log.debug("Finding stock movement by ID: {}", id);
            return springDataRepository.findById(id)
                    .map(entityMapper::toDomainStockMovement)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find movement by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movement by ID",
                "DomainStockMovementEntity",
                "findById",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByReferenceIdAndType(String referenceId, String referenceType) {
        try {
            log.debug("Finding movements by reference: {} - {}", referenceId, referenceType);
            return springDataRepository.findByReferenceIdAndReferenceType(referenceId, referenceType)
                    .stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by reference", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by reference",
                "DomainStockMovementEntity",
                "findByReferenceIdAndType",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByBatchId(UUID batchId) {
        try {
            log.debug("Finding movements by batch ID: {}", batchId);
            return springDataRepository.findByBatchId(batchId)
                    .stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by batch ID", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by batch ID",
                "DomainStockMovementEntity",
                "findByBatchId",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByPerformedBy(String performedBy, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Finding movements by performer: {} between {} and {}", performedBy, startDate, endDate);
            return springDataRepository.findByPerformedByAndDateRange(performedBy, startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by performer", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by performer",
                "DomainStockMovementEntity",
                "findByPerformedBy",
                ex
            );
        }
    }

    @Override
    public DomainStockMovementEntity findMostRecentByWarehouseIdAndProductId(UUID warehouseId, DomainProductIdValue productId) {
        try {
            log.debug("Finding most recent movement for warehouse: {}, product: {}", warehouseId, productId.getValue());
            return springDataRepository.findMostRecentByWarehouseIdAndProductId(warehouseId, productId.getValue())
                    .map(entityMapper::toDomainStockMovement)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find most recent movement", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find most recent movement",
                "DomainStockMovementEntity",
                "findMostRecentByWarehouseIdAndProductId",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByMovementTypeAndDateRange(
            DomainStockMovementTypeEnum movementType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size) {
        try {
            log.debug("Finding movements by type: {} between {} and {}", movementType, startDate, endDate);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
            
            Page<InfrastructureStockMovementJpaEntity> pageResult = springDataRepository.findByMovementTypeAndDateRange(
                    movementType.name(),
                    startDate,
                    endDate,
                    pageable
            );
            
            return pageResult.getContent().stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by type and date range", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by type and date range",
                "DomainStockMovementEntity",
                "findByMovementTypeAndDateRange",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findWithReason(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Finding movements with reason between {} and {}", startDate, endDate);
            return springDataRepository.findWithReason(startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements with reason", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements with reason",
                "DomainStockMovementEntity",
                "findWithReason",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findWithCostInformation(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Finding movements with cost information between {} and {}", startDate, endDate);
            return springDataRepository.findWithCostInformation(startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements with cost information", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements with cost information",
                "DomainStockMovementEntity",
                "findWithCostInformation",
                ex
            );
        }
    }

    @Override
    public long countByWarehouse(UUID warehouseId) {
        try {
            log.debug("Counting movements by warehouse: {}", warehouseId);
            return springDataRepository.countByWarehouseId(warehouseId);
        } catch (DataAccessException ex) {
            log.error("Failed to count movements by warehouse", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count movements by warehouse",
                "DomainStockMovementEntity",
                "countByWarehouse",
                ex
            );
        }
    }

    @Override
    public long countByProduct(DomainProductIdValue productId) {
        try {
            log.debug("Counting movements by product: {}", productId.getValue());
            return springDataRepository.countByProductId(productId.getValue());
        } catch (DataAccessException ex) {
            log.error("Failed to count movements by product", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count movements by product",
                "DomainStockMovementEntity",
                "countByProduct",
                ex
            );
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            log.debug("Deleting movement by ID: {}", id);
            springDataRepository.deleteById(id);
            log.debug("Successfully deleted movement with ID: {}", id);
        } catch (DataAccessException ex) {
            log.error("Failed to delete movement by ID: {}", id, ex);
            throw new InfrastructurePersistenceException(
                "Failed to delete movement by ID",
                "DomainStockMovementEntity",
                "deleteById",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByWarehouseId(UUID warehouseId, int page, int size) {
        try {
            log.debug("Finding movements by warehouse: {} (page: {}, size: {})", warehouseId, page, size);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
            
            Page<InfrastructureStockMovementJpaEntity> pageResult = springDataRepository.findByWarehouseId(warehouseId, pageable);
            
            return pageResult.getContent().stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by warehouse", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by warehouse",
                "DomainStockMovementEntity",
                "findByWarehouseId",
                ex
            );
        }
    }

    @Override
    public List<DomainStockMovementEntity> findByProductId(DomainProductIdValue productId, int page, int size) {
        try {
            log.debug("Finding movements by product: {} (page: {}, size: {})", productId.getValue(), page, size);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
            
            Page<InfrastructureStockMovementJpaEntity> pageResult = springDataRepository.findByProductId(productId.getValue(), pageable);
            
            return pageResult.getContent().stream()
                    .map(entityMapper::toDomainStockMovement)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find movements by product", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find movements by product",
                "DomainStockMovementEntity",
                "findByProductId",
                ex
            );
        }
    }
}
