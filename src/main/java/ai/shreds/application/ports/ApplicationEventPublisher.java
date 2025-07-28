package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedProductCreatedEvent;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.shared.dtos.SharedProductDeletedEvent;
import ai.shreds.shared.dtos.SharedCategoryChangedEvent;

/**
 * Alternative interface for publishing domain events, created to resolve compilation issues.
 * This interface avoids problematic imports by using fully qualified class names.
 */
public interface ApplicationEventPublisher {

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
     * Using fully qualified name to avoid import issues.
     */
    void publishAttributeUpdatedEvent(ai.shreds.shared.dtos.SharedProductAttributeUpdatedEvent event);

    /**
     * Publishes a media updated event.
     * Using fully qualified name to avoid import issues.
     */
    void publishMediaUpdatedEvent(ai.shreds.shared.dtos.SharedProductMediaUpdatedEvent event);
}