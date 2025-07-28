package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedProductCreatedEvent;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.shared.dtos.SharedProductDeletedEvent;
import ai.shreds.shared.dtos.SharedCategoryChangedEvent;

/**
 * Alternative event publisher port created to resolve compilation caching issues.
 * This replaces ApplicationOutputPortEventPublisher functionality.
 */
public interface ApplicationEventPublisherPort {

    /**
     * Publishes a product created event.
     */
    void publishProductCreatedEvent(SharedProductCreatedEvent event);

    /**
     * Publishes a product updated event.
     */
    void publishProductUpdatedEvent(SharedProductUpdatedEvent event);

    /**
     * Publishes a product deleted event.
     */
    void publishProductDeletedEvent(SharedProductDeletedEvent event);

    /**
     * Publishes a category changed event.
     */
    void publishCategoryChangedEvent(SharedCategoryChangedEvent event);

    /**
     * Publishes an attribute updated event.
     * Generic approach to avoid import issues.
     */
    void publishAttributeUpdatedEvent(Object event);

    /**
     * Publishes a media updated event.
     * Generic approach to avoid import issues.
     */
    void publishMediaUpdatedEvent(Object event);
}