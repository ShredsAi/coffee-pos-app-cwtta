package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.shared.dtos.SharedBatchCreatedEventDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import ai.shreds.shared.dtos.SharedStockMovementCreatedEventDTO;
import ai.shreds.shared.dtos.SharedWarehouseStatusChangedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Infrastructure adapter for publishing application events.
 * Uses Spring's ApplicationEventPublisher as the underlying mechanism.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureEventPublisherAdapter implements ApplicationEventPublisherOutputPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishStockMovementCreated(SharedStockMovementCreatedEventDTO event) {
        log.debug("Publishing stock movement created event: {}", event);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishStockLevelsUpdated(SharedStockLevelsUpdatedEventDTO event) {
        log.debug("Publishing stock levels updated event: {}", event);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishBatchCreated(SharedBatchCreatedEventDTO event) {
        log.debug("Publishing batch created event: {}", event);
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishWarehouseStatusChanged(SharedWarehouseStatusChangedEventDTO event) {
        log.debug("Publishing warehouse status changed event: {}", event);
        applicationEventPublisher.publishEvent(event);
    }
}