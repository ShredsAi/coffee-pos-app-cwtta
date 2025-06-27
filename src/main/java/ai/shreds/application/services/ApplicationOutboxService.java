package ai.shreds.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.domain.ports.DomainOutputPortEventOutbox;
import ai.shreds.domain.entities.DomainEventOutboxEntity;
import ai.shreds.shared.dtos.SharedStockMovementCreatedEventDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import ai.shreds.shared.dtos.SharedBatchCreatedEventDTO;
import ai.shreds.shared.dtos.SharedWarehouseStatusChangedEventDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class ApplicationOutboxService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationOutboxService.class);

    private final DomainOutputPortEventOutbox outboxRepository;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ObjectMapper objectMapper;

    public ApplicationOutboxService(DomainOutputPortEventOutbox outboxRepository,
                                    ApplicationEventPublisherOutputPort eventPublisher,
                                    ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    public void saveEvent(String eventType, java.util.UUID aggregateId, String aggregateType, Object payload) {
        DomainEventOutboxEntity event = new DomainEventOutboxEntity();
        event.setAggregateId(aggregateId);
        event.setAggregateType(aggregateType);
        event.setEventType(eventType);
        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
        event.setCreatedAt(LocalDateTime.now());
        event.setProcessed(false);
        outboxRepository.save(event);
    }

    public void processUnpublishedEvents() {
        List<DomainEventOutboxEntity> events = outboxRepository.findUnprocessed();
        for (DomainEventOutboxEntity event : events) {
            try {
                switch (event.getEventType()) {
                    case "StockMovementCreated":
                        SharedStockMovementCreatedEventDTO smEvent = objectMapper.readValue(event.getPayload(), SharedStockMovementCreatedEventDTO.class);
                        eventPublisher.publishStockMovementCreated(smEvent);
                        break;
                    case "StockLevelsUpdated":
                        SharedStockLevelsUpdatedEventDTO slEvent = objectMapper.readValue(event.getPayload(), SharedStockLevelsUpdatedEventDTO.class);
                        eventPublisher.publishStockLevelsUpdated(slEvent);
                        break;
                    case "BatchCreated":
                        SharedBatchCreatedEventDTO bcEvent = objectMapper.readValue(event.getPayload(), SharedBatchCreatedEventDTO.class);
                        eventPublisher.publishBatchCreated(bcEvent);
                        break;
                    case "WarehouseStatusChanged":
                        SharedWarehouseStatusChangedEventDTO wsEvent = objectMapper.readValue(event.getPayload(), SharedWarehouseStatusChangedEventDTO.class);
                        eventPublisher.publishWarehouseStatusChanged(wsEvent);
                        break;
                    default:
                        logger.warn("Unknown event type: {}", event.getEventType());
                }
                outboxRepository.markAsProcessed(event.getId());
            } catch (Exception e) {
                logger.error("Failed to process event with id {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }

    public void markEventAsProcessed(Long eventId) {
        outboxRepository.markAsProcessed(eventId);
    }
}