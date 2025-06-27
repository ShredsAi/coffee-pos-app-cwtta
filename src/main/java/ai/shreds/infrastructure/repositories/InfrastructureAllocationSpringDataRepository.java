package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureAllocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for allocation operations.
 * Handles inventory allocations for specific orders.
 */
@Repository
public interface InfrastructureAllocationSpringDataRepository extends JpaRepository<InfrastructureAllocationJpaEntity, UUID> {
    
    /**
     * Finds allocations by warehouse and product IDs.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of allocations for the given warehouse and product
     */
    List<InfrastructureAllocationJpaEntity> findByWarehouseIdAndProductId(UUID warehouseId, String productId);
    
    /**
     * Finds allocations by warehouse, product, and status.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param status the allocation status
     * @return list of allocations with the given status
     */
    List<InfrastructureAllocationJpaEntity> findByWarehouseIdAndProductIdAndStatus(UUID warehouseId, String productId, String status);
    
    /**
     * Finds allocations by batch ID.
     * @param batchId the batch ID
     * @return list of allocations for the given batch
     */
    List<InfrastructureAllocationJpaEntity> findByBatchId(UUID batchId);
    
    /**
     * Finds allocations by the reference they're allocated to.
     * @param allocatedTo the reference they're allocated to (e.g., order ID)
     * @return list of allocations for the given reference
     */
    List<InfrastructureAllocationJpaEntity> findByAllocatedTo(String allocatedTo);
    
    /**
     * Finds allocations by the reference they're allocated to and status.
     * @param allocatedTo the reference they're allocated to (e.g., order ID)
     * @param status the allocation status
     * @return list of allocations for the given reference and status
     */
    List<InfrastructureAllocationJpaEntity> findByAllocatedToAndStatus(String allocatedTo, String status);
    
    /**
     * Updates the status of an allocation.
     * @param id the allocation ID
     * @param status the new status
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureAllocationJpaEntity a SET a.status = :status WHERE a.id = :id")
    int updateAllocationStatus(@Param("id") UUID id, @Param("status") String status);
    
    /**
     * Counts pending allocations for a warehouse and product.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param status the allocation status
     * @return count of allocations with the given status
     */
    Long countByWarehouseIdAndProductIdAndStatus(UUID warehouseId, String productId, String status);
}