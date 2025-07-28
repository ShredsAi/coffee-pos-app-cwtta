package ai.shreds.infrastructure.specifications;

import ai.shreds.domain.specifications.DomainProductSpecification;
import ai.shreds.infrastructure.entities.InfrastructureProductJpaEntity;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

/**
 * Specification builder for converting domain product filters to JPA criteria queries.
 * Handles complex filtering including category, brand, status, and full-text search.
 * This is NOT a Spring bean - it provides static utility methods for converting domain specs to JPA criteria.
 */
@Slf4j
public class InfrastructureProductSpecification {

    /**
     * Converts domain specification to JPA criteria predicate
     * @param spec the domain specification
     * @param cb the criteria builder
     * @param root the root entity
     * @return predicate for filtering
     */
    public static Predicate toCriteria(DomainProductSpecification spec, CriteriaBuilder cb, Root<InfrastructureProductJpaEntity> root) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (spec == null) {
            return cb.conjunction();
        }

        // Category filter
        if (spec.getCategoryId() != null) {
            predicates.add(buildCategoryFilter(spec.getCategoryId(), cb, root));
        }

        // Brand filter
        if (spec.getBrand() != null && !spec.getBrand().trim().isEmpty()) {
            predicates.add(buildBrandFilter(spec.getBrand(), cb, root));
        }

        // Publication status filter
        if (spec.getPublicationStatus() != null) {
            predicates.add(buildStatusFilter(spec.getPublicationStatus().name(), cb, root));
        }

        // Active status filter
        if (spec.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), spec.getIsActive()));
        }

        // Search filter
        if (spec.getSearch() != null && !spec.getSearch().trim().isEmpty()) {
            predicates.add(buildSearchFilter(spec.getSearch(), cb, root));
        }

        return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Builds category filter predicate
     * @param categoryId the category ID
     * @param cb the criteria builder
     * @param root the root entity
     * @return category filter predicate
     */
    private static Predicate buildCategoryFilter(UUID categoryId, CriteriaBuilder cb, Root<InfrastructureProductJpaEntity> root) {
        try {
            Join<Object, Object> categoryJoin = root.join("categories", JoinType.INNER);
            return cb.equal(categoryJoin.get("categoryId"), categoryId);
        } catch (Exception e) {
            log.warn("Error building category filter for categoryId: {}", categoryId, e);
            return cb.conjunction();
        }
    }

    /**
     * Builds brand filter predicate
     * @param brand the brand name
     * @param cb the criteria builder
     * @param root the root entity
     * @return brand filter predicate
     */
    private static Predicate buildBrandFilter(String brand, CriteriaBuilder cb, Root<InfrastructureProductJpaEntity> root) {
        return cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    /**
     * Builds publication status filter predicate
     * @param status the publication status
     * @param cb the criteria builder
     * @param root the root entity
     * @return status filter predicate
     */
    private static Predicate buildStatusFilter(String status, CriteriaBuilder cb, Root<InfrastructureProductJpaEntity> root) {
        return cb.equal(root.get("publicationStatus"), status);
    }

    /**
     * Builds full-text search filter predicate
     * @param search the search term
     * @param cb the criteria builder
     * @param root the root entity
     * @return search filter predicate
     */
    private static Predicate buildSearchFilter(String search, CriteriaBuilder cb, Root<InfrastructureProductJpaEntity> root) {
        String pattern = "%" + search.toLowerCase() + "%";
        
        return cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("description")), pattern),
            cb.like(cb.lower(root.get("shortDescription")), pattern),
            cb.like(cb.lower(root.get("brand")), pattern),
            cb.like(cb.lower(root.get("model")), pattern),
            cb.like(cb.lower(root.get("sku")), pattern)
        );
    }

    /**
     * Static factory methods for common specifications
     */

    /**
     * Creates specification for products by category
     * @param categoryId the category ID
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> byCategory(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return buildCategoryFilter(categoryId, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for products by brand
     * @param brand the brand name
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> byBrand(String brand) {
        return (root, query, criteriaBuilder) -> {
            if (brand == null || brand.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildBrandFilter(brand, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for products by publication status
     * @param status the publication status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> byPublicationStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildStatusFilter(status, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for products by active status
     * @param isActive the active status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> byActiveStatus(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isActive"), isActive);
        };
    }

    /**
     * Creates specification for full-text search
     * @param searchTerm the search term
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> bySearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildSearchFilter(searchTerm, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for products by SKU
     * @param sku the SKU
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> bySku(String sku) {
        return (root, query, criteriaBuilder) -> {
            if (sku == null || sku.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("sku"), sku);
        };
    }

    /**
     * Creates specification for products by slug
     * @param slug the slug
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> bySlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("slug"), slug);
        };
    }

    /**
     * Creates specification for products in multiple categories
     * @param categoryIds the list of category IDs
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> byCategories(List<UUID> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Object, Object> categoryJoin = root.join("categories", JoinType.INNER);
            return categoryJoin.get("categoryId").in(categoryIds);
        };
    }

    /**
     * Creates specification for products updated since a certain time
     * @param since the timestamp
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductJpaEntity> updatedSince(java.time.Instant since) {
        return (root, query, criteriaBuilder) -> {
            if (since == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("updatedAt"), since);
        };
    }

    /**
     * Combines multiple specifications with AND logic
     * @param specs the specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static Specification<InfrastructureProductJpaEntity> and(
            Specification<InfrastructureProductJpaEntity>... specs) {
        
        Specification<InfrastructureProductJpaEntity> result = Specification.where(null);
        
        for (Specification<InfrastructureProductJpaEntity> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        
        return result;
    }

    /**
     * Combines multiple specifications with OR logic
     * @param specs the specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static Specification<InfrastructureProductJpaEntity> or(
            Specification<InfrastructureProductJpaEntity>... specs) {
        
        Specification<InfrastructureProductJpaEntity> result = null;
        
        for (Specification<InfrastructureProductJpaEntity> spec : specs) {
            if (spec != null) {
                if (result == null) {
                    result = Specification.where(spec);
                } else {
                    result = result.or(spec);
                }
            }
        }
        
        return result != null ? result : Specification.where(null);
    }
}