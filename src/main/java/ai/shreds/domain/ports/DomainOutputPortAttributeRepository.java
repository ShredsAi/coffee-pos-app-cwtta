package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.domain.specifications.DomainAttributeSpecification;
import ai.shreds.domain.value_objects.DomainPage;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Output Port Attribute Repository
 * Interface for attribute persistence operations
 * Implemented by infrastructure layer to handle attribute data access
 */
public interface DomainOutputPortAttributeRepository {
    
    /**
     * Saves an attribute entity (for create or update)
     * 
     * @param attribute The attribute entity to save
     * @return The saved attribute entity with any generated IDs, timestamps, etc.
     */
    DomainProductAttributeEntity save(DomainProductAttributeEntity attribute);
    
    /**
     * Saves an attribute option entity
     * 
     * @param option The option entity to save
     * @return The saved option entity with any generated IDs, timestamps, etc.
     */
    DomainAttributeOptionEntity saveOption(DomainAttributeOptionEntity option);
    
    /**
     * Finds an attribute by its ID
     * 
     * @param id The attribute ID to find
     * @return Optional containing the attribute if found
     */
    Optional<DomainProductAttributeEntity> findById(UUID id);
    
    /**
     * Finds an attribute by its code
     * 
     * @param code The attribute code to find
     * @return Optional containing the attribute if found
     */
    Optional<DomainProductAttributeEntity> findByCode(String code);
    
    /**
     * Finds attributes based on a specification
     * 
     * @param specification The filtering/search criteria
     * @param page The page number (0-based)
     * @param size The page size
     * @return Page of matching attributes
     */
    DomainPage<DomainProductAttributeEntity> findAll(DomainAttributeSpecification specification, Integer page, Integer size);
    
    /**
     * Deletes an attribute by its ID
     * 
     * @param id The attribute ID to delete
     */
    void delete(UUID id);
    
    /**
     * Checks if an attribute exists with the given code
     * 
     * @param code The code to check
     * @return true if an attribute with the code exists, false otherwise
     */
    boolean existsByCode(String code);
    
    /**
     * Deletes an attribute option by its ID
     * 
     * @param optionId The option ID to delete
     */
    void deleteOption(UUID optionId);
    
    /**
     * Checks if an attribute is used by any products
     * 
     * @param attributeId The attribute ID to check
     * @return true if the attribute is used by products, false otherwise
     */
    boolean isAttributeInUse(UUID attributeId);
}