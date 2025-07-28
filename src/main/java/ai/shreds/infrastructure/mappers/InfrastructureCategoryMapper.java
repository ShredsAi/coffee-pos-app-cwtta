package ai.shreds.infrastructure.mappers;

import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.infrastructure.entities.InfrastructureCategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between category domain entities and JPA entities.
 * Handles the hierarchical relationships and path calculations.
 */
@Component
public class InfrastructureCategoryMapper {

    /**
     * Converts JPA entity to domain entity
     * @param jpaEntity the JPA entity
     * @return domain entity
     */
    public DomainCategoryEntity toDomainEntity(InfrastructureCategoryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DomainCategoryEntity.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .slug(jpaEntity.getSlug())
                .parentCategoryId(jpaEntity.getParentCategoryId())
                .level(jpaEntity.getLevel())
                .path(jpaEntity.getPath())
                .sortOrder(jpaEntity.getSortOrder())
                .isActive(jpaEntity.getIsActive())
                .children(mapChildren(jpaEntity.getChildren()))
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .createdBy(jpaEntity.getCreatedBy())
                .updatedBy(jpaEntity.getUpdatedBy())
                .version(jpaEntity.getVersion())
                .build();
    }

    /**
     * Converts domain entity to JPA entity
     * @param domainEntity the domain entity
     * @return JPA entity
     */
    public InfrastructureCategoryJpaEntity toJpaEntity(DomainCategoryEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        return InfrastructureCategoryJpaEntity.builder()
                .id(domainEntity.getId())
                .name(domainEntity.getName())
                .description(domainEntity.getDescription())
                .slug(domainEntity.getSlug())
                .parentCategoryId(domainEntity.getParentCategoryId())
                .level(domainEntity.getLevel())
                .path(domainEntity.getPath())
                .sortOrder(domainEntity.getSortOrder())
                .isActive(domainEntity.getIsActive())
                .children(mapChildrenToJpa(domainEntity.getChildren()))
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .createdBy(domainEntity.getCreatedBy())
                .updatedBy(domainEntity.getUpdatedBy())
                .version(domainEntity.getVersion())
                .build();
    }

    /**
     * Merges domain entity data into existing JPA entity
     * @param domain the domain entity source
     * @param jpa the JPA entity target
     */
    public void mergeDomainToJpa(DomainCategoryEntity domain, InfrastructureCategoryJpaEntity jpa) {
        if (domain == null || jpa == null) {
            return;
        }

        // Update basic fields
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setSlug(domain.getSlug());
        jpa.setParentCategoryId(domain.getParentCategoryId());
        jpa.setLevel(domain.getLevel());
        jpa.setPath(domain.getPath());
        jpa.setSortOrder(domain.getSortOrder());
        jpa.setIsActive(domain.getIsActive());
        jpa.setUpdatedAt(Instant.now());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setVersion(domain.getVersion());

        // Note: Children are typically managed separately to avoid deep recursion
        // and performance issues with large hierarchy updates
    }

