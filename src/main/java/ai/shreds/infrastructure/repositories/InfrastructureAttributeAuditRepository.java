package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureAttributeAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for attribute audit entities.
 * Provides CRUD operations and custom query methods for attribute audit history.
 */
@Repository
public interface InfrastructureAttributeAuditRepository extends JpaRepository<InfrastructureAttributeAuditJpaEntity, Long> {

    /**
     * Finds audit entries for a specific attribute ordered by change timestamp descending
     * @param attributeId the attribute ID
     * @return list of audit entries
     */
    List<InfrastructureAttributeAuditJpaEntity> findByAttributeIdOrderByChangedAtDesc(UUID attributeId);

    /**
     * Finds audit entries for a specific attribute and change type
     * @param attributeId the attribute ID
     * @param changeType the change type
     * @return list of audit entries
     */
    List<InfrastructureAttributeAuditJpaEntity> findByAttributeIdAndChangeTypeOrderByChangedAtDesc(UUID attributeId, String changeType);

    /**
     * Finds recent audit entries since a specific timestamp
     * @param since the timestamp to check changes since
     * @return list of recent audit entries
     */
    @Query("SELECT a FROM InfrastructureAttributeAuditJpaEntity a WHERE a.changedAt > :since ORDER BY a.changedAt DESC")
    List<InfrastructureAttributeAuditJpaEntity> findRecentChanges(@Param("since") Instant since);

    /**
     * Finds audit entries by user who made the change
     * @param changedBy the user who made the change
     * @return list of audit entries
     */
    List<InfrastructureAttributeAuditJpaEntity> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Finds audit entries within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit entries
     */
    @Query("SELECT a FROM InfrastructureAttributeAuditJpaEntity a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<InfrastructureAttributeAuditJpaEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Counts audit entries for a specific attribute
     * @param attributeId the attribute ID
     * @return count of audit entries
     */
    long countByAttributeId(UUID attributeId);

    /**
     * Deletes old audit entries before a specific date
     * @param beforeDate the date before which to delete entries
     */
    @Query("DELETE FROM InfrastructureAttributeAuditJpaEntity a WHERE a.changedAt < :beforeDate")
    void deleteOldEntries(@Param("beforeDate") Instant beforeDate);
}