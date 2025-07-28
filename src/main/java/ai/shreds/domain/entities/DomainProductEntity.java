package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainPublicationStatus;
import ai.shreds.shared.dtos.SharedProductDTO;
import ai.shreds.domain.dtos.DomainCreateProductCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain Product Entity - Aggregate Root
 * Represents the core product in the catalog system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainProductEntity {
    
    private UUID id;
    private String name;
    private String description;
    private String shortDescription;
    private String brand;
    private String model;
    private String sku;
    private String slug;
    private DomainPublicationStatus publicationStatus;
    private Boolean isActive;
    
    @Builder.Default
    private List<DomainProductAttributeValueEntity> attributes = new ArrayList<>();
    
    @Builder.Default
    private List<DomainProductCategoryEntity> categories = new ArrayList<>();
    
    @Builder.Default
    private List<DomainProductMediaEntity> media = new ArrayList<>();
    
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    
    /**
     * Validates business rules for product uniqueness
     */
    public void validateUniqueness() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be null or empty");
        }
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Product slug cannot be null or empty");
        }
    }
    
    /**
     * Changes the publication status with validation
     */
    public void changePublicationStatus(DomainPublicationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Publication status cannot be null");
        }
        
        if (this.publicationStatus != null && !this.publicationStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.publicationStatus, newStatus)
            );
        }
        
        this.publicationStatus = newStatus;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adds an attribute value to the product
     */
    public void addAttributeValue(DomainProductAttributeValueEntity value) {
        if (value == null) {
            throw new IllegalArgumentException("Attribute value cannot be null");
        }
        
        value.validateAgainstAttribute();
        
        // Remove existing value for the same attribute
        this.attributes.removeIf(existing -> 
            existing.getAttributeId().equals(value.getAttributeId())
        );
        
        this.attributes.add(value);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adds a category to the product
     */
    public void addCategory(DomainProductCategoryEntity category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        // Check if category already exists
        boolean exists = this.categories.stream()
            .anyMatch(existing -> existing.getCategoryId().equals(category.getCategoryId()));
        
        if (!exists) {
            this.categories.add(category);
            this.updatedAt = Instant.now();
        }
    }
    
    /**
     * Sets the primary category for the product
     */
    public void setPrimaryCategory(UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        
        // Find the category association
        DomainProductCategoryEntity targetCategory = this.categories.stream()
            .filter(cat -> cat.getCategoryId().equals(categoryId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Category not associated with product"));
        
        // Reset all categories to non-primary
        this.categories.forEach(cat -> cat.setIsPrimary(false));
        
        // Set the target category as primary
        targetCategory.setIsPrimary(true);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adds media to the product
     */
    public void addMedia(DomainProductMediaEntity mediaEntity) {
        if (mediaEntity == null) {
            throw new IllegalArgumentException("Media entity cannot be null");
        }
        
        mediaEntity.validateFileSize();
        mediaEntity.validateMimeType();
        
        this.media.add(mediaEntity);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Sets the primary media for the product
     */
    public void setPrimaryMedia(UUID mediaId) {
        if (mediaId == null) {
            throw new IllegalArgumentException("Media ID cannot be null");
        }
        
        // Find the media
        DomainProductMediaEntity targetMedia = this.media.stream()
            .filter(med -> med.getId().equals(mediaId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Media not found in product"));
        
        // Reset all media to non-primary
        this.media.forEach(med -> med.setIsPrimary(false));
        
        // Set the target media as primary
        targetMedia.setIsPrimary(true);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedProductDTO toDTO() {
        return SharedProductDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainProductEntity fromDTO(SharedProductDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainProductEntity.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .shortDescription(dto.getShortDescription())
            .brand(dto.getBrand())
            .model(dto.getModel())
            .sku(dto.getSku())
            .slug(dto.getSlug())
            .publicationStatus(DomainPublicationStatus.valueOf(dto.getPublicationStatus().name()))
            .isActive(dto.getIsActive())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .createdBy(dto.getCreatedBy())
            .updatedBy(dto.getUpdatedBy())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a draft product from command
     */
    public static DomainProductEntity createDraft(DomainCreateProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create command cannot be null");
        }
        
        Instant now = Instant.now();
        UUID productId = UUID.randomUUID();
        
        DomainProductEntity product = DomainProductEntity.builder()
            .id(productId)
            .name(command.getName())
            .description(command.getDescription())
            .shortDescription(command.getShortDescription())
            .brand(command.getBrand())
            .model(command.getModel())
            .sku(command.getSku())
            .slug(command.getSlug())
            .publicationStatus(command.getPublicationStatus())
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(command.getCreatedBy())
            .updatedBy(command.getCreatedBy())
            .version(0L)
            .build();
        
        product.validateUniqueness();
        return product;
    }
}