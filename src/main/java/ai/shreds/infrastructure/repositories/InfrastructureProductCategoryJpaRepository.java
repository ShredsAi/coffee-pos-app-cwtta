package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product-category relationship entities.
 * Provides CRUD operations and custom query methods for product-category associations.
 */
@Repository
public interface InfrastructureProductCategoryJpaRepository extends JpaRepository<InfrastructureProductCategoryJpaEntity, UUID> {

    /**
     * Finds all category associations for a specific product
     * @param productId the product ID
     * @return list of product-category associations
     */
    List<InfrastructureProductCategoryJpaEntity> findByProductId(UUID productId);

    /**
     * Finds all product associations for a specific category
     * @param categoryId the category ID
     * @return list of product-category associations
     */
    List<InfrastructureProductCategoryJpaEntity> findByCategoryId(UUID categoryId);

    /**
     * Finds the primary category association for a product
     * @param productId the product ID
     * @return Optional containing the primary category association if found
     */
    Optional<InfrastructureProductCategoryJpaEntity> findByProductIdAndIsPrimaryTrue(UUID productId);

    /**
     * Finds non-primary category associations for a product
     * @param productId the product ID
     * @return list of non-primary category associations
     */
    List<InfrastructureProductCategoryJpaEntity> findByProductIdAndIsPrimaryFalse(UUID productId);

    /**
     * Finds specific product-category association
     * @param productId the product ID
     * @param categoryId the category ID
     * @return Optional containing the association if found
     */
    Optional<InfrastructureProductCategoryJpaEntity> findByProductIdAndCategoryId(UUID productId, UUID categoryId);

    /**
     * Checks if a product is associated with a category
     * @param productId the product ID
     * @param categoryId the category ID
     * @return true if association exists
     */
    boolean existsByProductIdAndCategoryId(UUID productId, UUID categoryId);

    /**
     * Checks if a product has a primary category
     * @param productId the product ID
     * @return true if product has a primary category
     */
    boolean existsByProductIdAndIsPrimaryTrue(UUID productId);

    /**
     * Deletes all category associations for a product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureProductCategoryJpaEntity pc WHERE pc.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Deletes specific product-category association
     * @param productId the product ID
     * @param categoryId the category ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureProductCategoryJpaEntity pc WHERE pc.productId = :productId AND pc.categoryId = :categoryId")
    void deleteByProductIdAndCategoryId(@Param("productId") UUID productId, @Param("categoryId") UUID categoryId);

    /**
     * Removes primary flag from all categories for a product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductCategoryJpaEntity pc SET pc.isPrimary = false WHERE pc.productId = :productId")
    void removePrimaryFlagForProduct(@Param("productId") UUID productId);

    /**
     * Sets a specific category as primary for a product
     * @param productId the product ID
     * @param categoryId the category ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductCategoryJpaEntity pc SET pc.isPrimary = true WHERE pc.productId = :productId AND pc.categoryId = :categoryId")
    void setPrimaryCategory(@Param("productId") UUID productId, @Param("categoryId") UUID categoryId);

    /**
     * Counts products in a specific category
     * @param categoryId the category ID
     * @return count of products in the category
     */
    long countByCategoryId(UUID categoryId);

    /**
     * Counts categories for a specific product
     * @param productId the product ID
     * @return count of categories for the product
     */
    long countByProductId(UUID productId);

    /**
     * Finds products in multiple categories
     * @param categoryIds the list of category IDs
     * @return list of product IDs that are in any of the specified categories
     */
    @Query("SELECT DISTINCT pc.productId FROM InfrastructureProductCategoryJpaEntity pc WHERE pc.categoryId IN :categoryIds")
    List<UUID> findProductIdsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);

    /**
     * Finds primary categories for multiple products
     * @param productIds the list of product IDs
     * @return list of product-category associations that are primary
     */
    @Query("SELECT pc FROM InfrastructureProductCategoryJpaEntity pc WHERE pc.productId IN :productIds AND pc.isPrimary = true")
    List<InfrastructureProductCategoryJpaEntity> findPrimaryCategoriesByProductIds(@Param("productIds") List<UUID> productIds);

    /**
     * Finds products that have the specified category as primary
     * @param categoryId the category ID
     * @return list of product IDs that have this category as primary
     */
    @Query("SELECT pc.productId FROM InfrastructureProductCategoryJpaEntity pc WHERE pc.categoryId = :categoryId AND pc.isPrimary = true")
    List<UUID> findProductIdsWithPrimaryCategory(@Param("categoryId") UUID categoryId);

    /**
     * Finds category usage statistics
     * @return list of arrays containing [categoryId, productCount]
     */
    @Query("SELECT pc.categoryId, COUNT(pc.productId) FROM InfrastructureProductCategoryJpaEntity pc GROUP BY pc.categoryId")
    List<Object[]> getCategoryUsageStatistics();

    /**
     * Finds products without any category assignments
     * @param allProductIds the list of all product IDs to check
     * @return list of product IDs that have no category assignments
     */
    @Query("SELECT p.id FROM InfrastructureProductJpaEntity p WHERE p.id NOT IN (SELECT DISTINCT pc.productId FROM InfrastructureProductCategoryJpaEntity pc)")
    List<UUID> findProductsWithoutCategories();

    /**
     * Finds products without primary category
     * @return list of product IDs that don't have a primary category
     */
    @Query("SELECT DISTINCT pc1.productId FROM InfrastructureProductCategoryJpaEntity pc1 " +
           "WHERE pc1.productId NOT IN (" +
           "SELECT pc2.productId FROM InfrastructureProductCategoryJpaEntity pc2 WHERE pc2.isPrimary = true" +
           ")")
    List<UUID> findProductsWithoutPrimaryCategory();

    // Note: updateAssignmentTimestamp method removed because assignedAt field is marked as updatable=false
    // If you need to update assignment timestamps, you would need to delete and recreate the association
    // or modify the entity to make assignedAt updatable
}