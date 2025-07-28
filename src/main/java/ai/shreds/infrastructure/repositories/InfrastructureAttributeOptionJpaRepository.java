package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureAttributeOptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for attribute option entities.
 * Provides CRUD operations and custom query methods for attribute options.
 */
@Repository
public interface InfrastructureAttributeOptionJpaRepository extends JpaRepository<InfrastructureAttributeOptionJpaEntity, UUID> {

    /**
     * Finds all options for a specific attribute
     * @param attributeId the attribute ID
     * @return list of options for the attribute
     */
    List<InfrastructureAttributeOptionJpaEntity> findByAttributeId(UUID attributeId);

    /**
     * Finds all active options for a specific attribute, ordered by sort order
     * @param attributeId the attribute ID
     * @return list of active options for the attribute
     */
    @Query("SELECT o FROM InfrastructureAttributeOptionJpaEntity o " +
           "WHERE o.attributeId = :attributeId AND o.isActive = true " +
           "ORDER BY o.sortOrder, o.value")
    List<InfrastructureAttributeOptionJpaEntity> findActiveByAttributeId(@Param("attributeId") UUID attributeId);

    /**
     * Finds an option by attribute ID and value
     * @param attributeId the attribute ID
     * @param value the option value
     * @return Optional containing the option if found
     */
    Optional<InfrastructureAttributeOptionJpaEntity> findByAttributeIdAndValue(UUID attributeId, String value);

    /**
     * Finds an option by attribute ID and code
     * @param attributeId the attribute ID
     * @param code the option code
     * @return Optional containing the option if found
     */
    Optional<InfrastructureAttributeOptionJpaEntity> findByAttributeIdAndCode(UUID attributeId, String code);

    /**
     * Checks if an option exists with the given code for an attribute
     * @param attributeId the attribute ID
     * @param code the code to check
     * @return true if an option with the code exists for the attribute
     */
    boolean existsByAttributeIdAndCode(UUID attributeId, String code);

    /**
     * Checks if an option exists with the given value for an attribute
     * @param attributeId the attribute ID
     * @param value the value to check
     * @return true if an option with the value exists for the attribute
     */
    boolean existsByAttributeIdAndValue(UUID attributeId, String value);

    /**
     * Checks if an option exists with the given value for an attribute, excluding a specific option ID
     * @param attributeId the attribute ID
     * @param value the value to check
     * @param id the option ID to exclude
     * @return true if another option with the value exists for the attribute
     */
    boolean existsByAttributeIdAndValueAndIdNot(UUID attributeId, String value, UUID id);

    /**
     * Checks if an option exists with the given code for an attribute, excluding a specific option ID
     * @param attributeId the attribute ID
     * @param code the code to check
     * @param id the option ID to exclude
     * @return true if another option with the code exists for the attribute
     */
    boolean existsByAttributeIdAndCodeAndIdNot(UUID attributeId, String code, UUID id);

    /**
     * Deletes options for a specific attribute
     * @param attributeId the attribute ID
     */
    @Modifying
    @Query("DELETE FROM InfrastructureAttributeOptionJpaEntity o WHERE o.attributeId = :attributeId")
    void deleteByAttributeId(@Param("attributeId") UUID attributeId);

    /**
     * Updates sort order for an option
     * @param id the option ID
     * @param sortOrder the new sort order
     */
    @Modifying
    @Query("UPDATE InfrastructureAttributeOptionJpaEntity o SET o.sortOrder = :sortOrder WHERE o.id = :id")
    void updateSortOrder(@Param("id") UUID id, @Param("sortOrder") Integer sortOrder);

    /**
     * Updates active status for an option
     * @param id the option ID
     * @param isActive the new active status
     */
    @Modifying
    @Query("UPDATE InfrastructureAttributeOptionJpaEntity o SET o.isActive = :isActive WHERE o.id = :id")
    void updateActiveStatus(@Param("id") UUID id, @Param("isActive") Boolean isActive);

    /**
     * Counts options for a specific attribute
     * @param attributeId the attribute ID
     * @return count of options for the attribute
     */
    long countByAttributeId(UUID attributeId);

    /**
     * Finds the maximum sort order for an attribute's options
     * @param attributeId the attribute ID
     * @return the maximum sort order value
     */
    @Query("SELECT MAX(o.sortOrder) FROM InfrastructureAttributeOptionJpaEntity o WHERE o.attributeId = :attributeId")
    Optional<Integer> findMaxSortOrderForAttribute(@Param("attributeId") UUID attributeId);

    /**
     * Finds options by value containing (case-insensitive)
     * @param value the value to search for
     * @return list of options containing the value
     */
    @Query("SELECT o FROM InfrastructureAttributeOptionJpaEntity o WHERE LOWER(o.value) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<InfrastructureAttributeOptionJpaEntity> findByValueContainingIgnoreCase(@Param("value") String value);
}
