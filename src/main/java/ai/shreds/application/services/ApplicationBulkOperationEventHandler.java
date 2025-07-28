package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.domain.ports.DomainInputPortProductService;
import ai.shreds.domain.ports.DomainInputPortCategoryService;
import ai.shreds.domain.ports.DomainInputPortAttributeValueService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.shared.dtos.SharedBulkOperationCompletedEvent;
import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.entities.DomainProductCategoryEntity;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.shared.dtos.SharedCategoryChangedEvent;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for handling bulk operation completed events from other shreds
 * and performing necessary updates on affected products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationBulkOperationEventHandler {

    private final DomainInputPortProductService domainProductService;
    private final DomainInputPortCategoryService domainCategoryService;
    private final DomainInputPortAttributeValueService domainAttributeValueService;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    
    /**
     * Handles bulk operation completed events by processing updates 
     * on affected products based on the operation type.
     *
     * @param event The bulk operation completed event
     */
    @Transactional
    public void handleBulkOperationCompleted(SharedBulkOperationCompletedEvent event) {
        if (event == null || event.getOperationId() == null || event.getOperationType() == null) {
            log.warn("Received invalid bulk operation completed event");
            return;
        }
        
        if (event.getAffectedProductIds() == null || event.getAffectedProductIds().isEmpty()) {
            log.info("Bulk operation {} completed but no products were affected", event.getOperationId());
            return;
        }
        
        log.info("Handling bulk operation completed event: {} with {} affected products", 
                event.getOperationType(), event.getAffectedProductIds().size());
                
        try {
            processBulkUpdate(event.getAffectedProductIds(), event.getOperationType());
        } catch (Exception e) {
            log.error("Error processing bulk operation {}: {}", event.getOperationId(), e.getMessage(), e);
        }
    }
    
    /**
     * Processes different types of bulk operations based on operation type.
     *
     * @param productIds List of affected product IDs
     * @param operationType The type of operation performed
     */
    private void processBulkUpdate(List<UUID> productIds, String operationType) {
        switch (operationType.toUpperCase()) {
            case "CATEGORY_ASSIGNMENT":
                processCategoryAssignment(productIds);
                break;
            case "ATTRIBUTE_UPDATE":
                processAttributeUpdate(productIds);
                break;
            case "PUBLICATION_STATUS_CHANGE":
                processPublicationStatusChange(productIds);
                break;
            case "REINDEX":
                processReindexing(productIds);
                break;
            default:
                log.warn("Unknown bulk operation type: {}", operationType);
        }
    }
    
    /**
     * Processes bulk category assignments by recalculating product category paths.
     *
     * @param productIds List of affected product IDs
     */
    private void processCategoryAssignment(List<UUID> productIds) {
        log.info("Processing bulk category assignment for {} products", productIds.size());
        
        for (UUID productId : productIds) {
            try {
                DomainProductEntity product = domainProductService.getProduct(productId);
                
                // Determine primary category ID
                UUID primaryCategoryId = product.getCategories().stream()
                    .filter(DomainProductCategoryEntity::getIsPrimary)
                    .map(DomainProductCategoryEntity::getCategoryId)
                    .findFirst()
                    .orElse(null);
                
                if (primaryCategoryId != null) {
                    DomainCategoryEntity category = domainCategoryService.getCategory(primaryCategoryId);
                    
                    Map<String, Object> changes = new HashMap<>();
                    changes.put("categoryUpdated", true);
                    changes.put("primaryCategoryId", category.getId());
                    changes.put("primaryCategoryPath", category.getPath());
                    
                    // Publish product updated event
                    eventPublisher.publishProductUpdatedEvent(
                        SharedProductUpdatedEvent.fromProduct(product, changes, getCurrentUser())
                    );
                }
            } catch (Exception e) {
                log.error("Error processing category assignment for product {}: {}", productId, e.getMessage());
            }
        }
    }
    
    /**
     * Processes bulk attribute updates by validating product attributes.
     *
     * @param productIds List of affected product IDs
     */
    private void processAttributeUpdate(List<UUID> productIds) {
        log.info("Processing bulk attribute update for {} products", productIds.size());
        
        for (UUID productId : productIds) {
            try {
                // Validate that all required attributes are present
                domainAttributeValueService.validateRequiredAttributes(productId);
                
                DomainProductEntity product = domainProductService.getProduct(productId);
                Map<String, Object> changes = new HashMap<>();
                changes.put("attributesUpdated", true);
                
                // Publish product updated event
                eventPublisher.publishProductUpdatedEvent(
                    SharedProductUpdatedEvent.fromProduct(product, changes, getCurrentUser())
                );
            } catch (Exception e) {
                log.error("Error processing attribute update for product {}: {}", productId, e.getMessage());
            }
        }
    }
    
    /**
     * Processes bulk publication status changes.
     *
     * @param productIds List of affected product IDs
     */
    private void processPublicationStatusChange(List<UUID> productIds) {
        log.info("Processing bulk publication status change for {} products", productIds.size());
        
        for (UUID productId : productIds) {
            try {
                DomainProductEntity product = domainProductService.getProduct(productId);
                Map<String, Object> changes = new HashMap<>();
                changes.put("publicationStatusRevalidated", true);
                changes.put("currentStatus", product.getPublicationStatus().name());
                
                // Publish product updated event
                eventPublisher.publishProductUpdatedEvent(
                    SharedProductUpdatedEvent.fromProduct(product, changes, getCurrentUser())
                );
            } catch (Exception e) {
                log.error("Error processing publication status change for product {}: {}", productId, e.getMessage());
            }
        }
    }
    
    /**
     * Processes bulk reindexing of products - would typically publish events for search indexing.
     *
     * @param productIds List of affected product IDs
     */
    private void processReindexing(List<UUID> productIds) {
        log.info("Processing bulk reindexing for {} products", productIds.size());
        
        for (UUID productId : productIds) {
            try {
                DomainProductEntity product = domainProductService.getProduct(productId);
                Map<String, Object> changes = new HashMap<>();
                changes.put("reindexed", true);
                
                // Publish product updated event
                eventPublisher.publishProductUpdatedEvent(
                    SharedProductUpdatedEvent.fromProduct(product, changes, getCurrentUser())
                );
            } catch (Exception e) {
                log.error("Error processing reindexing for product {}: {}", productId, e.getMessage());
            }
        }
    }
    
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}
