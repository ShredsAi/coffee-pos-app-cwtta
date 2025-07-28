package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedProductAttributeDTO;
import ai.shreds.shared.dtos.SharedAttributeOptionDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationUpdateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationCreateAttributeOptionCommand;
import ai.shreds.application.services.ApplicationAttributeFilterSpecification;

import java.util.UUID;

/**
 * Input port for attribute-related operations in the application layer.
 * Defines the contract for product attribute management use cases.
 */
public interface ApplicationInputPortAttributeService {
    
    /**
     * Creates a new product attribute based on the provided command.
     * 
     * @param command the command containing attribute creation data
     * @return the created attribute as DTO
     */
    SharedProductAttributeDTO createAttribute(ApplicationCreateAttributeCommand command);
    
    /**
     * Updates an existing product attribute based on the provided command.
     * 
     * @param command the command containing attribute update data
     * @return the updated attribute as DTO
     */
    SharedProductAttributeDTO updateAttribute(ApplicationUpdateAttributeCommand command);
    
    /**
     * Retrieves a product attribute by its unique identifier.
     * 
     * @param id the attribute identifier
     * @return the attribute as DTO
     */
    SharedProductAttributeDTO getAttribute(UUID id);
    
    /**
     * Lists product attributes based on the provided filter specification.
     * 
     * @param specification the filter and pagination specification
     * @return paged response containing matching attributes
     */
    SharedPagedResponse<SharedProductAttributeDTO> listAttributes(ApplicationAttributeFilterSpecification specification);
    
    /**
     * Deletes a product attribute by its unique identifier.
     * 
     * @param id the attribute identifier to delete
     */
    void deleteAttribute(UUID id);
    
    /**
     * Adds an option to an existing attribute.
     * 
     * @param command the command containing option creation data
     * @return the created attribute option as DTO
     */
    SharedAttributeOptionDTO addAttributeOption(ApplicationCreateAttributeOptionCommand command);
}