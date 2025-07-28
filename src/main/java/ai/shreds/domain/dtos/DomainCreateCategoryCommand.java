package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Create Category Command
 * Command object containing all data needed to create a new category
 * Used within the domain layer for category creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCreateCategoryCommand {
    
    private String name;
    private String description;
    private String slug;
    private UUID parentCategoryId;
    private Integer sortOrder;
    private String createdBy;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be null or empty");
        }
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainCreateCategoryCommand fromApplicationCommand(Object applicationCommand, String userId) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getName = applicationCommand.getClass().getMethod("getName");
            java.lang.reflect.Method getDescription = applicationCommand.getClass().getMethod("getDescription");
            java.lang.reflect.Method getSlug = applicationCommand.getClass().getMethod("getSlug");
            java.lang.reflect.Method getParentCategoryId = applicationCommand.getClass().getMethod("getParentCategoryId");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            
            DomainCreateCategoryCommand command = DomainCreateCategoryCommand.builder()
                .name((String) getName.invoke(applicationCommand))
                .description((String) getDescription.invoke(applicationCommand))
                .slug((String) getSlug.invoke(applicationCommand))
                .parentCategoryId((UUID) getParentCategoryId.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .createdBy(userId)
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}