package ai.shreds.adapter.primary;

import ai.shreds.application.services.ApplicationBulkOperationEventHandler;
import ai.shreds.shared.dtos.SharedBulkOperationCompletedEvent;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Primary adapter that listens to bulk operation completion events from external systems.
 * These events inform the product catalog when batch operations (like imports, updates,
 * or migrations) have been completed and may require synchronization or validation.
 */
@Slf4j
@Component
@Tag(name = "Event Listeners", description = "Event listeners for external system events")
public class AdapterBulkOperationEventListener {

    private final ApplicationBulkOperationEventHandler applicationBulkOperationEventHandler;

    public AdapterBulkOperationEventListener(ApplicationBulkOperationEventHandler applicationBulkOperationEventHandler) {
        this.applicationBulkOperationEventHandler = applicationBulkOperationEventHandler;
    }

    /**
     * Handles bulk operation completion events by delegating to the application service.
     * This may trigger re-validation, re-indexing, or other synchronization processes
     * for the affected products.
     *
     * @param event The bulk operation completed event with information about the operation
     */
    @Async
    @EventListener
    public void handleBulkOperationCompleted(SharedBulkOperationCompletedEvent event) {
        log.info("Received bulk operation completed event: operationId={}, operationType={}, success={}, affectedProducts={}, userId={}",
                event.getOperationId(), event.getOperationType(), event.getSuccess(), 
                event.getAffectedProductIds().size(), event.getUserId());
        
        try {
            applicationBulkOperationEventHandler.handleBulkOperationCompleted(event);
            log.debug("Successfully processed bulk operation completed event for operationId={}", event.getOperationId());
        } catch (Exception e) {
            log.error("Error processing bulk operation completed event for operationId={}: {}", 
                    event.getOperationId(), e.getMessage(), e);
            throw e; // Re-throw to allow error handling at the Spring event level
        }
    }
}