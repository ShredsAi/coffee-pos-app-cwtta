package ai.shreds.shared.dtos;

import ai.shreds.domain.dtos.DomainUpdateAttributeCommand;
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
public class ApplicationUpdateAttributeCommand {
    
    @NotNull(message = "Attribute ID is required")
    private UUID id;
    
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
    
    /**
     * Converts application command to domain command
     * @return DomainUpdateAttributeCommand for domain layer processing
     */
    public DomainUpdateAttributeCommand toDomainCommand() {
        String currentUser = getCurrentUser();
        return DomainUpdateAttributeCommand.fromApplicationCommand(this, currentUser);
    }
    
    /**
     * Gets the current authenticated user
     * @return current user name or "system" as fallback
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
    
    public static ApplicationUpdateAttributeCommand fromRequest(UUID id, SharedAttributeUpdateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationUpdateAttributeCommand.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .isRequired(request.getIsRequired())
                .isFilterable(request.getIsFilterable())
                .isSearchable(request.getIsSearchable())
                .unit(request.getUnit())
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .version(request.getVersion())
                .build();
    }
}