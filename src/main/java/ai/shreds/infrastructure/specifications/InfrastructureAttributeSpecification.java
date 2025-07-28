package ai.shreds.infrastructure.specifications;

import ai.shreds.domain.specifications.DomainAttributeSpecification;
import ai.shreds.infrastructure.entities.InfrastructureProductAttributeJpaEntity;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

/**
 * Specification builder for converting domain attribute filters to JPA criteria queries.
 * Handles complex filtering including attribute type, filterable, searchable, required, active status, and search.
 * This is NOT a Spring bean - it provides static utility methods for converting domain specs to JPA criteria.
 */
@Slf4j
public class InfrastructureAttributeSpecification {

    /**
     * Converts domain specification to JPA criteria predicate
     * @param spec the domain specification
     * @param cb the criteria builder
     * @param root the root entity
     * @return predicate for filtering
     */
    public static Predicate toCriteria(DomainAttributeSpecification spec, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (spec == null) {
            return cb.conjunction();
        }

        // Attribute type filter
        if (spec.getAttributeType() != null) {
            predicates.add(buildTypeFilter(spec.getAttributeType().name(), cb, root));
        }

        // Filterable status filter
        if (spec.getIsFilterable() != null) {
            predicates.add(buildFilterableFilter(spec.getIsFilterable(), cb, root));
        }

        // Searchable status filter
        if (spec.getIsSearchable() != null) {
            predicates.add(buildSearchableFilter(spec.getIsSearchable(), cb, root));
        }

        // Required status filter
        if (spec.getIsRequired() != null) {
            predicates.add(buildRequiredFilter(spec.getIsRequired(), cb, root));
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
     * Builds attribute type filter predicate
     * @param attributeType the attribute type
     * @param cb the criteria builder
     * @param root the root entity
     * @return type filter predicate
     */
    private static Predicate buildTypeFilter(String attributeType, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        return cb.equal(root.get("attributeType"), attributeType);
    }

    /**
     * Builds filterable status filter predicate
     * @param isFilterable the filterable status
     * @param cb the criteria builder
     * @param root the root entity
     * @return filterable filter predicate
     */
    private static Predicate buildFilterableFilter(Boolean isFilterable, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        return cb.equal(root.get("isFilterable"), isFilterable);
    }

    /**
     * Builds searchable status filter predicate
     * @param isSearchable the searchable status
     * @param cb the criteria builder
     * @param root the root entity
     * @return searchable filter predicate
     */
    private static Predicate buildSearchableFilter(Boolean isSearchable, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        return cb.equal(root.get("isSearchable"), isSearchable);
    }

    /**
     * Builds required status filter predicate
     * @param isRequired the required status
     * @param cb the criteria builder
     * @param root the root entity
     * @return required filter predicate
     */
    private static Predicate buildRequiredFilter(Boolean isRequired, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        return cb.equal(root.get("isRequired"), isRequired);
    }

    /**
     * Builds active status filter predicate
     * @param isActive the active status
     * @param cb the criteria builder
     * @param root the root entity
     * @return active status filter predicate
     */
    private static Predicate buildActiveFilter(Boolean isActive, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        return cb.equal(root.get("isActive"), isActive);
    }

    /**
     * Builds search filter predicate
     * @param search the search term
     * @param cb the criteria builder
     * @param root the root entity
     * @return search filter predicate
     */
    private static Predicate buildSearchFilter(String search, CriteriaBuilder cb, Root<InfrastructureProductAttributeJpaEntity> root) {
        String pattern = "%" + search.toLowerCase() + "%";
        
        return cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("description")), pattern),
            cb.like(cb.lower(root.get("code")), pattern)
        );
    }

    /**
     * Static factory methods for common specifications
     */

    /**
     * Creates specification for attributes by type
     * @param attributeType the attribute type
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> byType(String attributeType) {
        return (root, query, criteriaBuilder) -> {
            if (attributeType == null || attributeType.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildTypeFilter(attributeType, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for filterable attributes
     * @param isFilterable the filterable status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> byFilterable(Boolean isFilterable) {
        return (root, query, criteriaBuilder) -> {
            if (isFilterable == null) {
                return criteriaBuilder.conjunction();
            }
            return buildFilterableFilter(isFilterable, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for searchable attributes
     * @param isSearchable the searchable status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> bySearchable(Boolean isSearchable) {
        return (root, query, criteriaBuilder) -> {
            if (isSearchable == null) {
                return criteriaBuilder.conjunction();
            }
            return buildSearchableFilter(isSearchable, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for required attributes
     * @param isRequired the required status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> byRequired(Boolean isRequired) {
        return (root, query, criteriaBuilder) -> {
            if (isRequired == null) {
                return criteriaBuilder.conjunction();
            }
            return buildRequiredFilter(isRequired, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for active/inactive attributes
     * @param isActive the active status
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> byActiveStatus(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }
            return buildActiveFilter(isActive, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for attribute search
     * @param searchTerm the search term
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> bySearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return buildSearchFilter(searchTerm, criteriaBuilder, root);
        };
    }

    /**
     * Creates specification for attributes by code
     * @param code the attribute code
     * @return JPA Specification
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> byCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (code == null || code.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("code"), code);
        };
    }

    /**
     * Creates specification for select-type attributes
     * @return JPA Specification for select-type attributes
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> selectTypes() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                criteriaBuilder.equal(root.get("attributeType"), "SELECT_SINGLE"),
                criteriaBuilder.equal(root.get("attributeType"), "SELECT_MULTIPLE")
            );
        };
    }

    /**
     * Creates specification for numeric attributes
     * @return JPA Specification for numeric attributes
     */
    public static Specification<InfrastructureProductAttributeJpaEntity> numericTypes() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("attributeType"), "NUMBER");
        };
    }

    /**
     * Combines multiple specifications with AND logic
     * @param specs the specifications to combine
     * @return combined specification
     */
    @SafeVarargs
    public static Specification<InfrastructureProductAttributeJpaEntity> and(
            Specification<InfrastructureProductAttributeJpaEntity>... specs) {
        
        Specification<InfrastructureProductAttributeJpaEntity> result = Specification.where(null);
        
        for (Specification<InfrastructureProductAttributeJpaEntity> spec : specs) {
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
    public static Specification<InfrastructureProductAttributeJpaEntity> or(
            Specification<InfrastructureProductAttributeJpaEntity>... specs) {
        
        Specification<InfrastructureProductAttributeJpaEntity> result = null;
        
        for (Specification<InfrastructureProductAttributeJpaEntity> spec : specs) {
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