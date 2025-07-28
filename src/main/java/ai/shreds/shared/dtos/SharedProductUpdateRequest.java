package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedPublicationStatus;
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
public class SharedProductUpdateRequest {
    
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
    
    @Builder.Default
    private Boolean isActive = true;
    
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
    
    // TODO: Implementation of toCommand() method will be added
    // after application layer is available for proper transformation
    public Object toCommand(UUID id) {
        throw new UnsupportedOperationException("toCommand() method will be implemented when application layer is available");
    }
}