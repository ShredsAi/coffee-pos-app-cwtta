package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.dtos.DomainCreateCategoryCommand;
import ai.shreds.domain.dtos.DomainUpdateCategoryCommand;
import ai.shreds.domain.specifications.DomainCategorySpecification;

import java.util.List;
import java.util.UUID;

/**
 * Domain Input Port Category Service
 * Interface for category management operations within the domain
 * Implemented by domain service layer to handle category business logic
 */
public interface DomainInputPortCategoryService {
    
    /**
     * Creates a new category based on the command
     * 
     * @param command The category creation command with all required data
     * @return The created category entity
     * @throws IllegalArgumentException if the command is invalid
     */
    DomainCategoryEntity createCategory(DomainCreateCategoryCommand command);
    
    /**
     * Updates an existing category based on the command
     * 
     * @param command The category update command with fields to update
     * @return The updated category entity
     * @throws IllegalArgumentException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainCategoryNotFoundException if category not found
     */
    DomainCategoryEntity updateCategory(DomainUpdateCategoryCommand command);
    
    /**
     * Retrieves a category by its ID
     * 
     * @param id The category ID to find
     * @return The category entity
     * @throws ai.shreds.domain.exceptions.DomainCategoryNotFoundException if category not found
     */
    DomainCategoryEntity getCategory(UUID id);
    
    /**
     * Finds categories based on a specification
     * 
     * @param specification The filtering/search criteria
     * @return List of matching categories
     */
    List<DomainCategoryEntity> findCategories(DomainCategorySpecification specification);
    
    /**
     * Deletes a category by its ID
     * 
     * @param id The category ID to delete
     * @throws ai.shreds.domain.exceptions.DomainCategoryNotFoundException if category not found
     * @throws ai.shreds.domain.exceptions.DomainCategoryHierarchyException if category has active children or products
     */
    void deleteCategory(UUID id);
    
    /**
     * Recalculates the hierarchy paths and levels for a category and its descendants
     * 
     * @param categoryId The root category ID to start recalculation from
     */
    void recalculateHierarchy(UUID categoryId);
}