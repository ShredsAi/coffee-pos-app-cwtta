package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Create Option Command
 * Command object containing all data needed to create a new attribute option
 * Used within the domain layer for option creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCreateOptionCommand {
    
    private UUID attributeId;
    private String value;
    private String code;
    private Integer sortOrder;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (attributeId == null) {
            throw new IllegalArgumentException("Attribute ID cannot be null");
        }
        
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Option value cannot be null or empty");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainCreateOptionCommand fromApplicationCommand(Object applicationCommand) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getAttributeId = applicationCommand.getClass().getMethod("getAttributeId");
            java.lang.reflect.Method getValue = applicationCommand.getClass().getMethod("getValue");
            java.lang.reflect.Method getCode = applicationCommand.getClass().getMethod("getCode");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            
            DomainCreateOptionCommand command = DomainCreateOptionCommand.builder()
                .attributeId((UUID) getAttributeId.invoke(applicationCommand))
                .value((String) getValue.invoke(applicationCommand))
                .code((String) getCode.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}