package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureBatchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for batch operations.
 * Provides comprehensive batch management queries supporting FIFO allocation.
 */
@Repository
public interface InfrastructureBatchSpringDataRepository extends JpaRepository<InfrastructureBatchJpaEntity, UUID> {
    
    /**
     * Finds the first batch by warehouse and product ordered by received date (FIFO).
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the oldest batch if found
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE b.warehouseId = :warehouseId AND b.productId = :productId AND b.quantity > 0 ORDER BY b.receivedAt ASC")
    Optional<InfrastructureBatchJpaEntity> findFirstByWarehouseIdAndProductIdOrderByReceivedAtAsc(@Param("warehouseId") UUID warehouseId, @Param("productId") String productId);
    
    /**
     * Checks if a batch exists by warehouse, product and batch number.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @return true if exists
     */
    boolean existsByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, String productId, String batchNumber);
    
    /**
     * Finds batches with available quantity for a warehouse and product.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param quantity minimum available quantity
     * @return list of batches with available quantity
     */
    List<InfrastructureBatchJpaEntity> findByWarehouseIdAndProductIdAndQuantityGreaterThan(UUID warehouseId, String productId, BigDecimal quantity);
    
    /**
     * Finds a batch by warehouse, product and batch number.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @return the batch if found
     */
    Optional<InfrastructureBatchJpaEntity> findByWarehouseIdAndProductIdAndBatchNumber(UUID warehouseId, String productId, String batchNumber);
    
    /**
     * Finds all batches for a warehouse and product ordered by FIFO.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of batches ordered by received date
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE b.warehouseId = :warehouseId AND b.productId = :productId AND b.quantity > 0 ORDER BY b.receivedAt ASC")
    List<InfrastructureBatchJpaEntity> findAvailableBatchesByFifoOrder(@Param("warehouseId") UUID warehouseId, @Param("productId") String productId);
    
    /**
     * Finds all batches for a warehouse.
     * @param warehouseId the warehouse ID
     * @return list of batches
     */
    List<InfrastructureBatchJpaEntity> findByWarehouseId(UUID warehouseId);
    
    /**
     * Finds all batches for a product.
     * @param productId the product ID
     * @return list of batches
     */
    List<InfrastructureBatchJpaEntity> findByProductId(String productId);
    
    /**
     * Finds batches by batch number.
     * @param batchNumber the batch number
     * @return list of batches with the batch number
     */
    List<InfrastructureBatchJpaEntity> findByBatchNumber(String batchNumber);
    
    /**
     * Finds expired batches.
     * @param warehouseId optional warehouse ID filter
     * @param currentDate current date for comparison
     * @return list of expired batches
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE " +
           "(:warehouseId IS NULL OR b.warehouseId = :warehouseId) AND " +
           "b.expirationDate < :currentDate")
    List<InfrastructureBatchJpaEntity> findExpiredBatches(@Param("warehouseId") UUID warehouseId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Finds batches expiring before a specific date.
     * @param warehouseId optional warehouse ID filter
     * @param expirationDate the expiration cutoff date
     * @return list of batches expiring before the date
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE " +
           "(:warehouseId IS NULL OR b.warehouseId = :warehouseId) AND " +
           "b.expirationDate < :expirationDate")
    List<InfrastructureBatchJpaEntity> findBatchesExpiringBefore(@Param("warehouseId") UUID warehouseId, @Param("expirationDate") LocalDate expirationDate);
    
    /**
     * Finds batches by supplier.
     * @param supplierId the supplier ID
     * @return list of batches from the supplier
     */
    List<InfrastructureBatchJpaEntity> findBySupplierId(UUID supplierId);
    
    /**
     * Finds empty batches (quantity = 0).
     * @param warehouseId optional warehouse ID filter
     * @return list of empty batches
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE " +
           "(:warehouseId IS NULL OR b.warehouseId = :warehouseId) AND " +
           "b.quantity = 0")
    List<InfrastructureBatchJpaEntity> findEmptyBatches(@Param("warehouseId") UUID warehouseId);
    
    /**
     * Locks a batch for concurrent operations.
     * @param id the batch ID
     * @return the locked batch
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE b.id = :id")
    Optional<InfrastructureBatchJpaEntity> lockById(@Param("id") UUID id);
    
    /**
     * Counts batches by warehouse.
     * @param warehouseId the warehouse ID
     * @return count of batches
     */
    long countByWarehouseId(UUID warehouseId);
    
    /**
     * Counts batches by product.
     * @param productId the product ID
     * @return count of batches
     */
    long countByProductId(String productId);
    
    /**
     * Finds batches by manufacturing date range.
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of batches manufactured in the date range
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE " +
           "b.manufacturingDate >= :startDate AND b.manufacturingDate <= :endDate")
    List<InfrastructureBatchJpaEntity> findByManufacturingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Finds batches for a warehouse ordered by expiration date.
     * @param warehouseId the warehouse ID
     * @return list of batches ordered by expiration date
     */
    List<InfrastructureBatchJpaEntity> findByWarehouseIdOrderByExpirationDateAsc(UUID warehouseId);
    
    /**
     * Finds batches with specific product and supplier.
     * @param productId the product ID
     * @param supplierId the supplier ID
     * @return list of batches for the product and supplier
     */
    List<InfrastructureBatchJpaEntity> findByProductIdAndSupplierId(String productId, UUID supplierId);
    
    /**
     * Finds batches received after a specific date.
     * @param receivedAfter the date to filter by
     * @return list of batches received after the date
     */
    @Query("SELECT b FROM InfrastructureBatchJpaEntity b WHERE b.receivedAt > :receivedAfter")
    List<InfrastructureBatchJpaEntity> findByReceivedAtAfter(@Param("receivedAfter") java.time.LocalDateTime receivedAfter);
}
