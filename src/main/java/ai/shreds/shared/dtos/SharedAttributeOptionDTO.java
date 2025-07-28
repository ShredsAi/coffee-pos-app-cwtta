package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedAttributeOptionDTO {
    
    private UUID id;
    
    @NotNull(message = "Attribute ID is required")
    private UUID attributeId;
    
    @NotBlank(message = "Option value is required")
    @Size(max = 255, message = "Option value must not exceed 255 characters")
    private String value;
    
    @NotBlank(message = "Option code is required")
    @Size(max = 50, message = "Option code must not exceed 50 characters")
    private String code;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    private Long version;

    // TODO: Implementation of toEntity() and fromEntity() methods will be added
    // after domain layer is available for proper transformation
    public Object toEntity() {
        throw new UnsupportedOperationException("toEntity() method will be implemented when domain layer is available");
    }

    public static SharedAttributeOptionDTO fromEntity(Object entity) {
        throw new UnsupportedOperationException("fromEntity() method will be implemented when domain layer is available");
    }
}