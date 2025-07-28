package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainProductCategoryEntity;
import ai.shreds.domain.entities.DomainProductAttributeValueEntity;
import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.shared.enums.SharedPublicationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedProductDTO {
    
    private UUID id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
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
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;
    
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;
    
    @NotNull(message = "Publication status is required")
    private SharedPublicationStatus publicationStatus;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private List<SharedProductAttributeValueDTO> attributes = new ArrayList<>();
    
    @Builder.Default
    private List<SharedCategoryDTO> categories = new ArrayList<>();
    
    private UUID primaryCategoryId;
    
    @Builder.Default
    private List<SharedProductMediaDTO> media = new ArrayList<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Long version;

    /**
     * Converts this DTO to a domain entity
     * @return a new domain entity
     */
    public DomainProductEntity toEntity() {
        return DomainProductEntity.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .shortDescription(this.shortDescription)
                .brand(this.brand)
                .model(this.model)
                .sku(this.sku)
                .slug(this.slug)
                .publicationStatus(this.publicationStatus != null ? 
                        ai.shreds.domain.enums.DomainPublicationStatus.valueOf(this.publicationStatus.name()) : null)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .version(this.version)
                .build();
    }

    /**
     * Creates a DTO from a domain entity
     * @param entity the domain entity
     * @return a new DTO
     */
    public static SharedProductDTO fromEntity(DomainProductEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SharedProductDTO dto = SharedProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .sku(entity.getSku())
                .slug(entity.getSlug())
                .publicationStatus(entity.getPublicationStatus() != null ? 
                        SharedPublicationStatus.valueOf(entity.getPublicationStatus().name()) : null)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();
        
        // Find primary category ID
        if (entity.getCategories() != null && !entity.getCategories().isEmpty()) {
            for (DomainProductCategoryEntity categoryEntity : entity.getCategories()) {
                if (Boolean.TRUE.equals(categoryEntity.getIsPrimary())) {
                    dto.setPrimaryCategoryId(categoryEntity.getCategoryId());
                    break;
                }
            }
        }
        
        // Convert attribute values if present
        if (entity.getAttributes() != null && !entity.getAttributes().isEmpty()) {
            List<SharedProductAttributeValueDTO> attributeDTOs = entity.getAttributes().stream()
                .map(SharedProductAttributeValueDTO::fromEntity)
                .collect(Collectors.toList());
            dto.setAttributes(attributeDTOs);
        }
        
        // Convert categories if present
        if (entity.getCategories() != null && !entity.getCategories().isEmpty()) {
            List<SharedCategoryDTO> categoryDTOs = entity.getCategories().stream()
                .filter(cat -> cat.getCategory() != null)
                .map(cat -> SharedCategoryDTO.fromEntity(cat.getCategory()))
                .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);
        }
        
        // Convert media if present
        if (entity.getMedia() != null && !entity.getMedia().isEmpty()) {
            List<SharedProductMediaDTO> mediaDTOs = entity.getMedia().stream()
                .map(SharedProductMediaDTO::fromEntity)
                .collect(Collectors.toList());
            dto.setMedia(mediaDTOs);
        }
        
        return dto;
    }
}