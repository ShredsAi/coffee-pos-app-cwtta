package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedCategoryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain Category Entity - Aggregate Root
 * Represents hierarchical product categories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainCategoryEntity {
    
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private UUID parentCategoryId;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private Boolean isActive;
    
    @Builder.Default
    private List<DomainCategoryEntity> children = new ArrayList<>();
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;
    
    private String createdBy;
    private String updatedBy;
    private Long version;
    
    /**
     * Calculates the hierarchical path for the category
     * Path format: "/root/parent/child"
     */
    public String calculatePath() {
        if (this.parentCategoryId == null) {
            // Root category
            return "/" + this.slug;
        }
        
        // For child categories, path should be calculated based on parent
        // This method assumes parent path is available in the context
        // Full implementation would require parent entity or service
        return this.path != null ? this.path : "/" + this.slug;
    }
    
    /**
     * Calculates the level in the hierarchy (0 for root)
     */
    public Integer calculateLevel() {
        if (this.parentCategoryId == null) {
            return 0; // Root level
        }
        
        // Level calculation requires parent information
        // This would typically be handled by the domain service
        return this.level != null ? this.level : 1;
    }
    
    /**
     * Validates the category hierarchy to prevent cycles and depth overflow
     */
    public void validateHierarchy() {
        // Prevent self-referencing
        if (this.id != null && this.id.equals(this.parentCategoryId)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        
        // Validate maximum depth (configurable, default 5)
        int maxDepth = 5;
        if (this.level != null && this.level > maxDepth) {
            throw new IllegalArgumentException(
                String.format("Category hierarchy cannot exceed %d levels", maxDepth)
            );
        }
        
        // Validate required fields
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be null or empty");
        }
    }
    
    /**
     * Adds a child category
     */
    public void addChild(DomainCategoryEntity child) {
        if (child == null) {
            throw new IllegalArgumentException("Child category cannot be null");
        }
        
        // Prevent adding the same child multiple times
        boolean exists = this.children.stream()
            .anyMatch(existing -> existing.getId().equals(child.getId()));
        
        if (!exists) {
            child.setParentCategoryId(this.id);
            child.setLevel(this.level != null ? this.level + 1 : 1);
            child.setPath(this.calculatePath() + "/" + child.getSlug());
            
            this.children.add(child);
            this.updatedAt = Instant.now();
        }
    }
    
    /**
     * Removes a child category
     */
    public void removeChild(UUID childId) {
        if (childId == null) {
            throw new IllegalArgumentException("Child ID cannot be null");
        }
        
        boolean removed = this.children.removeIf(child -> child.getId().equals(childId));
        
        if (removed) {
            this.updatedAt = Instant.now();
        }
    }
    
    /**
     * Updates the hierarchy information (level and path)
     */
    public void updateHierarchy(String parentPath, Integer parentLevel) {
        if (this.parentCategoryId == null) {
            // Root category
            this.level = 0;
            this.path = "/" + this.slug;
        } else {
            // Child category
            this.level = parentLevel != null ? parentLevel + 1 : 1;
            this.path = (parentPath != null ? parentPath : "") + "/" + this.slug;
        }
        
        this.updatedAt = Instant.now();
        
        // Update all children recursively
        for (DomainCategoryEntity child : this.children) {
            child.updateHierarchy(this.path, this.level);
        }
    }
    
    /**
     * Checks if this category is a root category
     */
    public boolean isRoot() {
        return this.parentCategoryId == null;
    }
    
    /**
     * Checks if this category has children
     */
    public boolean hasChildren() {
        return !this.children.isEmpty();
    }
    
    /**
     * Gets the depth of this category in the hierarchy
     */
    public int getDepth() {
        return this.level != null ? this.level : 0;
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedCategoryDTO toDTO() {
        return SharedCategoryDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainCategoryEntity fromDTO(SharedCategoryDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainCategoryEntity.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .slug(dto.getSlug())
            .parentCategoryId(dto.getParentCategoryId())
            .level(dto.getLevel())
            .path(dto.getPath())
            .sortOrder(dto.getSortOrder())
            .isActive(dto.getIsActive())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .createdBy(dto.getCreatedBy())
            .updatedBy(dto.getUpdatedBy())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a new category
     */
    public static DomainCategoryEntity create(String name, String slug, String description, 
                                            UUID parentCategoryId, Integer sortOrder, String createdBy) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be null or empty");
        }
        
        Instant now = Instant.now();
        
        DomainCategoryEntity category = DomainCategoryEntity.builder()
            .id(UUID.randomUUID())
            .name(name)
            .description(description)
            .slug(slug)
            .parentCategoryId(parentCategoryId)
            .level(parentCategoryId == null ? 0 : 1) // Will be recalculated by service
            .path("/" + slug) // Will be recalculated by service
            .sortOrder(sortOrder != null ? sortOrder : 0)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy)
            .updatedBy(createdBy)
            .version(0L)
            .build();
        
        category.validateHierarchy();
        return category;
    }
}