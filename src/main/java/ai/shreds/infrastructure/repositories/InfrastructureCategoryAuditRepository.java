package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureCategoryAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for category audit entities.
 * Provides CRUD operations and custom query methods for category audit history.
 */
@Repository
public interface InfrastructureCategoryAuditRepository extends JpaRepository<InfrastructureCategoryAuditJpaEntity, Long> {

    /**
     * Finds audit entries for a specific category ordered by change timestamp descending
     * @param categoryId the category ID
     * @return list of audit entries
     */
    List<InfrastructureCategoryAuditJpaEntity> findByCategoryIdOrderByChangedAtDesc(UUID categoryId);

    /**
     * Finds audit entries for a specific category and change type
     * @param categoryId the category ID
     * @param changeType the change type
     * @return list of audit entries
     */
    List<InfrastructureCategoryAuditJpaEntity> findByCategoryIdAndChangeTypeOrderByChangedAtDesc(UUID categoryId, String changeType);

    /**
     * Finds recent audit entries since a specific timestamp
     * @param since the timestamp to check changes since
     * @return list of recent audit entries
     */
    @Query("SELECT a FROM InfrastructureCategoryAuditJpaEntity a WHERE a.changedAt > :since ORDER BY a.changedAt DESC")
    List<InfrastructureCategoryAuditJpaEntity> findRecentChanges(@Param("since") Instant since);

    /**
     * Finds audit entries by user who made the change
     * @param changedBy the user who made the change
     * @return list of audit entries
     */
    List<InfrastructureCategoryAuditJpaEntity> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Finds audit entries within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit entries
     */
    @Query("SELECT a FROM InfrastructureCategoryAuditJpaEntity a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<InfrastructureCategoryAuditJpaEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Counts audit entries for a specific category
     * @param categoryId the category ID
     * @return count of audit entries
     */
    long countByCategoryId(UUID categoryId);

    /**
     * Deletes old audit entries before a specific date
     * @param beforeDate the date before which to delete entries
     */
    @Query("DELETE FROM InfrastructureCategoryAuditJpaEntity a WHERE a.changedAt < :beforeDate")
    void deleteOldEntries(@Param("beforeDate") Instant beforeDate);
}