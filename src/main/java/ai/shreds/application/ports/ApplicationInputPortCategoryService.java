package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedCategoryDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateCategoryCommand;
import ai.shreds.shared.dtos.ApplicationUpdateCategoryCommand;
import ai.shreds.application.services.ApplicationCategoryFilterSpecification;

import java.util.UUID;

/**
 * Input port for category-related operations in the application layer.
 * Defines the contract for category management use cases.
 */
public interface ApplicationInputPortCategoryService {
    
    /**
     * Creates a new category based on the provided command.
     * 
     * @param command the command containing category creation data
     * @return the created category as DTO
     */
    SharedCategoryDTO createCategory(ApplicationCreateCategoryCommand command);
    
    /**
     * Updates an existing category based on the provided command.
     * 
     * @param command the command containing category update data
     * @return the updated category as DTO
     */
    SharedCategoryDTO updateCategory(ApplicationUpdateCategoryCommand command);
    
    /**
     * Retrieves a category by its unique identifier.
     * 
     * @param id the category identifier
     * @return the category as DTO
     */
    SharedCategoryDTO getCategory(UUID id);
    
    /**
     * Lists categories based on the provided filter specification.
     * 
     * @param specification the filter and pagination specification
     * @return paged response containing matching categories
     */
    SharedPagedResponse<SharedCategoryDTO> listCategories(ApplicationCategoryFilterSpecification specification);
    
    /**
     * Deletes a category by its unique identifier.
     * 
     * @param id the category identifier to delete
     */
    void deleteCategory(UUID id);
}