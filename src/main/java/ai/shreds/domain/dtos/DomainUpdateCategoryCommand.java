package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Update Category Command
 * Command object containing all data needed to update an existing category
 * Used within the domain layer for category updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainUpdateCategoryCommand {
    
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private UUID parentCategoryId;
    private Integer sortOrder;
    private Boolean isActive;
    private Long version;
    private String updatedBy;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null for update");
        }
        
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null for update (optimistic locking)");
        }
        
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Updated by cannot be null or empty");
        }
        
        // Validate non-null fields
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        
        if (slug != null && slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be empty");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainUpdateCategoryCommand fromApplicationCommand(Object applicationCommand, String userId) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getId = applicationCommand.getClass().getMethod("getId");
            java.lang.reflect.Method getName = applicationCommand.getClass().getMethod("getName");
            java.lang.reflect.Method getDescription = applicationCommand.getClass().getMethod("getDescription");
            java.lang.reflect.Method getSlug = applicationCommand.getClass().getMethod("getSlug");
            java.lang.reflect.Method getParentCategoryId = applicationCommand.getClass().getMethod("getParentCategoryId");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            java.lang.reflect.Method getIsActive = applicationCommand.getClass().getMethod("getIsActive");
            java.lang.reflect.Method getVersion = applicationCommand.getClass().getMethod("getVersion");
            
            DomainUpdateCategoryCommand command = DomainUpdateCategoryCommand.builder()
                .id((UUID) getId.invoke(applicationCommand))
                .name((String) getName.invoke(applicationCommand))
                .description((String) getDescription.invoke(applicationCommand))
                .slug((String) getSlug.invoke(applicationCommand))
                .parentCategoryId((UUID) getParentCategoryId.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .isActive((Boolean) getIsActive.invoke(applicationCommand))
                .version((Long) getVersion.invoke(applicationCommand))
                .updatedBy(userId)
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}