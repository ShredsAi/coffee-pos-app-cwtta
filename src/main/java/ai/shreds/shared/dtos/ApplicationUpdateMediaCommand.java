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
public class ApplicationUpdateMediaCommand {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Media ID is required")
    private UUID mediaId;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Builder.Default
    private Boolean isPrimary = false;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
    
    // TODO: Implementation of toDomainCommand() method will be added
    // after domain layer is available for proper transformation
    public Object toDomainCommand() {
        throw new UnsupportedOperationException("toDomainCommand() method will be implemented when domain layer is available");
    }
    
    public static ApplicationUpdateMediaCommand fromRequest(UUID productId, UUID mediaId, SharedMediaUpdateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationUpdateMediaCommand.builder()
                .productId(productId)
                .mediaId(mediaId)
                .altText(request.getAltText())
                .title(request.getTitle())
                .isPrimary(request.getIsPrimary())
                .sortOrder(request.getSortOrder())
                .version(request.getVersion())
                .build();
    }
}