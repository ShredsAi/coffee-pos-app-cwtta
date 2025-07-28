package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for category entities.
 * Provides CRUD operations and custom query methods for categories.
 */
@Repository
public interface InfrastructureCategoryJpaRepository extends 
        JpaRepository<InfrastructureCategoryJpaEntity, UUID>, 
        JpaSpecificationExecutor<InfrastructureCategoryJpaEntity> {

    /**
     * Finds a category by its slug
     * @param slug the category slug
     * @return Optional containing the category if found
     */
    Optional<InfrastructureCategoryJpaEntity> findBySlug(String slug);

    /**
     * Finds all categories with a specific parent
     * @param parentId the parent category ID
     * @return list of child categories
     */
    List<InfrastructureCategoryJpaEntity> findByParentCategoryId(UUID parentId);

    /**
     * Finds all root categories (no parent)
     * @return list of root categories
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.parentCategoryId IS NULL ORDER BY c.sortOrder, c.name")
    List<InfrastructureCategoryJpaEntity> findRootCategories();

    /**
     * Finds all active root categories
     * @return list of active root categories
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.parentCategoryId IS NULL AND c.isActive = true ORDER BY c.sortOrder, c.name")
    List<InfrastructureCategoryJpaEntity> findActiveRootCategories();

    /**
     * Finds categories by level
     * @param level the category level
     * @return list of categories at the specified level
     */
    List<InfrastructureCategoryJpaEntity> findByLevelOrderBySortOrderAscNameAsc(Integer level);

    /**
     * Finds active categories by level
     * @param level the category level
     * @return list of active categories at the specified level
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.level = :level AND c.isActive = true ORDER BY c.sortOrder, c.name")
    List<InfrastructureCategoryJpaEntity> findActiveByLevel(@Param("level") Integer level);

    /**
     * Checks if a category exists with the given slug
     * @param slug the slug to check
     * @return true if a category with the slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if a category exists with the given slug, excluding a specific category ID
     * @param slug the slug to check
     * @param id the category ID to exclude
     * @return true if another category with the slug exists
     */
    boolean existsBySlugAndIdNot(String slug, UUID id);

    /**
     * Finds all descendant categories of a parent category
     * @param parentPath the path of the parent category
     * @return list of descendant categories
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.path LIKE CONCAT(:parentPath, '%') AND c.path != :parentPath")
    List<InfrastructureCategoryJpaEntity> findDescendants(@Param("parentPath") String parentPath);

    /**
     * Finds categories by name (case-insensitive search)
     * @param name the name to search for
     * @return list of categories matching the name
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<InfrastructureCategoryJpaEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds the maximum level in the category hierarchy
     * @return the maximum level
     */
    @Query("SELECT MAX(c.level) FROM InfrastructureCategoryJpaEntity c")
    Optional<Integer> findMaxLevel();

    /**
     * Counts direct children of a category
     * @param parentId the parent category ID
     * @return count of direct children
     */
    @Query("SELECT COUNT(c) FROM InfrastructureCategoryJpaEntity c WHERE c.parentCategoryId = :parentId")
    long countChildren(@Param("parentId") UUID parentId);

    /**
     * Counts active direct children of a category
     * @param parentId the parent category ID
     * @return count of active direct children
     */
    @Query("SELECT COUNT(c) FROM InfrastructureCategoryJpaEntity c WHERE c.parentCategoryId = :parentId AND c.isActive = true")
    long countActiveChildren(@Param("parentId") UUID parentId);

    /**
     * Finds categories used by active products
     * @return list of categories that have active products
     */
    @Query("SELECT DISTINCT c FROM InfrastructureCategoryJpaEntity c " +
           "JOIN InfrastructureProductCategoryJpaEntity pc ON c.id = pc.categoryId " +
           "JOIN InfrastructureProductJpaEntity p ON pc.productId = p.id " +
           "WHERE p.isActive = true")
    List<InfrastructureCategoryJpaEntity> findCategoriesWithActiveProducts();

    /**
     * Finds sibling categories (same parent)
     * @param parentId the parent category ID
     * @param excludeId the category ID to exclude from results
     * @return list of sibling categories
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.parentCategoryId = :parentId AND c.id != :excludeId ORDER BY c.sortOrder, c.name")
    List<InfrastructureCategoryJpaEntity> findSiblings(@Param("parentId") UUID parentId, @Param("excludeId") UUID excludeId);

    /**
     * Finds categories that need path recalculation
     * @return list of categories with potential path issues
     */
    @Query("SELECT c FROM InfrastructureCategoryJpaEntity c WHERE c.path IS NULL OR c.path = ''")
    List<InfrastructureCategoryJpaEntity> findCategoriesNeedingPathRecalculation();
}
