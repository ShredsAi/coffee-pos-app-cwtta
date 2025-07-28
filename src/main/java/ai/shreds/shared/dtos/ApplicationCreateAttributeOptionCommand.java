package ai.shreds.shared.dtos;

import ai.shreds.domain.dtos.DomainCreateOptionCommand;
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
public class ApplicationCreateAttributeOptionCommand {
    
    @NotNull(message = "Attribute ID is required")
    private UUID attributeId;
    
    @NotBlank(message = "Option value is required")
    @Size(max = 255, message = "Option value must not exceed 255 characters")
    private String value;
    
    @NotBlank(message = "Option code is required")
    @Size(max = 50, message = "Option code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Code can only contain letters, numbers, underscores, and hyphens")
    private String code;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * Converts application command to domain command
     * @return DomainCreateOptionCommand for domain layer processing
     */
    public DomainCreateOptionCommand toDomainCommand() {
        return DomainCreateOptionCommand.fromApplicationCommand(this);
    }
    
    public static ApplicationCreateAttributeOptionCommand fromRequest(UUID attributeId, SharedAttributeOptionCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationCreateAttributeOptionCommand.builder()
                .attributeId(attributeId)
                .value(request.getValue())
                .code(request.getCode())
                .sortOrder(request.getSortOrder())
                .build();
    }
}