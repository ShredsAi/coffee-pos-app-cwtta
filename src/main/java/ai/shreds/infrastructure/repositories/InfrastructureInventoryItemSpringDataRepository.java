package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureInventoryItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for inventory item operations.
 * Provides comprehensive inventory management queries and operations.
 */
@Repository
public interface InfrastructureInventoryItemSpringDataRepository extends JpaRepository<InfrastructureInventoryItemJpaEntity, UUID> {
    
    /**
     * Finds an inventory item by warehouse and product ids.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the inventory item if found
     */
    Optional<InfrastructureInventoryItemJpaEntity> findByWarehouseIdAndProductId(UUID warehouseId, String productId);
    
    /**
     * Locks an inventory item for update to ensure consistent changes.
     * @param id the inventory item ID
     * @return the locked inventory item
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE i.id = :id")
    Optional<InfrastructureInventoryItemJpaEntity> lockById(@Param("id") UUID id);
    
    /**
     * Locks an inventory item by warehouse and product for update.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return the locked inventory item
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE i.warehouseId = :warehouseId AND i.productId = :productId")
    Optional<InfrastructureInventoryItemJpaEntity> lockByWarehouseIdAndProductId(@Param("warehouseId") UUID warehouseId, @Param("productId") String productId);
    
    /**
     * Finds all inventory items for a warehouse.
     * @param warehouseId the warehouse ID
     * @return list of inventory items
     */
    List<InfrastructureInventoryItemJpaEntity> findByWarehouseId(UUID warehouseId);
    
    /**
     * Finds all inventory items for a specific product across warehouses.
     * @param productId the product ID
     * @return list of inventory items for the product
     */
    List<InfrastructureInventoryItemJpaEntity> findByProductId(String productId);
    
    /**
     * Finds inventory items where available quantity is below safety stock level.
     * @param warehouseId optional warehouse ID filter
     * @return list of low stock inventory items
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "i.availableQty <= i.safetyStockLevel")
    List<InfrastructureInventoryItemJpaEntity> findBelowSafetyStockLevel(@Param("warehouseId") UUID warehouseId);
    
    /**
     * Finds inventory items where available quantity is below reorder point.
     * @param warehouseId optional warehouse ID filter
     * @return list of inventory items below reorder point
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "i.availableQty <= i.reorderPoint")
    List<InfrastructureInventoryItemJpaEntity> findBelowReorderPoint(@Param("warehouseId") UUID warehouseId);
    
    /**
     * Finds inventory items with zero available quantity (out of stock).
     * @param warehouseId optional warehouse ID filter
     * @return list of out-of-stock inventory items
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "i.availableQty = 0")
    List<InfrastructureInventoryItemJpaEntity> findOutOfStock(@Param("warehouseId") UUID warehouseId);
    
    /**
     * Finds inventory items with zero total quantity (no physical inventory).
     * @param warehouseId optional warehouse ID filter
     * @return list of inventory items with no physical stock
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "i.totalQty = 0")
    List<InfrastructureInventoryItemJpaEntity> findWithNoStock(@Param("warehouseId") UUID warehouseId);
    
    /**
     * Finds inventory items with movements after the specified date.
     * @param warehouseId optional warehouse ID filter
     * @param sinceDate the date to check movements after
     * @return list of inventory items with recent movements
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "i.lastMovementAt > :sinceDate")
    List<InfrastructureInventoryItemJpaEntity> findWithMovementsAfter(@Param("warehouseId") UUID warehouseId, @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Finds inventory items with no movements after the specified date (stagnant inventory).
     * @param warehouseId optional warehouse ID filter
     * @param sinceDate the date to check for no movements after
     * @return list of inventory items with no recent movements
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "(:warehouseId IS NULL OR i.warehouseId = :warehouseId) AND " +
           "(i.lastMovementAt IS NULL OR i.lastMovementAt <= :sinceDate)")
    List<InfrastructureInventoryItemJpaEntity> findWithNoMovementsAfter(@Param("warehouseId") UUID warehouseId, @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Counts inventory items for a warehouse.
     * @param warehouseId the warehouse ID
     * @return count of inventory items
     */
    long countByWarehouseId(UUID warehouseId);
    
    /**
     * Checks if an inventory item exists for warehouse and product.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return true if inventory item exists
     */
    boolean existsByWarehouseIdAndProductId(UUID warehouseId, String productId);
    
    /**
     * Updates thresholds for an inventory item.
     * @param id the inventory item ID
     * @param safetyStockLevel new safety stock level
     * @param reorderPoint new reorder point
     */
    @Modifying
    @Query("UPDATE InfrastructureInventoryItemJpaEntity i SET " +
           "i.safetyStockLevel = :safetyStockLevel, " +
           "i.reorderPoint = :reorderPoint " +
           "WHERE i.id = :id")
    void updateThresholds(@Param("id") UUID id, 
                         @Param("safetyStockLevel") BigDecimal safetyStockLevel, 
                         @Param("reorderPoint") BigDecimal reorderPoint);
    
    /**
     * Finds inventory items by multiple product IDs.
     * @param productIds list of product IDs
     * @return list of inventory items
     */
    List<InfrastructureInventoryItemJpaEntity> findByProductIdIn(List<String> productIds);
    
    /**
     * Finds inventory items with total quantity greater than specified amount.
     * @param warehouseId the warehouse ID
     * @param minQuantity minimum quantity threshold
     * @return list of inventory items above threshold
     */
    @Query("SELECT i FROM InfrastructureInventoryItemJpaEntity i WHERE " +
           "i.warehouseId = :warehouseId AND i.totalQty > :minQuantity")
    List<InfrastructureInventoryItemJpaEntity> findByWarehouseIdAndTotalQtyGreaterThan(
            @Param("warehouseId") UUID warehouseId, 
            @Param("minQuantity") BigDecimal minQuantity);
    
    /**
     * Finds all inventory items ordered by last movement date.
     * @param warehouseId the warehouse ID
     * @return list of inventory items ordered by last movement date
     */
    List<InfrastructureInventoryItemJpaEntity> findByWarehouseIdOrderByLastMovementAtDesc(UUID warehouseId);
}
