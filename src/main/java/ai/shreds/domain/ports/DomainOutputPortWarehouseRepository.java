package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainWarehouseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Output port for warehouse repository operations.
 * Defines the contract for warehouse data persistence to be implemented by infrastructure layer.
 * 
 * Note: This interface follows domain-driven design principles where the domain defines
 * the contract and infrastructure implements it.
 */
public interface DomainOutputPortWarehouseRepository {
    
    /**
     * Saves a warehouse entity (create or update).
     * Handles both new warehouse creation and existing warehouse updates.
     * 
     * @param warehouse the warehouse entity to save
     * @return the saved warehouse entity with any generated/updated fields
     * @throws ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException if save fails
     */
    DomainWarehouseEntity save(DomainWarehouseEntity warehouse);
    
    /**
     * Finds a warehouse by its unique identifier.
     * 
     * @param id the warehouse ID
     * @return the warehouse entity, or null if not found
     * @throws ai.shreds.domain.exceptions.DomainValidationException if id is null
     */
    DomainWarehouseEntity findById(UUID id);
    
    /**
     * Checks if a warehouse exists with the specified code.
     * Used for uniqueness validation during warehouse creation.
     * 
     * @param code the warehouse code to check
     * @return true if a warehouse with this code exists, false otherwise
     */
    boolean existsByCode(String code);
    
    /**
     * Finds a warehouse by its business code.
     * 
     * @param code the warehouse code
     * @return the warehouse entity, or null if not found
     */
    DomainWarehouseEntity findByCode(String code);
    
    /**
     * Finds all active warehouses.
     * Useful for operational queries where only active warehouses should be considered.
     * 
     * @return list of active warehouses
     */
    List<DomainWarehouseEntity> findAllActive();
    
    /**
     * Finds all warehouses regardless of status.
     * 
     * @return list of all warehouses
     */
    List<DomainWarehouseEntity> findAll();
    
    /**
     * Deletes a warehouse by its ID.
     * Note: This should be used carefully as it may impact referential integrity.
     * 
     * @param id the warehouse ID to delete
     * @return true if warehouse was deleted, false if it didn't exist
     */
    boolean deleteById(UUID id);
    
    /**
     * Finds warehouses by status (active/inactive).
     * 
     * @param isActive the status to filter by
     * @return list of warehouses with the specified status
     */
    List<DomainWarehouseEntity> findByStatus(boolean isActive);
    
    /**
     * Counts total number of warehouses.
     * 
     * @return the total count of warehouses
     */
    long count();
    
    /**
     * Counts warehouses by status.
     * 
     * @param isActive the status to count
     * @return the count of warehouses with the specified status
     */
    long countByStatus(boolean isActive);
    
    /**
     * Checks if a warehouse exists by ID.
     * 
     * @param id the warehouse ID
     * @return true if warehouse exists, false otherwise
     */
    boolean existsById(UUID id);
    
    /**
     * Finds warehouses containing the specified name (case-insensitive search).
     * Useful for search functionality.
     * 
     * @param namePart part of the warehouse name to search for
     * @return list of warehouses whose names contain the specified text
     */
    List<DomainWarehouseEntity> findByNameContainingIgnoreCase(String namePart);
    
    /**
     * Finds warehouses by country.
     * 
     * @param country the country to filter by
     * @return list of warehouses in the specified country
     */
    List<DomainWarehouseEntity> findByCountry(String country);
    
    /**
     * Finds warehouses by city.
     * 
     * @param city the city to filter by
     * @return list of warehouses in the specified city
     */
    List<DomainWarehouseEntity> findByCity(String city);
}