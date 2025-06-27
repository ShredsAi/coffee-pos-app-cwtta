package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEventOutboxEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for event outbox repository operations.
 * Defines the contract for reliable event publishing using the transactional outbox pattern.
 * To be implemented by infrastructure layer.
 * 
 * The outbox pattern ensures that events are published reliably by storing them
 * in the same database transaction as business data and publishing them asynchronously.
 */
public interface DomainOutputPortEventOutbox {
    
    /**
     * Saves an event outbox entity.
     * Events are stored transactionally with business data to ensure consistency.
     * 
     * @param event the event outbox entity to save
     * @return the saved event with generated ID if it was null
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if save fails
     */
    DomainEventOutboxEntity save(DomainEventOutboxEntity event);
    
    /**
     * Finds all unprocessed events in order of creation.
     * Used by event publishers to process events in FIFO order.
     * 
     * @return list of unprocessed events ordered by creation time
     */
    List<DomainEventOutboxEntity> findUnprocessed();
    
    /**
     * Marks an event as processed by setting the processed flag and timestamp.
     * Called after successful event publication to prevent reprocessing.
     * 
     * @param eventId the event ID to mark as processed
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if event not found
     */
    void markAsProcessed(Long eventId);
    
    /**
     * Finds an event by its unique identifier.
     * 
     * @param eventId the event ID
     * @return the event entity, or null if not found
     */
    DomainEventOutboxEntity findById(Long eventId);
    
    /**
     * Finds a limited number of unprocessed events for batch processing.
     * Used by event publishers to process events in batches for better performance.
     * 
     * @param limit the maximum number of events to return
     * @return list of unprocessed events limited to the specified count
     */
    List<DomainEventOutboxEntity> findUnprocessedWithLimit(int limit);
    
    /**
     * Finds events by aggregate ID.
     * Useful for tracking events related to a specific business entity.
     * 
     * @param aggregateId the aggregate ID
     * @return list of events for the aggregate
     */
    List<DomainEventOutboxEntity> findByAggregateId(UUID aggregateId);
    
    /**
     * Finds events by aggregate type.
     * 
     * @param aggregateType the aggregate type (e.g., "InventoryItem", "Warehouse")
     * @return list of events for the aggregate type
     */
    List<DomainEventOutboxEntity> findByAggregateType(String aggregateType);
    
    /**
     * Finds events by event type.
     * 
     * @param eventType the event type (e.g., "StockMovementCreated")
     * @return list of events of the specified type
     */
    List<DomainEventOutboxEntity> findByEventType(String eventType);
    
    /**
     * Finds processed events within a date range.
     * Used for monitoring and auditing purposes.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of processed events in the date range
     */
    List<DomainEventOutboxEntity> findProcessedBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds unprocessed events older than the specified date.
     * Used to identify stale events that may need attention.
     * 
     * @param olderThan the date threshold
     * @return list of unprocessed events older than the date
     */
    List<DomainEventOutboxEntity> findUnprocessedOlderThan(LocalDateTime olderThan);
    
    /**
     * Counts total number of unprocessed events.
     * Used for monitoring and alerting.
     * 
     * @return count of unprocessed events
     */
    long countUnprocessed();
    
    /**
     * Counts processed events within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return count of processed events
     */
    long countProcessedBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Deletes processed events older than the specified date.
     * Used for cleanup to prevent unbounded growth of the outbox table.
     * 
     * @param olderThan the date threshold for deletion
     * @return number of events deleted
     */
    long deleteProcessedOlderThan(LocalDateTime olderThan);
    
    /**
     * Finds events that failed processing (unprocessed and very old).
     * 
     * @param maxAgeMinutes the maximum age in minutes for normal processing
     * @return list of events that appear to have failed processing
     */
    List<DomainEventOutboxEntity> findFailedEvents(int maxAgeMinutes);
    
    /**
     * Batch updates multiple events as processed.
     * More efficient than individual updates when processing many events.
     * 
     * @param eventIds the list of event IDs to mark as processed
     */
    void markMultipleAsProcessed(List<Long> eventIds);
    
    /**
     * Finds events by aggregate ID and type within a date range.
     * 
     * @param aggregateId the aggregate ID
     * @param aggregateType the aggregate type
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of matching events
     */
    List<DomainEventOutboxEntity> findByAggregateAndDateRange(
            UUID aggregateId, 
            String aggregateType, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
    
    /**
     * Gets the oldest unprocessed event.
     * Used to identify processing backlog.
     * 
     * @return the oldest unprocessed event, or null if none
     */
    DomainEventOutboxEntity findOldestUnprocessed();
    
    /**
     * Gets the most recently processed event.
     * 
     * @return the most recently processed event, or null if none
     */
    DomainEventOutboxEntity findMostRecentlyProcessed();
    
    /**
     * Resets an event to unprocessed state.
     * Used for retrying failed events.
     * 
     * @param eventId the event ID to reset
     */
    void resetToUnprocessed(Long eventId);
}