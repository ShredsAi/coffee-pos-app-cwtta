package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.domain.dtos.DomainCreateAttributeCommand;
import ai.shreds.domain.dtos.DomainUpdateAttributeCommand;
import ai.shreds.domain.dtos.DomainCreateOptionCommand;
import ai.shreds.domain.specifications.DomainAttributeSpecification;

import java.util.List;
import java.util.UUID;

/**
 * Domain Input Port Attribute Definition Service
 * Interface for attribute definition management operations within the domain
 * Implemented by domain service layer to handle attribute business logic
 */
public interface DomainInputPortAttributeDefinitionService {
    
    /**
     * Creates a new attribute definition based on the command
     * 
     * @param command The attribute creation command with all required data
     * @return The created attribute entity
     * @throws IllegalArgumentException if the command is invalid
     */
    DomainProductAttributeEntity createAttribute(DomainCreateAttributeCommand command);
    
    /**
     * Updates an existing attribute definition based on the command
     * 
     * @param command The attribute update command with fields to update
     * @return The updated attribute entity
     * @throws IllegalArgumentException if the command is invalid
     * @throws ai.shreds.domain.exceptions.DomainAttributeNotFoundException if attribute not found
     */
    DomainProductAttributeEntity updateAttribute(DomainUpdateAttributeCommand command);
    
    /**
     * Retrieves an attribute by its ID
     * 
     * @param id The attribute ID to find
     * @return The attribute entity
     * @throws ai.shreds.domain.exceptions.DomainAttributeNotFoundException if attribute not found
     */
    DomainProductAttributeEntity getAttribute(UUID id);
    
    /**
     * Finds attributes based on a specification
     * 
     * @param specification The filtering/search criteria
     * @return List of matching attributes
     */
    List<DomainProductAttributeEntity> findAttributes(DomainAttributeSpecification specification);
    
    /**
     * Deletes an attribute by its ID
     * 
     * @param id The attribute ID to delete
     * @throws ai.shreds.domain.exceptions.DomainAttributeNotFoundException if attribute not found
     * @throws ai.shreds.domain.exceptions.DomainAttributeValidationException if attribute is in use
     */
    void deleteAttribute(UUID id);
    
    /**
     * Adds an option to a select-type attribute
     * 
     * @param command The option creation command
     * @return The created option entity
     * @throws ai.shreds.domain.exceptions.DomainAttributeNotFoundException if attribute not found
     * @throws IllegalArgumentException if attribute is not select type
     */
    DomainAttributeOptionEntity addOption(DomainCreateOptionCommand command);
}