    /**
     * Maps a list of domain categories to JPA entities (non-recursive for performance)
     * @param domainCategories the domain categories
     * @return list of JPA entities
     */
    public List<InfrastructureCategoryJpaEntity> toJpaEntities(List<DomainCategoryEntity> domainCategories) {
        if (domainCategories == null || domainCategories.isEmpty()) {
            return new ArrayList<>();
        }

        return domainCategories.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of JPA categories to domain entities (non-recursive for performance)
     * @param jpaCategories the JPA categories
     * @return list of domain entities
     */
    public List<DomainCategoryEntity> toDomainEntities(List<InfrastructureCategoryJpaEntity> jpaCategories) {
        if (jpaCategories == null || jpaCategories.isEmpty()) {
            return new ArrayList<>();
        }

        return jpaCategories.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * Helper methods for mapping collections
     */
    
    private List<DomainCategoryEntity> mapChildren(List<InfrastructureCategoryJpaEntity> jpaChildren) {
        if (jpaChildren == null || jpaChildren.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Map children but avoid deep recursion to prevent performance issues
        return jpaChildren.stream()
                .map(this::toShallowDomainEntity)
                .collect(Collectors.toList());
    }
    
    private List<InfrastructureCategoryJpaEntity> mapChildrenToJpa(List<DomainCategoryEntity> domainChildren) {
        if (domainChildren == null || domainChildren.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Map children but avoid deep recursion to prevent performance issues
        return domainChildren.stream()
                .map(this::toShallowJpaEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a shallow domain entity (without children) to avoid recursion
     * @param jpaEntity the JPA entity
     * @return shallow domain entity
     */
    private DomainCategoryEntity toShallowDomainEntity(InfrastructureCategoryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DomainCategoryEntity.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .slug(jpaEntity.getSlug())
                .parentCategoryId(jpaEntity.getParentCategoryId())
                .level(jpaEntity.getLevel())
                .path(jpaEntity.getPath())
                .sortOrder(jpaEntity.getSortOrder())
                .isActive(jpaEntity.getIsActive())
                .children(new ArrayList<>()) // Empty to avoid recursion
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .createdBy(jpaEntity.getCreatedBy())
                .updatedBy(jpaEntity.getUpdatedBy())
                .version(jpaEntity.getVersion())
                .build();
    }
    
    /**
     * Creates a shallow JPA entity (without children) to avoid recursion
     * @param domainEntity the domain entity
     * @return shallow JPA entity
     */
    private InfrastructureCategoryJpaEntity toShallowJpaEntity(DomainCategoryEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        return InfrastructureCategoryJpaEntity.builder()
                .id(domainEntity.getId())
                .name(domainEntity.getName())
                .description(domainEntity.getDescription())
                .slug(domainEntity.getSlug())
                .parentCategoryId(domainEntity.getParentCategoryId())
                .level(domainEntity.getLevel())
                .path(domainEntity.getPath())
                .sortOrder(domainEntity.getSortOrder())
                .isActive(domainEntity.getIsActive())
                .children(new ArrayList<>()) // Empty to avoid recursion
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .createdBy(domainEntity.getCreatedBy())
                .updatedBy(domainEntity.getUpdatedBy())
                .version(domainEntity.getVersion())
                .build();
    }
    
    /**
     * Utility methods for category hierarchy management
     */
    
    /**
     * Generates a new category ID if not present
     * @return new UUID
     */
    public UUID generateNewCategoryId() {
        return UUID.randomUUID();
    }
    
    /**
     * Sets audit fields for new category creation
     * @param jpaEntity the JPA entity
     * @param createdBy the user creating the category
     */
    public void setCreationAuditFields(InfrastructureCategoryJpaEntity jpaEntity, String createdBy) {
        Instant now = Instant.now();
        jpaEntity.setCreatedAt(now);
        jpaEntity.setUpdatedAt(now);
        jpaEntity.setCreatedBy(createdBy);
        jpaEntity.setUpdatedBy(createdBy);
        
        if (jpaEntity.getIsActive() == null) {
            jpaEntity.setIsActive(true);
        }
        if (jpaEntity.getSortOrder() == null) {
            jpaEntity.setSortOrder(0);
        }
    }
    
    /**
     * Sets audit fields for category updates
     * @param jpaEntity the JPA entity
     * @param updatedBy the user updating the category
     */
    public void setUpdateAuditFields(InfrastructureCategoryJpaEntity jpaEntity, String updatedBy) {
        jpaEntity.setUpdatedAt(Instant.now());
        jpaEntity.setUpdatedBy(updatedBy);
    }
    
    /**
     * Calculates the category path based on parent hierarchy
     * @param categoryName the current category name
     * @param parentPath the parent category path (can be null for root)
     * @return calculated path
     */
    public String calculatePath(String categoryName, String parentPath) {
        if (parentPath == null || parentPath.trim().isEmpty()) {
            return categoryName;
        }
        return parentPath + " > " + categoryName;
    }
    
    /**
     * Calculates the category level based on parent level
     * @param parentLevel the parent category level (null for root)
     * @return calculated level
     */
    public Integer calculateLevel(Integer parentLevel) {
        if (parentLevel == null) {
            return 0; // Root level
        }
        return parentLevel + 1;
    }
    
    /**
     * Validates category hierarchy to prevent cycles
     * @param categoryId the category ID
     * @param parentCategoryId the proposed parent category ID
     * @param allCategories all categories for cycle detection
     * @return true if the hierarchy is valid (no cycles)
     */
    public boolean isValidHierarchy(UUID categoryId, UUID parentCategoryId, List<InfrastructureCategoryJpaEntity> allCategories) {
        if (parentCategoryId == null) {
            return true; // Root category is always valid
        }

        if (categoryId.equals(parentCategoryId)) {
            return false; // Category cannot be its own parent
        }

        // Check for cycles by following parent chain
        UUID currentParentId = parentCategoryId;
        int maxDepth = 10; // Prevent infinite loops
        int depth = 0;

        while (currentParentId != null && depth < maxDepth) {
            if (categoryId.equals(currentParentId)) {
                return false; // Cycle detected
            }

            // Capture the id in a final variable for lambda
            final UUID pid = currentParentId;
            // Find the parent category
            InfrastructureCategoryJpaEntity parentCategory = allCategories.stream()
                    .filter(cat -> cat.getId().equals(pid))
                    .findFirst()
                    .orElse(null);

            if (parentCategory == null) {
                break; // Parent not found, assume valid
            }

            currentParentId = parentCategory.getParentCategoryId();
            depth++;
        }

        return depth < maxDepth; // Valid if we didn't hit max depth (no infinite loop)
    }
}
