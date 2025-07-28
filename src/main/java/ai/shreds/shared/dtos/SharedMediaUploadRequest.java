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
public class SharedMediaUploadRequest {
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Builder.Default
    private Boolean isPrimary = false;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    // TODO: Implementation of toCommand() method will be added
    // after application layer is available for proper transformation
    public Object toCommand(UUID productId, Object file) {
        throw new UnsupportedOperationException("toCommand() method will be implemented when application layer is available");
    }
}