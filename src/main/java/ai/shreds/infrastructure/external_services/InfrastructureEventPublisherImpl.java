package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.shared.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureEventPublisherImpl implements ApplicationOutputPortEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishProductCreatedEvent(SharedProductCreatedEvent event) {
        log.debug("Publishing product created event for product ID: {}", event.getProductId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishProductUpdatedEvent(SharedProductUpdatedEvent event) {
        log.debug("Publishing product updated event for product ID: {}", event.getProductId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishProductDeletedEvent(SharedProductDeletedEvent event) {
        log.debug("Publishing product deleted event for product ID: {}", event.getProductId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishCategoryChangedEvent(SharedCategoryChangedEvent event) {
        log.debug("Publishing category changed event for category ID: {}", event.getCategoryId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAttributeUpdatedEvent(SharedProductAttributeUpdatedEvent event) {
        log.debug("Publishing attribute updated event for attribute ID: {}", event.getAttributeId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishMediaUpdatedEvent(SharedProductMediaUpdatedEvent event) {
        log.debug("Publishing media updated event for media ID: {}", event.getMediaId());
        applicationEventPublisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionalEvent(Object event) {
        log.debug("Event received after commit: {}", event.getClass().getSimpleName());
    }
}