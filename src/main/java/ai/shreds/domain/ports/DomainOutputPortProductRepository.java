package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.specifications.DomainProductSpecification;
import ai.shreds.domain.value_objects.DomainPage;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Output Port Product Repository
 * Interface for product persistence operations
 * Implemented by infrastructure layer to handle product data access
 */
public interface DomainOutputPortProductRepository {
    
    /**
     * Saves a product entity (for create or update)
     * 
     * @param product The product entity to save
     * @return The saved product entity with any generated IDs, timestamps, etc.
     */
    DomainProductEntity save(DomainProductEntity product);
    
    /**
     * Finds a product by its ID
     * 
     * @param id The product ID to find
     * @return Optional containing the product if found
     */
    Optional<DomainProductEntity> findById(UUID id);
    
    /**
     * Finds a product by its SKU
     * 
     * @param sku The product SKU to find
     * @return Optional containing the product if found
     */
    Optional<DomainProductEntity> findBySku(String sku);
    
    /**
     * Finds a product by its slug
     * 
     * @param slug The product slug to find
     * @return Optional containing the product if found
     */
    Optional<DomainProductEntity> findBySlug(String slug);
    
    /**
     * Finds products based on a specification
     * 
     * @param specification The filtering/search criteria
     * @param page The page number (0-based)
     * @param size The page size
     * @return Page of matching products
     */
    DomainPage<DomainProductEntity> findAll(DomainProductSpecification specification, Integer page, Integer size);
    
    /**
     * Deletes a product by its ID
     * 
     * @param id The product ID to delete
     */
    void delete(UUID id);
    
    /**
     * Checks if a product exists with the given SKU
     * 
     * @param sku The SKU to check
     * @return true if a product with the SKU exists, false otherwise
     */
    boolean existsBySku(String sku);
    
    /**
     * Checks if a product exists with the given slug
     * 
     * @param slug The slug to check
     * @return true if a product with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}