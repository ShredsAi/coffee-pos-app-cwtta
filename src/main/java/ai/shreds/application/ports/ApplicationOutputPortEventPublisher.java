package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedCategoryChangedEvent;
import ai.shreds.shared.dtos.SharedProductCreatedEvent;
import ai.shreds.shared.dtos.SharedProductDeletedEvent;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.shared.dtos.SharedProductAttributeUpdatedEvent;
import ai.shreds.shared.dtos.SharedProductMediaUpdatedEvent;

/**
 * Port for publishing domain events to external consumers.
 */
public interface ApplicationOutputPortEventPublisher {
    void publishProductCreatedEvent(SharedProductCreatedEvent event);
    void publishProductUpdatedEvent(SharedProductUpdatedEvent event);
    void publishProductDeletedEvent(SharedProductDeletedEvent event);
    void publishCategoryChangedEvent(SharedCategoryChangedEvent event);
    void publishAttributeUpdatedEvent(SharedProductAttributeUpdatedEvent event);
    void publishMediaUpdatedEvent(SharedProductMediaUpdatedEvent event);
}