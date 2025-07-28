package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product entities.
 * Provides CRUD operations and custom query methods for products.
 */
@Repository
public interface InfrastructureProductJpaRepository extends 
        JpaRepository<InfrastructureProductJpaEntity, UUID>, 
        JpaSpecificationExecutor<InfrastructureProductJpaEntity> {

    /**
     * Finds a product by its SKU
     * @param sku the product SKU
     * @return Optional containing the product if found
     */
    Optional<InfrastructureProductJpaEntity> findBySku(String sku);

    /**
     * Finds a product by its slug
     * @param slug the product slug
     * @return Optional containing the product if found
     */
    Optional<InfrastructureProductJpaEntity> findBySlug(String slug);

    /**
     * Checks if a product exists with the given SKU
     * @param sku the SKU to check
     * @return true if a product with the SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Checks if a product exists with the given slug
     * @param slug the slug to check
     * @return true if a product with the slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if a product exists with the given SKU, excluding a specific product ID
     * @param sku the SKU to check
     * @param id the product ID to exclude
     * @return true if another product with the SKU exists
     */
    boolean existsBySkuAndIdNot(String sku, UUID id);

    /**
     * Checks if a product exists with the given slug, excluding a specific product ID
     * @param slug the slug to check
     * @param id the product ID to exclude
     * @return true if another product with the slug exists
     */
    boolean existsBySlugAndIdNot(String slug, UUID id);

    /**
     * Finds products by brand
     * @param brand the brand to search for
     * @return list of products with the specified brand
     */
    @Query("SELECT p FROM InfrastructureProductJpaEntity p WHERE p.brand = :brand AND p.isActive = true")
    java.util.List<InfrastructureProductJpaEntity> findByBrandAndActive(@Param("brand") String brand);

    /**
     * Finds products by publication status
     * @param publicationStatus the publication status
     * @return list of products with the specified status
     */
    @Query("SELECT p FROM InfrastructureProductJpaEntity p WHERE p.publicationStatus = :status")
    java.util.List<InfrastructureProductJpaEntity> findByPublicationStatus(@Param("status") String publicationStatus);

    /**
     * Finds active products in specific categories
     * @param categoryId the category ID
     * @return list of active products in the category
     */
    @Query("SELECT DISTINCT p FROM InfrastructureProductJpaEntity p " +
           "JOIN p.categories pc " +
           "WHERE pc.categoryId = :categoryId AND p.isActive = true")
    java.util.List<InfrastructureProductJpaEntity> findActiveProductsByCategory(@Param("categoryId") UUID categoryId);

    /**
     * Full-text search on product name and description
     * @param searchTerm the search term
     * @return list of products matching the search term
     */
    @Query("SELECT p FROM InfrastructureProductJpaEntity p " +
           "WHERE p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
           ")")
    java.util.List<InfrastructureProductJpaEntity> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * Finds products with specific attribute values
     * @param attributeId the attribute ID
     * @param textValue the text value to match
     * @return list of products with the specified attribute value
     */
    @Query("SELECT DISTINCT p FROM InfrastructureProductJpaEntity p " +
           "JOIN p.attributes a " +
           "WHERE a.attributeId = :attributeId AND a.textValue = :textValue AND p.isActive = true")
    java.util.List<InfrastructureProductJpaEntity> findByAttributeTextValue(
            @Param("attributeId") UUID attributeId, 
            @Param("textValue") String textValue);

    /**
     * Counts products by publication status
     * @param publicationStatus the publication status
     * @return count of products with the status
     */
    @Query("SELECT COUNT(p) FROM InfrastructureProductJpaEntity p WHERE p.publicationStatus = :status")
    long countByPublicationStatus(@Param("status") String publicationStatus);

    /**
     * Finds products that need to be reindexed (recently updated)
     * @param since the timestamp to check updates since
     * @return list of recently updated products
     */
    @Query("SELECT p FROM InfrastructureProductJpaEntity p WHERE p.updatedAt > :since")
    java.util.List<InfrastructureProductJpaEntity> findUpdatedSince(@Param("since") java.time.Instant since);
}
