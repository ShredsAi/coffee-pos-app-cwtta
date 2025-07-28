package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.enums.DomainPublicationStatus;
import ai.shreds.domain.dtos.DomainCreateProductCommand;
import ai.shreds.domain.dtos.DomainUpdateProductCommand;
import ai.shreds.domain.specifications.DomainProductSpecification;

import java.util.List;
import java.util.UUID;

/**
 * Domain Input Port Product Service
 * Interface for product management operations within the domain
 * Implemented by domain service layer to handle product business logic
 */
public interface DomainInputPortProductService {
    
    /**
     * Creates a new product based on the command
     * 
     * @param command The product creation command with all required data
     * @return The created product entity
     * @throws IllegalArgumentException if the command is invalid
     */
    DomainProductEntity createProduct(DomainCreateProductCommand command);
    
    /**
     * Updates an existing product based on the command
     * 
     * @param command The product update command with fields to update
     * @return The updated product entity
     * @throws IllegalArgumentException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    DomainProductEntity updateProduct(DomainUpdateProductCommand command);
    
    /**
     * Retrieves a product by its ID
     * 
     * @param id The product ID to find
     * @return The product entity
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    DomainProductEntity getProduct(UUID id);
    
    /**
     * Finds products based on a specification
     * 
     * @param specification The filtering/search criteria
     * @return List of matching products
     */
    List<DomainProductEntity> findProducts(DomainProductSpecification specification);
    
    /**
     * Deletes a product by its ID
     * 
     * @param id The product ID to delete
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    void deleteProduct(UUID id);
    
    /**
     * Changes the publication status of a product
     * 
     * @param productId The product ID to update
     * @param newStatus The new publication status
     * @return The updated product entity
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainInvalidStateTransitionException if transition is invalid
     */
    DomainProductEntity changePublicationStatus(UUID productId, DomainPublicationStatus newStatus);
    
    /**
     * Validates that a product meets all requirements for publication
     * 
     * @param productId The product ID to validate
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainAttributeValidationException if validation fails
     * @throws IllegalStateException if product cannot be published
     */
    void validatePublicationRequirements(UUID productId);
}