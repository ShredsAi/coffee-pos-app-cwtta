package ai.shreds.shared.dtos;

import ai.shreds.domain.dtos.DomainUpdateCategoryCommand;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUpdateCategoryCommand {
    
    @NotNull(message = "Category ID is required")
    private UUID id;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;
    
    private UUID parentCategoryId;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
    
    /**
     * Converts this application command to a domain command
     */
    public DomainUpdateCategoryCommand toDomainCommand() {
        String currentUser = getCurrentUser();
        return DomainUpdateCategoryCommand.fromApplicationCommand(this, currentUser);
    }
    
    public static ApplicationUpdateCategoryCommand fromRequest(UUID id, SharedCategoryUpdateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationUpdateCategoryCommand.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .parentCategoryId(request.getParentCategoryId())
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .version(request.getVersion())
                .build();
    }
    
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}