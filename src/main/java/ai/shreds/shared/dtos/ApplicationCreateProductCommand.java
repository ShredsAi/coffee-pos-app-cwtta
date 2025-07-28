package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedPublicationStatus;
import ai.shreds.domain.dtos.DomainCreateProductCommand;
import ai.shreds.domain.enums.DomainPublicationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateProductCommand {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;
    
    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_\\-\\.]+$", message = "SKU can only contain letters, numbers, underscores, hyphens, and dots")
    private String sku;
    
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;
    
    @NotNull(message = "Publication status is required")
    private SharedPublicationStatus publicationStatus;
    
    @Valid
    @Builder.Default
    private List<SharedAttributeValueRequest> attributes = new ArrayList<>();
    
    @NotEmpty(message = "At least one category must be selected")
    @Builder.Default
    private List<UUID> categoryIds = new ArrayList<>();
    
    private UUID primaryCategoryId;
    
    /**
     * Converts this application command to a domain command
     * @return DomainCreateProductCommand
     */
    public DomainCreateProductCommand toDomainCommand() {
        return DomainCreateProductCommand.builder()
                .name(this.name)
                .description(this.description)
                .shortDescription(this.shortDescription)
                .brand(this.brand)
                .model(this.model)
                .sku(this.sku)
                .slug(this.slug)
                .publicationStatus(mapPublicationStatus(this.publicationStatus))
                .categoryIds(this.categoryIds)
                .primaryCategoryId(this.primaryCategoryId)
                .createdBy("system") // Will be set properly by the service layer
                .build();
    }
    
    /**
     * Maps SharedPublicationStatus to DomainPublicationStatus
     */
    private DomainPublicationStatus mapPublicationStatus(SharedPublicationStatus sharedStatus) {
        if (sharedStatus == null) {
            return DomainPublicationStatus.DRAFT;
        }
        
        switch (sharedStatus) {
            case DRAFT:
                return DomainPublicationStatus.DRAFT;
            case REVIEW:
                return DomainPublicationStatus.REVIEW;
            case PUBLISHED:
                return DomainPublicationStatus.PUBLISHED;
            case ARCHIVED:
                return DomainPublicationStatus.ARCHIVED;
            default:
                return DomainPublicationStatus.DRAFT;
        }
    }
    
    public static ApplicationCreateProductCommand fromRequest(SharedProductCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationCreateProductCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .brand(request.getBrand())
                .model(request.getModel())
                .sku(request.getSku())
                .slug(request.getSlug())
                .publicationStatus(request.getPublicationStatus())
                .attributes(request.getAttributes())
                .categoryIds(request.getCategoryIds())
                .primaryCategoryId(request.getPrimaryCategoryId())
                .build();
    }
}