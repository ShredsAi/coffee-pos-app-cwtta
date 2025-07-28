package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedAttributeType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedProductAttributeDTO {
    
    private UUID id;
    
    @NotBlank(message = "Attribute name is required")
    @Size(max = 255, message = "Attribute name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Attribute code is required")
    @Size(max = 50, message = "Attribute code must not exceed 50 characters")
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
    
    @Builder.Default
    private List<SharedAttributeOptionDTO> options = new ArrayList<>();
    
    @Builder.Default
    private Boolean isActive = true;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Long version;

    // TODO: Implementation of toEntity() and fromEntity() methods will be added
    // after domain layer is available for proper transformation
    public Object toEntity() {
        throw new UnsupportedOperationException("toEntity() method will be implemented when domain layer is available");
    }

    public static SharedProductAttributeDTO fromEntity(Object entity) {
        throw new UnsupportedOperationException("fromEntity() method will be implemented when domain layer is available");
    }
}