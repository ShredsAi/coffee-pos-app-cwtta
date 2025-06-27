package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.repositories.entities.InfrastructureWarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for warehouse operations.
 * Provides CRUD operations and custom queries for warehouse entities.
 */
@Repository
public interface InfrastructureWarehouseSpringDataRepository extends JpaRepository<InfrastructureWarehouseJpaEntity, UUID> {
    
    /**
     * Finds a warehouse entity by its code.
     * @param code the warehouse code
     * @return the warehouse if found
     */
    Optional<InfrastructureWarehouseJpaEntity> findByCode(String code);
    
    /**
     * Checks if a warehouse exists with the given code.
     * @param code the warehouse code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Finds all active warehouses.
     * @return list of active warehouses
     */
    List<InfrastructureWarehouseJpaEntity> findByIsActiveTrue();
    
    /**
     * Finds all warehouses by status.
     * @param isActive the status to filter by
     * @return list of warehouses with the specified status
     */
    List<InfrastructureWarehouseJpaEntity> findByIsActive(boolean isActive);
    
    /**
     * Counts warehouses by status.
     * @param isActive the status to count
     * @return the count of warehouses with the specified status
     */
    long countByIsActive(boolean isActive);
    
    /**
     * Finds warehouses containing the specified name (case-insensitive search).
     * @param namePart part of the warehouse name to search for
     * @return list of warehouses whose names contain the specified text
     */
    List<InfrastructureWarehouseJpaEntity> findByNameContainingIgnoreCase(String namePart);
    
    /**
     * Finds warehouses by country.
     * @param country the country to filter by
     * @return list of warehouses in the specified country
     */
    List<InfrastructureWarehouseJpaEntity> findByCountry(String country);
    
    /**
     * Finds warehouses by city.
     * @param city the city to filter by
     * @return list of warehouses in the specified city
     */
    List<InfrastructureWarehouseJpaEntity> findByCity(String city);
    
    /**
     * Finds warehouses by city and country.
     * @param city the city to filter by
     * @param country the country to filter by
     * @return list of warehouses in the specified city and country
     */
    List<InfrastructureWarehouseJpaEntity> findByCityAndCountry(String city, String country);
    
    /**
     * Finds warehouses in a specific country that are active.
     * @param country the country to filter by
     * @return list of active warehouses in the specified country
     */
    List<InfrastructureWarehouseJpaEntity> findByCountryAndIsActiveTrue(String country);
    
    /**
     * Custom query to find warehouses by multiple criteria.
     * @param isActive the status filter
     * @param country the country filter (optional)
     * @param city the city filter (optional)
     * @return list of warehouses matching the criteria
     */
    @Query("SELECT w FROM InfrastructureWarehouseJpaEntity w WHERE " +
           "(:isActive IS NULL OR w.isActive = :isActive) AND " +
           "(:country IS NULL OR LOWER(w.country) = LOWER(:country)) AND " +
           "(:city IS NULL OR LOWER(w.city) = LOWER(:city))")
    List<InfrastructureWarehouseJpaEntity> findByCriteria(
            @Param("isActive") Boolean isActive,
            @Param("country") String country,
            @Param("city") String city
    );
    
    /**
     * Finds warehouses ordered by creation date.
     * @return list of warehouses ordered by creation date descending
     */
    List<InfrastructureWarehouseJpaEntity> findAllByOrderByCreatedAtDesc();
    
    /**
     * Finds warehouses updated after a specific date.
     * @param updatedAfter the date to filter by
     * @return list of warehouses updated after the specified date
     */
    @Query("SELECT w FROM InfrastructureWarehouseJpaEntity w WHERE w.updatedAt > :updatedAfter")
    List<InfrastructureWarehouseJpaEntity> findUpdatedAfter(@Param("updatedAfter") java.time.LocalDateTime updatedAfter);
}
