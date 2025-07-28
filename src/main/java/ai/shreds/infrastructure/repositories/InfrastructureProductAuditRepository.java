package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product audit entities.
 * Provides CRUD operations and custom query methods for product audit history.
 */
@Repository
public interface InfrastructureProductAuditRepository extends JpaRepository<InfrastructureProductAuditJpaEntity, Long> {

    /**
     * Finds audit entries for a specific product ordered by change timestamp descending
     * @param productId the product ID
     * @return list of audit entries
     */
    List<InfrastructureProductAuditJpaEntity> findByProductIdOrderByChangedAtDesc(UUID productId);

    /**
     * Finds audit entries for a specific product and change type
     * @param productId the product ID
     * @param changeType the change type
     * @return list of audit entries
     */
    List<InfrastructureProductAuditJpaEntity> findByProductIdAndChangeTypeOrderByChangedAtDesc(UUID productId, String changeType);

    /**
     * Finds recent audit entries since a specific timestamp
     * @param since the timestamp to check changes since
     * @return list of recent audit entries
     */
    @Query("SELECT a FROM InfrastructureProductAuditJpaEntity a WHERE a.changedAt > :since ORDER BY a.changedAt DESC")
    List<InfrastructureProductAuditJpaEntity> findRecentChanges(@Param("since") Instant since);

    /**
     * Finds audit entries by user who made the change
     * @param changedBy the user who made the change
     * @return list of audit entries
     */
    List<InfrastructureProductAuditJpaEntity> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Finds audit entries within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit entries
     */
    @Query("SELECT a FROM InfrastructureProductAuditJpaEntity a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<InfrastructureProductAuditJpaEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Counts audit entries for a specific product
     * @param productId the product ID
     * @return count of audit entries
     */
    long countByProductId(UUID productId);

    /**
     * Deletes old audit entries before a specific date
     * @param beforeDate the date before which to delete entries
     */
    @Query("DELETE FROM InfrastructureProductAuditJpaEntity a WHERE a.changedAt < :beforeDate")
    void deleteOldEntries(@Param("beforeDate") Instant beforeDate);
}