package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedStockMovementCreatedEventDTO;
import ai.shreds.shared.dtos.SharedStockLevelsUpdatedEventDTO;
import ai.shreds.shared.dtos.SharedBatchCreatedEventDTO;
import ai.shreds.shared.dtos.SharedWarehouseStatusChangedEventDTO;

/**
 * Output port for publishing domain events.
 * This interface defines the contract for publishing various domain events
 * to external systems or other bounded contexts.
 */
public interface ApplicationEventPublisherOutputPort {

    /**
     * Publishes a StockMovementCreated event.
     * This event is triggered when a stock movement is successfully processed.
     *
     * @param event The stock movement created event containing movement details
     */
    void publishStockMovementCreated(SharedStockMovementCreatedEventDTO event);

    /**
     * Publishes a StockLevelsUpdated event.
     * This event is triggered when inventory quantities are modified due to stock movements.
     *
     * @param event The stock levels updated event containing old and new quantity information
     */
    void publishStockLevelsUpdated(SharedStockLevelsUpdatedEventDTO event);

    /**
     * Publishes a BatchCreated event.
     * This event is triggered when a new batch is created for FIFO tracking.
     *
     * @param event The batch created event containing batch details
     */
    void publishBatchCreated(SharedBatchCreatedEventDTO event);

    /**
     * Publishes a WarehouseStatusChanged event.
     * This event is triggered when a warehouse's active status changes.
     *
     * @param event The warehouse status changed event containing status change details
     */
    void publishWarehouseStatusChanged(SharedWarehouseStatusChangedEventDTO event);
}