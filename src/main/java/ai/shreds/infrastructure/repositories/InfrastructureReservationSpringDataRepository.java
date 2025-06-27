package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for reservation operations.
 * Handles temporary inventory reservations with expiration policy.
 */
@Repository
public interface InfrastructureReservationSpringDataRepository extends JpaRepository<InfrastructureReservationJpaEntity, UUID> {
    
    /**
     * Finds reservations by warehouse, product and status.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param status the reservation status
     * @return list of matching reservations
     */
    List<InfrastructureReservationJpaEntity> findByWarehouseIdAndProductIdAndStatus(UUID warehouseId, String productId, String status);
    
    /**
     * Finds active reservations for a warehouse and product.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return list of active reservations
     */
    @Query("SELECT r FROM InfrastructureReservationJpaEntity r WHERE r.warehouseId = :warehouseId " +
           "AND r.productId = :productId AND r.status = 'ACTIVE'")
    List<InfrastructureReservationJpaEntity> findActiveByWarehouseIdAndProductId(
            @Param("warehouseId") UUID warehouseId, 
            @Param("productId") String productId);
    
    /**
     * Finds expired reservations that are still active.
     * @param currentTime the current datetime for comparison
     * @return list of expired reservations
     */
    @Query("SELECT r FROM InfrastructureReservationJpaEntity r " +
           "WHERE r.status = 'ACTIVE' AND r.expiresAt < :currentTime")
    List<InfrastructureReservationJpaEntity> findExpiredReservations(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Updates status of expired reservations.
     * @param currentTime the current datetime for comparison
     * @param newStatus the new status to set
     * @return count of updated reservations
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureReservationJpaEntity r SET r.status = :newStatus " +
           "WHERE r.status = 'ACTIVE' AND r.expiresAt < :currentTime")
    int updateExpiredReservations(@Param("currentTime") LocalDateTime currentTime, @Param("newStatus") String newStatus);
    
    /**
     * Updates the status of a reservation.
     * @param id the reservation ID
     * @param status the new status
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureReservationJpaEntity r SET r.status = :status WHERE r.id = :id")
    int updateReservationStatus(@Param("id") UUID id, @Param("status") String status);
    
    /**
     * Finds reservations by the reference they're reserved for.
     * @param reservedFor the reference they're reserved for (e.g., cart ID)
     * @return list of reservations for the given reference
     */
    List<InfrastructureReservationJpaEntity> findByReservedFor(String reservedFor);
    
    /**
     * Finds reservations by the reference they're reserved for and status.
     * @param reservedFor the reference they're reserved for (e.g., cart ID)
     * @param status the reservation status
     * @return list of reservations for the given reference and status
     */
    List<InfrastructureReservationJpaEntity> findByReservedForAndStatus(String reservedFor, String status);
    
    /**
     * Counts active reservations for a warehouse and product.
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @return count of active reservations
     */
    @Query("SELECT COUNT(r) FROM InfrastructureReservationJpaEntity r WHERE r.warehouseId = :warehouseId " +
           "AND r.productId = :productId AND r.status = 'ACTIVE'")
    Long countActiveReservations(@Param("warehouseId") UUID warehouseId, @Param("productId") String productId);
}