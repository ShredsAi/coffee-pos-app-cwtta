package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.domain.ports.DomainInputPortProductService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.shared.dtos.SharedSkuStatusChangedEvent;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.enums.DomainPublicationStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for handling SKU status changed events from other shreds
 * and updating product publication status accordingly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationSkuEventHandler {

    private final DomainInputPortProductService domainProductService;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    
    /**
     * Handles SKU status changed events by updating product publication status
     * if necessary based on SKU availability.
     *
     * @param event The SKU status changed event
     */
    public void handleSkuStatusChanged(SharedSkuStatusChangedEvent event) {
        if (event == null || event.getProductId() == null || event.getSkuStatus() == null) {
            log.warn("Received invalid SKU status changed event");
            return;
        }
        
        log.info("Handling SKU status changed event for product {}: new status {}", 
                event.getProductId(), event.getSkuStatus());
                
        updateProductPublicationStatus(event.getProductId(), event.getSkuStatus());
    }
    
    /**
     * Updates product publication status based on SKU status.
     * If SKU becomes INACTIVE and product is PUBLISHED, changes to REVIEW.
     * If SKU becomes ACTIVE and product is REVIEW or DRAFT, validates if it can be published.
     *
     * @param productId The product ID
     * @param skuStatus The SKU status (ACTIVE, INACTIVE, etc.)
     */
    private void updateProductPublicationStatus(UUID productId, String skuStatus) {
        try {
            DomainProductEntity product = domainProductService.getProduct(productId);
            DomainPublicationStatus currentStatus = product.getPublicationStatus();
            DomainPublicationStatus newStatus = currentStatus;
            boolean statusChanged = false;
            Map<String, Object> changes = new HashMap<>();
            
            // If SKU is inactive and product is published, move to review
            if ("INACTIVE".equalsIgnoreCase(skuStatus) && currentStatus == DomainPublicationStatus.PUBLISHED) {
                newStatus = DomainPublicationStatus.REVIEW;
                statusChanged = true;
            }
            
            // If SKU is active and product is in review, check if it can be published
            // Note: In a real implementation, additional validation would be done here
            if ("ACTIVE".equalsIgnoreCase(skuStatus) && 
                    (currentStatus == DomainPublicationStatus.REVIEW || currentStatus == DomainPublicationStatus.DRAFT)) {
                // Validate that product meets PUBLISHED requirements
                try {
                    domainProductService.validatePublicationRequirements(productId);
                    // If validation passes, we can publish
                    newStatus = DomainPublicationStatus.PUBLISHED;
                    statusChanged = true;
                } catch (Exception e) {
                    log.info("Product {} cannot be published: {}", productId, e.getMessage());
                    // Keep in current state if validation fails
                }
            }
            
            // If status changed, update the product
            if (statusChanged) {
                log.info("Updating product {} publication status from {} to {}", 
                        productId, currentStatus, newStatus);
                        
                domainProductService.changePublicationStatus(productId, newStatus);
                
                changes.put("publicationStatus", newStatus.name());
                changes.put("previousStatus", currentStatus.name());
                changes.put("reason", "SKU status changed to " + skuStatus);
                
                // Republish updated product event
                product = domainProductService.getProduct(productId); // Get updated product
                eventPublisher.publishProductUpdatedEvent(
                    SharedProductUpdatedEvent.fromProduct(product, changes, getCurrentUser())
                );
            }
        } catch (Exception e) {
            log.error("Error updating product {} publication status based on SKU status: {}", 
                    productId, e.getMessage(), e);
        }
    }
    
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}