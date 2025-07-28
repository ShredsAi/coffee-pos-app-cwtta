package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedAttributeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedAttributeCreateRequest {
    
    @NotBlank(message = "Attribute name is required")
    @Size(max = 255, message = "Attribute name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Attribute code is required")
    @Size(max = 50, message = "Attribute code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Code can only contain letters, numbers, underscores, and hyphens")
    private String code;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Attribute type is required")
    private SharedAttributeType attributeType;
    
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
    
    @Valid
    @Builder.Default
    private List<SharedAttributeOptionRequest> options = new ArrayList<>();
    
    // TODO: Implementation of toCommand() method will be added
    // after application layer is available for proper transformation
    public Object toCommand() {
        throw new UnsupportedOperationException("toCommand() method will be implemented when application layer is available");
    }
}