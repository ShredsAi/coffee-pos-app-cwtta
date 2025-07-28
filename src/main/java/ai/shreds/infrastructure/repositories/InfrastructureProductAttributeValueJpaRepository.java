package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductAttributeValueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product attribute value entities.
 * Provides CRUD operations and custom query methods for product attribute values.
 */
@Repository
public interface InfrastructureProductAttributeValueJpaRepository extends JpaRepository<InfrastructureProductAttributeValueJpaEntity, UUID> {

    /**
     * Finds all attribute values for a specific product
     * @param productId the product ID
     * @return list of attribute values for the product
     */
    List<InfrastructureProductAttributeValueJpaEntity> findByProductId(UUID productId);

    /**
     * Finds all active attribute values for a specific product
     * @param productId the product ID
     * @return list of active attribute values for the product
     */
    @Query("SELECT v FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.productId = :productId AND v.isActive = true")
    List<InfrastructureProductAttributeValueJpaEntity> findActiveByProductId(@Param("productId") UUID productId);

    /**
     * Finds attribute value by product and attribute
     * @param productId the product ID
     * @param attributeId the attribute ID
     * @return Optional containing the attribute value if found
     */
    Optional<InfrastructureProductAttributeValueJpaEntity> findByProductIdAndAttributeId(UUID productId, UUID attributeId);

    /**
     * Finds all values for a specific attribute across all products
     * @param attributeId the attribute ID
     * @return list of attribute values
     */
    List<InfrastructureProductAttributeValueJpaEntity> findByAttributeId(UUID attributeId);

    /**
     * Deletes all attribute values for a product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureProductAttributeValueJpaEntity v WHERE v.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Deletes attribute value by product and attribute
     * @param productId the product ID
     * @param attributeId the attribute ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureProductAttributeValueJpaEntity v WHERE v.productId = :productId AND v.attributeId = :attributeId")
    void deleteByProductIdAndAttributeId(@Param("productId") UUID productId, @Param("attributeId") UUID attributeId);

    /**
     * Finds products with specific text attribute value
     * @param attributeId the attribute ID
     * @param textValue the text value to match
     * @return list of product IDs with the specified text value
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.textValue = :textValue AND v.isActive = true")
    List<UUID> findProductIdsByTextValue(@Param("attributeId") UUID attributeId, @Param("textValue") String textValue);

    /**
     * Finds products with specific numeric attribute value
     * @param attributeId the attribute ID
     * @param numericValue the numeric value to match
     * @return list of product IDs with the specified numeric value
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.numericValue = :numericValue AND v.isActive = true")
    List<UUID> findProductIdsByNumericValue(@Param("attributeId") UUID attributeId, @Param("numericValue") BigDecimal numericValue);

    /**
     * Finds products with specific boolean attribute value
     * @param attributeId the attribute ID
     * @param booleanValue the boolean value to match
     * @return list of product IDs with the specified boolean value
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.booleanValue = :booleanValue AND v.isActive = true")
    List<UUID> findProductIdsByBooleanValue(@Param("attributeId") UUID attributeId, @Param("booleanValue") Boolean booleanValue);

    /**
     * Finds products with specific date attribute value
     * @param attributeId the attribute ID
     * @param dateValue the date value to match
     * @return list of product IDs with the specified date value
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.dateValue = :dateValue AND v.isActive = true")
    List<UUID> findProductIdsByDateValue(@Param("attributeId") UUID attributeId, @Param("dateValue") LocalDate dateValue);

    /**
     * Finds products with numeric values in a range
     * @param attributeId the attribute ID
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @return list of product IDs with values in the specified range
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.numericValue BETWEEN :minValue AND :maxValue AND v.isActive = true")
    List<UUID> findProductIdsByNumericRange(@Param("attributeId") UUID attributeId, @Param("minValue") BigDecimal minValue, @Param("maxValue") BigDecimal maxValue);

    /**
     * Finds products with text values containing a search term (case-insensitive)
     * @param attributeId the attribute ID
     * @param searchTerm the search term
     * @return list of product IDs with text values containing the search term
     */
    @Query("SELECT v.productId FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND LOWER(v.textValue) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND v.isActive = true")
    List<UUID> findProductIdsByTextSearch(@Param("attributeId") UUID attributeId, @Param("searchTerm") String searchTerm);

    /**
     * Counts attribute values for a specific attribute
     * @param attributeId the attribute ID
     * @return count of attribute values
     */
    long countByAttributeId(UUID attributeId);

    /**
     * Checks if a product has any attribute values
     * @param productId the product ID
     * @return true if the product has attribute values
     */
    boolean existsByProductId(UUID productId);

    /**
     * Checks if a specific attribute is used by any product
     * @param attributeId the attribute ID
     * @return true if the attribute is used
     */
    boolean existsByAttributeId(UUID attributeId);

    /**
     * Finds distinct text values for an attribute
     * @param attributeId the attribute ID
     * @return list of distinct text values
     */
    @Query("SELECT DISTINCT v.textValue FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.textValue IS NOT NULL AND v.isActive = true " +
           "ORDER BY v.textValue")
    List<String> findDistinctTextValues(@Param("attributeId") UUID attributeId);

    /**
     * Finds min and max numeric values for an attribute
     * @param attributeId the attribute ID
     * @return array with [min, max] values
     */
    @Query("SELECT MIN(v.numericValue), MAX(v.numericValue) FROM InfrastructureProductAttributeValueJpaEntity v " +
           "WHERE v.attributeId = :attributeId AND v.numericValue IS NOT NULL AND v.isActive = true")
    Object[] findNumericValueRange(@Param("attributeId") UUID attributeId);

    /**
     * Updates the active status of attribute values for a product
     * @param productId the product ID
     * @param isActive the new active status
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductAttributeValueJpaEntity v SET v.isActive = :isActive WHERE v.productId = :productId")
    void updateActiveStatusByProductId(@Param("productId") UUID productId, @Param("isActive") Boolean isActive);
}
