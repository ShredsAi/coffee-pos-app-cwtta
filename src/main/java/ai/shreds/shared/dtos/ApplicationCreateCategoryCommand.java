package ai.shreds.shared.dtos;

import ai.shreds.domain.dtos.DomainCreateCategoryCommand;
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
public class ApplicationCreateCategoryCommand {
    
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
    
    /**
     * Converts this application command to a domain command
     */
    public DomainCreateCategoryCommand toDomainCommand() {
        String currentUser = getCurrentUser();
        return DomainCreateCategoryCommand.fromApplicationCommand(this, currentUser);
    }
    
    /**
     * Factory method to create command from shared request
     */
    public static ApplicationCreateCategoryCommand fromRequest(SharedCategoryCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationCreateCategoryCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .parentCategoryId(request.getParentCategoryId())
                .sortOrder(request.getSortOrder())
                .build();
    }
    
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}