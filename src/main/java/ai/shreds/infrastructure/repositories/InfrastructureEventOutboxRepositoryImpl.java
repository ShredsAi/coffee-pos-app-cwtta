package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEventOutboxEntity;
import ai.shreds.domain.ports.DomainOutputPortEventOutbox;
import ai.shreds.infrastructure.repositories.entities.InfrastructureEventOutboxJpaEntity;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the event outbox repository port.
 * Manages event persistence for the transactional outbox pattern.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureEventOutboxRepositoryImpl implements DomainOutputPortEventOutbox {

    private final InfrastructureEventOutboxSpringDataRepository springDataRepository;
    private final InfrastructureEntityMapper entityMapper;

    @Override
    public DomainEventOutboxEntity save(DomainEventOutboxEntity event) {
        try {
            log.debug("Saving event outbox with ID: {}", event.getId());
            InfrastructureEventOutboxJpaEntity jpaEntity = entityMapper.toJpaEventOutbox(event);
            InfrastructureEventOutboxJpaEntity saved = springDataRepository.save(jpaEntity);
            DomainEventOutboxEntity result = entityMapper.toDomainEventOutbox(saved);
            log.debug("Successfully saved event outbox with ID: {}", result.getId());
            return result;
        } catch (DataAccessException ex) {
            log.error("Failed to save event outbox with ID: {}", event.getId(), ex);
            throw new InfrastructurePersistenceException(
                "Failed to save event outbox",
                "DomainEventOutboxEntity",
                "save",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findUnprocessed() {
        try {
            log.debug("Finding unprocessed events");
            return springDataRepository.findByProcessedFalseOrderByCreatedAtAsc()
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find unprocessed events", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find unprocessed events",
                "DomainEventOutboxEntity",
                "findUnprocessed",
                ex
            );
        }
    }

    @Override
    @Transactional
    public void markAsProcessed(Long eventId) {
        try {
            log.debug("Marking event as processed: {}", eventId);
            if (!springDataRepository.existsById(eventId)) {
                throw new DomainEntityNotFoundException(
                    "Event not found: " + eventId,
                    "DomainEventOutboxEntity",
                    eventId.toString()
                );
            }
            springDataRepository.updateProcessedStatus(eventId, true, LocalDateTime.now());
            log.debug("Successfully marked event as processed: {}", eventId);
        } catch (DataAccessException ex) {
            log.error("Failed to mark event as processed: {}", eventId, ex);
            throw new InfrastructurePersistenceException(
                "Failed to mark event as processed",
                "DomainEventOutboxEntity",
                "markAsProcessed",
                ex
            );
        }
    }

    @Override
    public DomainEventOutboxEntity findById(Long eventId) {
        try {
            log.debug("Finding event by ID: {}", eventId);
            return springDataRepository.findById(eventId)
                    .map(entityMapper::toDomainEventOutbox)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find event by ID: {}", eventId, ex);
            throw new InfrastructurePersistenceException(
                "Failed to find event by ID",
                "DomainEventOutboxEntity",
                "findById",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findUnprocessedWithLimit(int limit) {
        try {
            log.debug("Finding unprocessed events with limit: {}", limit);
            return springDataRepository.findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, limit))
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find unprocessed events with limit", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find unprocessed events with limit",
                "DomainEventOutboxEntity",
                "findUnprocessedWithLimit",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findByAggregateId(UUID aggregateId) {
        try {
            log.debug("Finding events by aggregate ID: {}", aggregateId);
            return springDataRepository.findByAggregateIdOrderByCreatedAtAsc(aggregateId)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find events by aggregate ID", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find events by aggregate ID",
                "DomainEventOutboxEntity",
                "findByAggregateId",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findByAggregateType(String aggregateType) {
        try {
            log.debug("Finding events by aggregate type: {}", aggregateType);
            return springDataRepository.findByAggregateTypeOrderByCreatedAtAsc(aggregateType)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find events by aggregate type", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find events by aggregate type",
                "DomainEventOutboxEntity",
                "findByAggregateType",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findByEventType(String eventType) {
        try {
            log.debug("Finding events by event type: {}", eventType);
            return springDataRepository.findByEventTypeOrderByCreatedAtAsc(eventType)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find events by event type", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find events by event type",
                "DomainEventOutboxEntity",
                "findByEventType",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findProcessedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Finding processed events between {} and {}", startDate, endDate);
            return springDataRepository.findProcessedBetween(startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find processed events between dates", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find processed events between dates",
                "DomainEventOutboxEntity",
                "findProcessedBetween",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findUnprocessedOlderThan(LocalDateTime olderThan) {
        try {
            log.debug("Finding unprocessed events older than: {}", olderThan);
            return springDataRepository.findUnprocessedOlderThan(olderThan)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find unprocessed events older than date", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find unprocessed events older than date",
                "DomainEventOutboxEntity",
                "findUnprocessedOlderThan",
                ex
            );
        }
    }

    @Override
    public long countUnprocessed() {
        try {
            log.debug("Counting unprocessed events");
            return springDataRepository.countByProcessedFalse();
        } catch (DataAccessException ex) {
            log.error("Failed to count unprocessed events", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count unprocessed events",
                "DomainEventOutboxEntity",
                "countUnprocessed",
                ex
            );
        }
    }

    @Override
    public long countProcessedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.debug("Counting processed events between {} and {}", startDate, endDate);
            return springDataRepository.countProcessedBetween(startDate, endDate);
        } catch (DataAccessException ex) {
            log.error("Failed to count processed events between dates", ex);
            throw new InfrastructurePersistenceException(
                "Failed to count processed events between dates",
                "DomainEventOutboxEntity",
                "countProcessedBetween",
                ex
            );
        }
    }

    @Override
    @Transactional
    public long deleteProcessedOlderThan(LocalDateTime olderThan) {
        try {
            log.debug("Deleting processed events older than: {}", olderThan);
            long deletedCount = springDataRepository.deleteProcessedOlderThan(olderThan);
            log.debug("Deleted {} processed events older than {}", deletedCount, olderThan);
            return deletedCount;
        } catch (DataAccessException ex) {
            log.error("Failed to delete processed events older than date", ex);
            throw new InfrastructurePersistenceException(
                "Failed to delete processed events older than date",
                "DomainEventOutboxEntity",
                "deleteProcessedOlderThan",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findFailedEvents(int maxAgeMinutes) {
        try {
            log.debug("Finding failed events older than {} minutes", maxAgeMinutes);
            LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(maxAgeMinutes);
            return springDataRepository.findUnprocessedOlderThan(thresholdTime)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find failed events", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find failed events",
                "DomainEventOutboxEntity",
                "findFailedEvents",
                ex
            );
        }
    }

    @Override
    @Transactional
    public void markMultipleAsProcessed(List<Long> eventIds) {
        try {
            log.debug("Marking multiple events as processed: {}", eventIds.size());
            LocalDateTime processedAt = LocalDateTime.now();
            for (Long eventId : eventIds) {
                springDataRepository.updateProcessedStatus(eventId, true, processedAt);
            }
            log.debug("Successfully marked {} events as processed", eventIds.size());
        } catch (DataAccessException ex) {
            log.error("Failed to mark multiple events as processed", ex);
            throw new InfrastructurePersistenceException(
                "Failed to mark multiple events as processed",
                "DomainEventOutboxEntity",
                "markMultipleAsProcessed",
                ex
            );
        }
    }

    @Override
    public List<DomainEventOutboxEntity> findByAggregateAndDateRange(
            UUID aggregateId,
            String aggregateType,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        try {
            log.debug("Finding events by aggregate {} and date range", aggregateId);
            return springDataRepository.findByAggregateAndDateRange(aggregateId, aggregateType, startDate, endDate)
                    .stream()
                    .map(entityMapper::toDomainEventOutbox)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to find events by aggregate and date range", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find events by aggregate and date range",
                "DomainEventOutboxEntity",
                "findByAggregateAndDateRange",
                ex
            );
        }
    }

    @Override
    public DomainEventOutboxEntity findOldestUnprocessed() {
        try {
            log.debug("Finding oldest unprocessed event");
            return springDataRepository.findOldestUnprocessed()
                    .map(entityMapper::toDomainEventOutbox)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find oldest unprocessed event", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find oldest unprocessed event",
                "DomainEventOutboxEntity",
                "findOldestUnprocessed",
                ex
            );
        }
    }

    @Override
    public DomainEventOutboxEntity findMostRecentlyProcessed() {
        try {
            log.debug("Finding most recently processed event");
            return springDataRepository.findMostRecentlyProcessed()
                    .map(entityMapper::toDomainEventOutbox)
                    .orElse(null);
        } catch (DataAccessException ex) {
            log.error("Failed to find most recently processed event", ex);
            throw new InfrastructurePersistenceException(
                "Failed to find most recently processed event",
                "DomainEventOutboxEntity",
                "findMostRecentlyProcessed",
                ex
            );
        }
    }

    @Override
    @Transactional
    public void resetToUnprocessed(Long eventId) {
        try {
            log.debug("Resetting event to unprocessed: {}", eventId);
            if (!springDataRepository.existsById(eventId)) {
                throw new DomainEntityNotFoundException(
                    "Event not found: " + eventId,
                    "DomainEventOutboxEntity",
                    eventId.toString()
                );
            }
            springDataRepository.updateProcessedStatus(eventId, false, null);
            log.debug("Successfully reset event to unprocessed: {}", eventId);
        } catch (DataAccessException ex) {
            log.error("Failed to reset event to unprocessed: {}", eventId, ex);
            throw new InfrastructurePersistenceException(
                "Failed to reset event to unprocessed",
                "DomainEventOutboxEntity",
                "resetToUnprocessed",
                ex
            );
        }
    }
}
