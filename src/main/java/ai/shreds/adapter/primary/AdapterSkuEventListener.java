package ai.shreds.adapter.primary;

import ai.shreds.application.services.ApplicationSkuEventHandler;
import ai.shreds.shared.dtos.SharedSkuStatusChangedEvent;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Primary adapter that listens to SKU status change events from external systems.
 * These events typically come from inventory or SKU lifecycle management systems
 * to inform the product catalog about changes in SKU availability or status.
 */
@Slf4j
@Component
@Tag(name = "Event Listeners", description = "Event listeners for external system events")
public class AdapterSkuEventListener {

    private final ApplicationSkuEventHandler applicationSkuEventHandler;

    public AdapterSkuEventListener(ApplicationSkuEventHandler applicationSkuEventHandler) {
        this.applicationSkuEventHandler = applicationSkuEventHandler;
    }

    /**
     * Handles SKU status change events by delegating to the application service.
     * The product's publication status may be changed based on SKU status updates.
     *
     * @param event The SKU status changed event with information about the affected SKU
     */
    @Async
    @EventListener
    public void handleSkuStatusChanged(SharedSkuStatusChangedEvent event) {
        log.info("Received SKU status changed event: productId={}, status={}, userId={}", 
                event.getProductId(), event.getSkuStatus(), event.getUserId());
        
        try {
            applicationSkuEventHandler.handleSkuStatusChanged(event);
            log.debug("Successfully processed SKU status changed event for productId={}", event.getProductId());
        } catch (Exception e) {
            log.error("Error processing SKU status changed event for productId={}: {}", 
                    event.getProductId(), e.getMessage(), e);
            throw e; // Re-throw to allow error handling at the Spring event level
        }
    }
}