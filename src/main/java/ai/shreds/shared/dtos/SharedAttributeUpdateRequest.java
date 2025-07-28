package ai.shreds.shared.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedAttributeUpdateRequest {
    
    @NotBlank(message = "Attribute name is required")
    @Size(max = 255, message = "Attribute name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Builder.Default
    private Boolean isRequired = false;
    
    @Builder.Default
    private Boolean isFilterable = true;
    
    @Builder.Default
    private Boolean isSearchable = false;
    
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
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