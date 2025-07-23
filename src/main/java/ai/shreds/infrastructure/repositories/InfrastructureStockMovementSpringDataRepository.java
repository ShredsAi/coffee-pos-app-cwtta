package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureStockMovementJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for stock movement operations.
 * Provides comprehensive querying capabilities for movement history and audit trails.
 */
@Repository
public interface InfrastructureStockMovementSpringDataRepository extends JpaRepository<InfrastructureStockMovementJpaEntity, UUID> {
    
    /**
     * Finds movements by warehouse, product, date range and optional movement type with pagination.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date
     * @param endDate the end date
     * @param movementType optional movement type filter
     * @param pageable pagination information
     * @return page of matching stock movements
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.warehouseId = :warehouseId AND m.productId = :productId " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "AND (:movementType IS NULL OR m.movementType = :movementType) " +
           "ORDER BY m.performedAt DESC")
    Page<InfrastructureStockMovementJpaEntity> findByFilters(
            @Param("warehouseId") UUID warehouseId,
            @Param("productId") String productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("movementType") String movementType,
            Pageable pageable);
    
    /**
     * Counts movements by warehouse, product, date range and optional movement type.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param startDate the start date
     * @param endDate the end date
     * @param movementType optional movement type filter
     * @return count of matching stock movements
     */
    @Query("SELECT COUNT(m) FROM InfrastructureStockMovementJpaEntity m WHERE m.warehouseId = :warehouseId AND m.productId = :productId " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "AND (:movementType IS NULL OR m.movementType = :movementType)")
    Long countByFilters(
            @Param("warehouseId") UUID warehouseId,
            @Param("productId") String productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("movementType") String movementType);
    
    /**
     * Finds all movements for a product in a warehouse ordered by performed date, descending.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of stock movements
     */
    List<InfrastructureStockMovementJpaEntity> findByWarehouseIdAndProductIdOrderByPerformedAtDesc(UUID warehouseId, String productId);
    
    /**
     * Finds movements by reference ID and type.
     * @param referenceId the reference ID
     * @param referenceType the reference type
     * @return list of movements with the reference
     */
    List<InfrastructureStockMovementJpaEntity> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
    
    /**
     * Finds movements for a specific batch.
     * @param batchId the batch ID
     * @return list of movements affecting the batch
     */
    List<InfrastructureStockMovementJpaEntity> findByBatchId(UUID batchId);
    
    /**
     * Finds movements performed by a specific user within a date range.
     * @param performedBy the user identifier
     * @param startDate the start date
     * @param endDate the end date
     * @return list of movements performed by the user
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.performedBy = :performedBy " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.performedAt DESC")
    List<InfrastructureStockMovementJpaEntity> findByPerformedByAndDateRange(
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds the most recent movement for a product in a warehouse.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the most recent movement
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.warehouseId = :warehouseId AND m.productId = :productId " +
           "ORDER BY m.performedAt DESC")
    Optional<InfrastructureStockMovementJpaEntity> findMostRecentByWarehouseIdAndProductId(
            @Param("warehouseId") UUID warehouseId,
            @Param("productId") String productId);
    
    /**
     * Finds movements by type within a date range.
     * @param movementType the movement type
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of matching movements
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.movementType = :movementType " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.performedAt DESC")
    Page<InfrastructureStockMovementJpaEntity> findByMovementTypeAndDateRange(
            @Param("movementType") String movementType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Finds movements with reason within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of movements with reasons
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.reason IS NOT NULL AND m.reason != '' " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.performedAt DESC")
    List<InfrastructureStockMovementJpaEntity> findWithReason(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds movements with cost information within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of movements with cost information
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.costPerUnit IS NOT NULL " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.performedAt DESC")
    List<InfrastructureStockMovementJpaEntity> findWithCostInformation(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Counts movements by warehouse.
     * @param warehouseId the warehouse ID
     * @return count of movements
     */
    long countByWarehouseId(UUID warehouseId);
    
    /**
     * Counts movements by product.
     * @param productId the product ID
     * @return count of movements
     */
    long countByProductId(String productId);
    
    /**
     * Finds movements by warehouse with pagination.
     * @param warehouseId the warehouse ID
     * @param pageable pagination information
     * @return page of movements
     */
    Page<InfrastructureStockMovementJpaEntity> findByWarehouseId(UUID warehouseId, Pageable pageable);
    
    /**
     * Finds movements by product with pagination.
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of movements
     */
    Page<InfrastructureStockMovementJpaEntity> findByProductId(String productId, Pageable pageable);
    
    /**
     * Finds movements by warehouse and movement type.
     * @param warehouseId the warehouse ID
     * @param movementType the movement type
     * @return list of movements
     */
    List<InfrastructureStockMovementJpaEntity> findByWarehouseIdAndMovementType(UUID warehouseId, String movementType);
    
    /**
     * Finds movements by product and movement type.
     * @param productId the product ID
     * @param movementType the movement type
     * @return list of movements
     */
    List<InfrastructureStockMovementJpaEntity> findByProductIdAndMovementType(String productId, String movementType);
    
    /**
     * Finds movements created after a specific date.
     * @param createdAfter the date to filter by
     * @return list of movements created after the date
     */
    List<InfrastructureStockMovementJpaEntity> findByCreatedAtAfter(LocalDateTime createdAfter);
    
    /**
     * Finds movements by warehouse and reference type.
     * @param warehouseId the warehouse ID
     * @param referenceType the reference type
     * @return list of movements
     */
    List<InfrastructureStockMovementJpaEntity> findByWarehouseIdAndReferenceType(UUID warehouseId, String referenceType);
    
    /**
     * Finds movements by product and date range.
     * @param productId the product ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of movements
     */
    @Query("SELECT m FROM InfrastructureStockMovementJpaEntity m WHERE m.productId = :productId " +
           "AND m.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.performedAt DESC")
    List<InfrastructureStockMovementJpaEntity> findByProductIdAndDateRange(
            @Param("productId") String productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
