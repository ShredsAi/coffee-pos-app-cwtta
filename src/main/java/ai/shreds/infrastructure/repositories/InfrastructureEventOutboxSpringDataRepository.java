package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureEventOutboxJpaEntity;
import org.springframework.data.domain.Pageable;
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
 * Spring Data JPA repository for event outbox operations.
 * Provides comprehensive querying capabilities for the transactional outbox pattern.
 */
@Repository
public interface InfrastructureEventOutboxSpringDataRepository extends JpaRepository<InfrastructureEventOutboxJpaEntity, Long> {
    
    /**
     * Finds unprocessed events ordered by creation time.
     * @param pageable pagination information
     * @return list of unprocessed events
     */
    List<InfrastructureEventOutboxJpaEntity> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);
    
    /**
     * Finds all unprocessed events ordered by creation time.
     * @return list of unprocessed events
     */
    List<InfrastructureEventOutboxJpaEntity> findByProcessedFalseOrderByCreatedAtAsc();
    
    /**
     * Updates the processed status of an event.
     * @param eventId the event ID
     * @param processed the processed status
     * @param processedAt the processed timestamp
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureEventOutboxJpaEntity e SET e.processed = :processed, e.processedAt = :processedAt WHERE e.id = :eventId")
    void updateProcessedStatus(@Param("eventId") Long eventId, @Param("processed") Boolean processed, @Param("processedAt") LocalDateTime processedAt);
    
    /**
     * Counts unprocessed events.
     * @return number of unprocessed events
     */
    long countByProcessedFalse();
    
    /**
     * Finds events by aggregate ID.
     * @param aggregateId the aggregate ID
     * @return list of events for the aggregate
     */
    List<InfrastructureEventOutboxJpaEntity> findByAggregateIdOrderByCreatedAtAsc(UUID aggregateId);
    
    /**
     * Finds events by aggregate type.
     * @param aggregateType the aggregate type
     * @return list of events for the aggregate type
     */
    List<InfrastructureEventOutboxJpaEntity> findByAggregateTypeOrderByCreatedAtAsc(String aggregateType);
    
    /**
     * Finds events by event type.
     * @param eventType the event type
     * @return list of events of the specified type
     */
    List<InfrastructureEventOutboxJpaEntity> findByEventTypeOrderByCreatedAtAsc(String eventType);
    
    /**
     * Finds processed events within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of processed events in the date range
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = true " +
           "AND e.processedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY e.processedAt DESC")
    List<InfrastructureEventOutboxJpaEntity> findProcessedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds unprocessed events older than the specified date.
     * @param olderThan the date threshold
     * @return list of unprocessed events older than the date
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = false " +
           "AND e.createdAt < :olderThan " +
           "ORDER BY e.createdAt ASC")
    List<InfrastructureEventOutboxJpaEntity> findUnprocessedOlderThan(@Param("olderThan") LocalDateTime olderThan);
    
    /**
     * Counts processed events within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return count of processed events
     */
    @Query("SELECT COUNT(e) FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = true " +
           "AND e.processedAt BETWEEN :startDate AND :endDate")
    long countProcessedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Deletes processed events older than the specified date.
     * @param olderThan the date threshold for deletion
     * @return number of events deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = true " +
           "AND e.processedAt < :olderThan")
    long deleteProcessedOlderThan(@Param("olderThan") LocalDateTime olderThan);
    
    /**
     * Finds events by aggregate ID and type within a date range.
     * @param aggregateId the aggregate ID
     * @param aggregateType the aggregate type
     * @param startDate the start date
     * @param endDate the end date
     * @return list of matching events
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.aggregateId = :aggregateId " +
           "AND e.aggregateType = :aggregateType " +
           "AND e.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY e.createdAt ASC")
    List<InfrastructureEventOutboxJpaEntity> findByAggregateAndDateRange(
            @Param("aggregateId") UUID aggregateId,
            @Param("aggregateType") String aggregateType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Gets the oldest unprocessed event.
     * @return the oldest unprocessed event
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = false " +
           "ORDER BY e.createdAt ASC")
    Optional<InfrastructureEventOutboxJpaEntity> findOldestUnprocessed();
    
    /**
     * Gets the most recently processed event.
     * @return the most recently processed event
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = true " +
           "ORDER BY e.processedAt DESC")
    Optional<InfrastructureEventOutboxJpaEntity> findMostRecentlyProcessed();
    
    /**
     * Finds events by aggregate ID and event type.
     * @param aggregateId the aggregate ID
     * @param eventType the event type
     * @return list of matching events
     */
    List<InfrastructureEventOutboxJpaEntity> findByAggregateIdAndEventTypeOrderByCreatedAtAsc(UUID aggregateId, String eventType);
    
    /**
     * Finds events by aggregate type and event type.
     * @param aggregateType the aggregate type
     * @param eventType the event type
     * @return list of matching events
     */
    List<InfrastructureEventOutboxJpaEntity> findByAggregateTypeAndEventTypeOrderByCreatedAtAsc(String aggregateType, String eventType);
    
    /**
     * Finds events created after a specific date.
     * @param createdAfter the date to filter by
     * @return list of events created after the date
     */
    List<InfrastructureEventOutboxJpaEntity> findByCreatedAtAfterOrderByCreatedAtAsc(LocalDateTime createdAfter);
    
    /**
     * Finds events processed after a specific date.
     * @param processedAfter the date to filter by
     * @return list of events processed after the date
     */
    @Query("SELECT e FROM InfrastructureEventOutboxJpaEntity e WHERE e.processed = true " +
           "AND e.processedAt > :processedAfter " +
           "ORDER BY e.processedAt DESC")
    List<InfrastructureEventOutboxJpaEntity> findProcessedAfter(@Param("processedAfter") LocalDateTime processedAfter);
    
    /**
     * Counts events by aggregate type.
     * @param aggregateType the aggregate type
     * @return count of events
     */
    long countByAggregateType(String aggregateType);
    
    /**
     * Counts events by event type.
     * @param eventType the event type
     * @return count of events
     */
    long countByEventType(String eventType);
    
    /**
     * Finds events by processing status.
     * @param processed the processing status
     * @return list of events with the specified status
     */
    List<InfrastructureEventOutboxJpaEntity> findByProcessedOrderByCreatedAtAsc(boolean processed);
}
