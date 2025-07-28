package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductAttributeValueEntity;

import java.util.List;
import java.util.UUID;

/**
 * Domain Input Port Attribute Value Service
 * Interface for product attribute value management operations within the domain
 * Implemented by domain service layer to handle attribute value business logic
 */
public interface DomainInputPortAttributeValueService {
    
    /**
     * Assigns attribute values to a product
     * 
     * @param productId The product ID to assign values to
     * @param values List of attribute values to assign
     * @throws IllegalArgumentException if productId is null or values are invalid
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainAttributeValidationException if values don't match attribute definitions
     */
    void assignValues(UUID productId, List<DomainProductAttributeValueEntity> values);
    
    /**
     * Updates a specific attribute value
     * 
     * @param valueId The ID of the attribute value to update
     * @param newValue The new value to set
     * @return The updated attribute value entity
     * @throws ai.shreds.domain.exceptions.DomainAttributeValueNotFoundException if value not found
     * @throws ai.shreds.domain.exceptions.DomainAttributeValidationException if new value is invalid
     */
    DomainProductAttributeValueEntity updateValue(UUID valueId, Object newValue);
    
    /**
     * Removes an attribute value from a product
     * 
     * @param productId The product ID to remove value from
     * @param attributeId The attribute ID to remove
     * @throws IllegalArgumentException if productId or attributeId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    void removeValue(UUID productId, UUID attributeId);
    
    /**
     * Validates that all required attributes have values for a product
     * 
     * @param productId The product ID to validate
     * @throws IllegalArgumentException if productId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainAttributeValidationException if required attributes are missing
     */
    void validateRequiredAttributes(UUID productId);
    
    /**
     * Gets all attribute values for a product
     * 
     * @param productId The product ID to get values for
     * @return List of attribute values
     * @throws IllegalArgumentException if productId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    List<DomainProductAttributeValueEntity> getProductAttributeValues(UUID productId);
    
    /**
     * Gets a specific attribute value for a product
     * 
     * @param productId The product ID
     * @param attributeId The attribute ID
     * @return The attribute value entity if found
     * @throws IllegalArgumentException if productId or attributeId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    DomainProductAttributeValueEntity getProductAttributeValue(UUID productId, UUID attributeId);
}