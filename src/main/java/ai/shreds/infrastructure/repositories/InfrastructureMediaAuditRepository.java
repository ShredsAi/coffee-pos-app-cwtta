package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureMediaAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for media audit entities.
 * Provides CRUD operations and custom query methods for media audit history.
 */
@Repository
public interface InfrastructureMediaAuditRepository extends JpaRepository<InfrastructureMediaAuditJpaEntity, Long> {

    /**
     * Finds audit entries for a specific media ordered by change timestamp descending
     * @param mediaId the media ID
     * @return list of audit entries
     */
    List<InfrastructureMediaAuditJpaEntity> findByMediaIdOrderByChangedAtDesc(UUID mediaId);

    /**
     * Finds audit entries for a specific media and change type
     * @param mediaId the media ID
     * @param changeType the change type
     * @return list of audit entries
     */
    List<InfrastructureMediaAuditJpaEntity> findByMediaIdAndChangeTypeOrderByChangedAtDesc(UUID mediaId, String changeType);

    /**
     * Finds recent audit entries since a specific timestamp
     * @param since the timestamp to check changes since
     * @return list of recent audit entries
     */
    @Query("SELECT a FROM InfrastructureMediaAuditJpaEntity a WHERE a.changedAt > :since ORDER BY a.changedAt DESC")
    List<InfrastructureMediaAuditJpaEntity> findRecentChanges(@Param("since") Instant since);

    /**
     * Finds audit entries by user who made the change
     * @param changedBy the user who made the change
     * @return list of audit entries
     */
    List<InfrastructureMediaAuditJpaEntity> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Finds audit entries within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit entries
     */
    @Query("SELECT a FROM InfrastructureMediaAuditJpaEntity a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<InfrastructureMediaAuditJpaEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Counts audit entries for a specific media
     * @param mediaId the media ID
     * @return count of audit entries
     */
    long countByMediaId(UUID mediaId);

    /**
     * Deletes old audit entries before a specific date
     * @param beforeDate the date before which to delete entries
     */
    @Query("DELETE FROM InfrastructureMediaAuditJpaEntity a WHERE a.changedAt < :beforeDate")
    void deleteOldEntries(@Param("beforeDate") Instant beforeDate);
}