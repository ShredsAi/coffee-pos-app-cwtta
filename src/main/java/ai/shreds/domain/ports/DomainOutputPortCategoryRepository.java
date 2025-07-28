package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.specifications.DomainCategorySpecification;
import ai.shreds.domain.value_objects.DomainPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Output Port Category Repository
 * Interface for category persistence operations
 * Implemented by infrastructure layer to handle category data access
 */
public interface DomainOutputPortCategoryRepository {
    
    /**
     * Saves a category entity (for create or update)
     * 
     * @param category The category entity to save
     * @return The saved category entity with any generated IDs, timestamps, etc.
     */
    DomainCategoryEntity save(DomainCategoryEntity category);
    
    /**
     * Finds a category by its ID
     * 
     * @param id The category ID to find
     * @return Optional containing the category if found
     */
    Optional<DomainCategoryEntity> findById(UUID id);
    
    /**
     * Finds a category by its slug
     * 
     * @param slug The category slug to find
     * @return Optional containing the category if found
     */
    Optional<DomainCategoryEntity> findBySlug(String slug);
    
    /**
     * Finds categories by parent ID
     * 
     * @param parentId The parent category ID (null for root categories)
     * @return List of child categories
     */
    List<DomainCategoryEntity> findByParentId(UUID parentId);
    
    /**
     * Finds categories based on a specification
     * 
     * @param specification The filtering/search criteria
     * @param page The page number (0-based)
     * @param size The page size
     * @return Page of matching categories
     */
    DomainPage<DomainCategoryEntity> findAll(DomainCategorySpecification specification, Integer page, Integer size);
    
    /**
     * Deletes a category by its ID
     * 
     * @param id The category ID to delete
     */
    void delete(UUID id);
    
    /**
     * Checks if a category exists with the given slug
     * 
     * @param slug The slug to check
     * @return true if a category with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
    
    /**
     * Finds all root categories (categories with no parent)
     * 
     * @return List of root categories
     */
    List<DomainCategoryEntity> findRootCategories();
    
    /**
     * Finds all descendants of a category (recursive)
     * 
     * @param categoryId The parent category ID
     * @return List of all descendant categories
     */
    List<DomainCategoryEntity> findAllDescendants(UUID categoryId);
    
    /**
     * Counts the number of active products in a category
     * 
     * @param categoryId The category ID
     * @return Number of active products associated with this category
     */
    long countActiveProductsInCategory(UUID categoryId);
}