package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedMediaType;
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
public class SharedProductMediaDTO {
    
    private UUID id;
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;
    
    @NotBlank(message = "URL is required")
    @Size(max = 1000, message = "URL must not exceed 1000 characters")
    private String url;
    
    @NotNull(message = "Media type is required")
    private SharedMediaType mediaType;
    
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Min(value = 1, message = "Width must be positive")
    private Integer width;
    
    @Min(value = 1, message = "Height must be positive")
    private Integer height;
    
    @Min(value = 0, message = "File size must be non-negative")
    private Long fileSize;
    
    @Builder.Default
    private Boolean isPrimary = false;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant uploadedAt;
    
    private Long version;

    // TODO: Implementation of toEntity() and fromEntity() methods will be added
    // after domain layer is available for proper transformation
    public Object toEntity() {
        throw new UnsupportedOperationException("toEntity() method will be implemented when domain layer is available");
    }

    public static SharedProductMediaDTO fromEntity(Object entity) {
        throw new UnsupportedOperationException("fromEntity() method will be implemented when domain layer is available");
    }
    
    // Getter for isPrimary to handle Boolean properly
    public Boolean getIsPrimary() {
        return isPrimary != null ? isPrimary : false;
    }
}