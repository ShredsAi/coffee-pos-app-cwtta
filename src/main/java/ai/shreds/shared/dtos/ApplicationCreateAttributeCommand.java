package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedAttributeType;
import ai.shreds.domain.dtos.DomainCreateAttributeCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateAttributeCommand {
    
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
    
    /**
     * Converts application command to domain command
     * @return DomainCreateAttributeCommand for domain layer processing
     */
    public DomainCreateAttributeCommand toDomainCommand() {
        String currentUser = getCurrentUser();
        return DomainCreateAttributeCommand.fromApplicationCommand(this, currentUser);
    }
    
    /**
     * Gets the current authenticated user
     * @return current user name or "system" as fallback
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
    
    public static ApplicationCreateAttributeCommand fromRequest(SharedAttributeCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationCreateAttributeCommand.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .attributeType(request.getAttributeType())
                .isRequired(request.getIsRequired())
                .isFilterable(request.getIsFilterable())
                .isSearchable(request.getIsSearchable())
                .unit(request.getUnit())
                .sortOrder(request.getSortOrder())
                .options(request.getOptions())
                .build();
    }
}