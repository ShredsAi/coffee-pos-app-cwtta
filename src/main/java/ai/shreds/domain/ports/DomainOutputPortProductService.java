package ai.shreds.domain.ports;

import ai.shreds.domain.value_objects.DomainProductIdValue;

import java.util.List;
import java.util.Map;

/**
 * Output port for product service operations.
 * Defines the contract for integration with external product catalog service.
 * To be implemented by infrastructure layer.
 * 
 * This port allows the domain to validate products and retrieve product information
 * without depending on external service implementation details.
 */
public interface DomainOutputPortProductService {
    
    /**
     * Validates that a product exists and is active for inventory operations.
     * Should check both existence and operational status of the product.
     * 
     * @param productId the product ID to validate
     * @return true if the product exists and is active for inventory operations
     * @throws ai.shreds.domain.exceptions.DomainValidationException if productId is null or invalid
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean validateProduct(DomainProductIdValue productId);
    
    /**
     * Gets comprehensive product details from the product catalog.
     * Returns essential product information needed for inventory management.
     * 
     * @param productId the product ID
     * @return map of product details including name, description, default unit, category, etc.
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<String, Object> getProductDetails(DomainProductIdValue productId);
    
    /**
     * Gets the default unit of measure for a product.
     * Used to ensure consistency in inventory operations.
     * 
     * @param productId the product ID
     * @return the default unit of measure (e.g., "pieces", "kg", "liters")
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getDefaultUnitOfMeasure(DomainProductIdValue productId);
    
    /**
     * Gets the product name for display and reporting purposes.
     * 
     * @param productId the product ID
     * @return the product name
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getProductName(DomainProductIdValue productId);
    
    /**
     * Checks if a product requires batch tracking.
     * Some products may require batch-level tracking for quality control or regulatory compliance.
     * 
     * @param productId the product ID
     * @return true if the product requires batch tracking
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean requiresBatchTracking(DomainProductIdValue productId);
    
    /**
     * Checks if a product has an expiration date.
     * Used to determine if batch expiration dates should be tracked.
     * 
     * @param productId the product ID
     * @return true if the product can expire
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean isPerishable(DomainProductIdValue productId);
    
    /**
     * Gets the product category for classification and reporting.
     * 
     * @param productId the product ID
     * @return the product category
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getProductCategory(DomainProductIdValue productId);
    
    /**
     * Validates multiple products in a single call for better performance.
     * 
     * @param productIds the list of product IDs to validate
     * @return map of product ID to validation result (true if valid)
     * @throws ai.shreds.domain.exceptions.DomainValidationException if productIds list is null or empty
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<DomainProductIdValue, Boolean> validateMultipleProducts(List<DomainProductIdValue> productIds);
    
    /**
     * Gets basic product information for multiple products.
     * Efficient for bulk operations.
     * 
     * @param productIds the list of product IDs
     * @return map of product ID to basic product information
     * @throws ai.shreds.domain.exceptions.DomainValidationException if productIds list is null or empty
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<DomainProductIdValue, Map<String, Object>> getMultipleProductDetails(List<DomainProductIdValue> productIds);
    
    /**
     * Checks if a product is discontinued or obsolete.
     * 
     * @param productId the product ID
     * @return true if the product is discontinued
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean isDiscontinued(DomainProductIdValue productId);
    
    /**
     * Gets allowed units of measure for a product.
     * Some products may have multiple valid units (e.g., pieces, boxes, pallets).
     * 
     * @param productId the product ID
     * @return list of valid units of measure for the product
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    List<String> getAllowedUnitsOfMeasure(DomainProductIdValue productId);
    
    /**
     * Validates that a unit of measure is valid for the given product.
     * 
     * @param productId the product ID
     * @param unit the unit to validate
     * @return true if the unit is valid for this product
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if product not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean isValidUnitForProduct(DomainProductIdValue productId, String unit);
    
    /**
     * Checks if the external product service is available.
     * Used for health checks and graceful degradation.
     * 
     * @return true if the service is available and responding
     */
    boolean isServiceAvailable();
    
    /**
     * Gets the service health status with additional details.
     * 
     * @return map containing service health information
     */
    Map<String, Object> getServiceHealthStatus();
}