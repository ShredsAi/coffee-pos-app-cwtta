package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductAttributeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product attribute entities.
 * Provides CRUD operations and custom query methods for product attributes.
 */
@Repository
public interface InfrastructureAttributeJpaRepository extends 
        JpaRepository<InfrastructureProductAttributeJpaEntity, UUID>, 
        JpaSpecificationExecutor<InfrastructureProductAttributeJpaEntity> {

    /**
     * Finds an attribute by its code
     * @param code the attribute code
     * @return Optional containing the attribute if found
     */
    Optional<InfrastructureProductAttributeJpaEntity> findByCode(String code);

    /**
     * Checks if an attribute exists with the given code
     * @param code the code to check
     * @return true if an attribute with the code exists
     */
    boolean existsByCode(String code);

    /**
     * Checks if an attribute exists with the given code, excluding a specific attribute ID
     * @param code the code to check
     * @param id the attribute ID to exclude
     * @return true if another attribute with the code exists
     */
    boolean existsByCodeAndIdNot(String code, UUID id);

    /**
     * Finds attributes by type
     * @param attributeType the attribute type
     * @return list of attributes of the specified type
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.attributeType = :type ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findByAttributeType(@Param("type") InfrastructureProductAttributeJpaEntity.AttributeType attributeType);

    /**
     * Finds all active attributes ordered by sort order and name
     * @return list of active attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findActiveAttributes();

    /**
     * Finds all filterable attributes
     * @return list of filterable attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.isFilterable = true AND a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findFilterableAttributes();

    /**
     * Finds all searchable attributes
     * @return list of searchable attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.isSearchable = true AND a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findSearchableAttributes();

    /**
     * Finds all required attributes
     * @return list of required attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.isRequired = true AND a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findRequiredAttributes();

    /**
     * Finds attributes by name (case-insensitive search)
     * @param name the name to search for
     * @return list of attributes matching the name
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<InfrastructureProductAttributeJpaEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds select-type attributes (single or multiple)
     * @return list of select-type attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.attributeType IN ('SELECT_SINGLE', 'SELECT_MULTIPLE') AND a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findSelectTypeAttributes();

    /**
     * Finds numeric-type attributes
     * @return list of numeric attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.attributeType = 'NUMBER' AND a.isActive = true ORDER BY a.sortOrder, a.name")
    List<InfrastructureProductAttributeJpaEntity> findNumericAttributes();

    /**
     * Finds attributes with specific unit
     * @param unit the unit to search for
     * @return list of attributes with the specified unit
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.unit = :unit AND a.isActive = true")
    List<InfrastructureProductAttributeJpaEntity> findByUnit(@Param("unit") String unit);

    /**
     * Counts attributes by type
     * @param attributeType the attribute type
     * @return count of attributes of the specified type
     */
    @Query("SELECT COUNT(a) FROM InfrastructureProductAttributeJpaEntity a WHERE a.attributeType = :type")
    long countByAttributeType(@Param("type") InfrastructureProductAttributeJpaEntity.AttributeType attributeType);

    /**
     * Finds attributes used by products (have values assigned)
     * @return list of attributes that are in use
     */
    @Query("SELECT DISTINCT a FROM InfrastructureProductAttributeJpaEntity a " +
           "JOIN InfrastructureProductAttributeValueJpaEntity v ON a.id = v.attributeId " +
           "WHERE a.isActive = true")
    List<InfrastructureProductAttributeJpaEntity> findAttributesInUse();

    /**
     * Finds unused attributes (not assigned to any product)
     * @return list of attributes that are not in use
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a " +
           "WHERE a.isActive = true AND a.id NOT IN (" +
           "SELECT DISTINCT v.attributeId FROM InfrastructureProductAttributeValueJpaEntity v" +
           ")")
    List<InfrastructureProductAttributeJpaEntity> findUnusedAttributes();

    /**
     * Finds the maximum sort order
     * @return the maximum sort order value
     */
    @Query("SELECT MAX(a.sortOrder) FROM InfrastructureProductAttributeJpaEntity a")
    Optional<Integer> findMaxSortOrder();

    /**
     * Finds attributes needing validation (recently changed required/type settings)
     * @param since the timestamp to check updates since
     * @return list of recently updated attributes
     */
    @Query("SELECT a FROM InfrastructureProductAttributeJpaEntity a WHERE a.updatedAt > :since")
    List<InfrastructureProductAttributeJpaEntity> findUpdatedSince(@Param("since") java.time.Instant since);
}
