package ai.shreds.infrastructure.specifications;

import ai.shreds.domain.specifications.DomainCategorySpecification;
import ai.shreds.infrastructure.entities.InfrastructureCategoryJpaEntity;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

/**
 * Specification builder for converting domain category filters to JPA criteria queries.
 * Handles complex filtering including parent category, level, active status, and search.
 * This is NOT a Spring bean - it provides static utility methods for converting domain specs to JPA criteria.
 */
@Slf4j
public class InfrastructureCategorySpecification {

    /**
     * Converts domain specification to JPA criteria predicate
     * @param spec the domain specification
     * @param cb the criteria builder
     * @param root the root entity
     * @return predicate for filtering
     */
    public static Predicate toCriteria(DomainCategorySpecification spec, CriteriaBuilder cb, Root<InfrastructureCategoryJpaEntity> root) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (spec == null) {
            return cb.conjunction();
        }

        // Parent category filter
        if (spec.getParentCategoryId() != null) {
            predicates.add(buildParentFilter(spec.getParentCategoryId(), cb, root));
        }

        // Level filter
        if (spec.getLevel() != null) {
            predicates.add(buildLevelFilter(spec.getLevel(), cb, root));
        }

        // Active status filter
        if (spec.getIsActive() != null) {
            predicates.add(buildActiveFilter(spec.getIsActive(), cb, root));
        }

        // Search filter
        if (spec.getSearch() != null && !spec.getSearch().trim().isEmpty()) {
            predicates.add(buildSearchFilter(spec.getSearch(), cb, root));
        }

        return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Builds parent category filter predicate
     * @param parentId the parent category ID (null for root categories)
     * @param cb the criteria builder
     * @param root the root entity
     * @return parent filter predicate
     */
    private static Predicate buildParentFilter(UUID parentId, CriteriaBuilder cb, Root<InfrastructureCategoryJpaEntity> root) {
        if (parentId == null) {
            // Find root categories (no parent)
            return cb.isNull(root.get("parentCategoryId"));
        } else {
            // Find categories with specific parent
            return cb.equal(root.get("parentCategoryId"), parentId);
        }
    }

    /**
     * Builds level filter predicate
     * @param level the category level
     * @param cb the criteria builder
     * @param root the root entity
     * @return level filter predicate
     */
    private static Predicate buildLevelFilter(Integer level, CriteriaBuilder cb, Root<InfrastructureCategoryJpaEntity> root) {
        return cb.equal(root.get("level"), level);
    }

    /**
     * Builds active status filter predicate
     * @param isActive the active status
     * @param cb the criteria builder
     * @param root the root entity
     * @return active status filter predicate
     */
    private static Predicate buildActiveFilter(Boolean isActive, CriteriaBuilder cb, Root<InfrastructureCategoryJpaEntity> root) {
        return cb.equal(root.get("isActive"), isActive);
    }

    /**
     * Builds search filter predicate
     * @param search the search term
     * @param cb the criteria builder
     * @param root the root entity
     * @return search filter predicate
     */
    private static Predicate buildSearchFilter(String search, CriteriaBuilder cb, Root<InfrastructureCategoryJpaEntity> root) {
        String pattern = "%" + search.toLowerCase() + "%";
        
        return cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("description")), pattern),
            cb.like(cb.lower(root.get("slug")), pattern)
        );
    }

    /**
     * Static factory methods for common specifications
     */

    /**
     * Creates specification for root categories
     * @return JPA Specification for root categories
     */
    public static Specification<InfrastructureCategoryJpaEntity> rootCategories() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isNull(root.get("parentCategoryId"));
        };
    }

    /**
     * Creates specification for categories by parent ID
     * @param parentId the parent category ID
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> byParent(UUID parentId) {
        return (root, query, criteriaBuilder) -> {
            if (parentId == null) {
                return criteriaBuilder.conjunction();
            }
            return buildParentFilter(parentId, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for categories by level
     * @param level the category level
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> byLevel(Integer level) {
        return (root, query, criteriaBuilder) -> {
            if (level == null) {
                return criteriaBuilder.conjunction();
            }
            return buildLevelFilter(level, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for active/inactive categories
     * @param isActive the active status
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> byActiveStatus(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }
            return buildActiveFilter(isActive, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for category search
     * @param searchTerm the search term
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> bySearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildSearchFilter(searchTerm, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for categories by path pattern
     * @param pathPattern the path pattern to match
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> byPathPattern(String pathPattern) {
        return (root, query, criteriaBuilder) -> {
            if (pathPattern == null || pathPattern.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("path"), pathPattern);
        };
    }

    /**
     * Creates specification for categories by slug
     * @param slug the category slug
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> bySlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("slug"), slug);
        };
    }

    /**
     * Creates specification for categories within a level range
     * @param minLevel minimum level (inclusive)
     * @param maxLevel maximum level (inclusive)
     * @return JPA Specification
     */
    public static Specification<InfrastructureCategoryJpaEntity> byLevelRange(Integer minLevel, Integer maxLevel) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minLevel != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel));
            }
            
            if (maxLevel != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel));
            }
            
            return predicates.isEmpty() ? 
                criteriaBuilder.conjunction() : 
                criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Combines multiple specifications with AND logic
     * @param specs the specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static Specification<InfrastructureCategoryJpaEntity> and(
            Specification<InfrastructureCategoryJpaEntity>... specs) {
        
        Specification<InfrastructureCategoryJpaEntity> result = Specification.where(null);
        
        for (Specification<InfrastructureCategoryJpaEntity> spec : specs) {
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
    public static Specification<InfrastructureCategoryJpaEntity> or(
            Specification<InfrastructureCategoryJpaEntity>... specs) {
        
        Specification<InfrastructureCategoryJpaEntity> result = null;
        
        for (Specification<InfrastructureCategoryJpaEntity> spec : specs) {
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