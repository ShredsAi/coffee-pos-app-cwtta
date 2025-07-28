package ai.shreds.domain.dtos;

import ai.shreds.domain.enums.DomainAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain Create Attribute Command
 * Command object containing all data needed to create a new attribute definition
 * Used within the domain layer for attribute creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCreateAttributeCommand {
    
    private String name;
    private String code;
    private String description;
    private DomainAttributeType attributeType;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Boolean isSearchable;
    private String unit;
    private Integer sortOrder;
    private List<DomainOptionCommand> options;
    private String createdBy;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
        
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute code cannot be null or empty");
        }
        
        if (attributeType == null) {
            throw new IllegalArgumentException("Attribute type cannot be null");
        }
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
        
        // Validate that select types have options
        if (attributeType.isSelectType() && (options == null || options.isEmpty())) {
            throw new IllegalArgumentException("Select-type attributes must have at least one option");
        }
        
        // Validate that non-select types don't have options
        if (!attributeType.isSelectType() && options != null && !options.isEmpty()) {
            throw new IllegalArgumentException("Options can only be specified for select-type attributes");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainCreateAttributeCommand fromApplicationCommand(Object applicationCommand, String userId) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getName = applicationCommand.getClass().getMethod("getName");
            java.lang.reflect.Method getCode = applicationCommand.getClass().getMethod("getCode");
            java.lang.reflect.Method getDescription = applicationCommand.getClass().getMethod("getDescription");
            java.lang.reflect.Method getAttributeType = applicationCommand.getClass().getMethod("getAttributeType");
            java.lang.reflect.Method getIsRequired = applicationCommand.getClass().getMethod("getIsRequired");
            java.lang.reflect.Method getIsFilterable = applicationCommand.getClass().getMethod("getIsFilterable");
            java.lang.reflect.Method getIsSearchable = applicationCommand.getClass().getMethod("getIsSearchable");
            java.lang.reflect.Method getUnit = applicationCommand.getClass().getMethod("getUnit");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            java.lang.reflect.Method getOptions = applicationCommand.getClass().getMethod("getOptions");
            
            Object attributeTypeObj = getAttributeType.invoke(applicationCommand);
            Object optionsObj = getOptions.invoke(applicationCommand);
            
            List<DomainOptionCommand> domainOptions = null;
            if (optionsObj instanceof List) {
                List<?> appOptions = (List<?>) optionsObj;
                domainOptions = appOptions.stream().map(opt -> {
                    try {
                        java.lang.reflect.Method getOptValue = opt.getClass().getMethod("getValue");
                        java.lang.reflect.Method getOptCode = opt.getClass().getMethod("getCode");
                        java.lang.reflect.Method getOptSortOrder = opt.getClass().getMethod("getSortOrder");
                        
                        return DomainOptionCommand.builder()
                            .value((String) getOptValue.invoke(opt))
                            .code((String) getOptCode.invoke(opt))
                            .sortOrder((Integer) getOptSortOrder.invoke(opt))
                            .build();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert option", e);
                    }
                }).toList();
            }
            
            DomainCreateAttributeCommand command = DomainCreateAttributeCommand.builder()
                .name((String) getName.invoke(applicationCommand))
                .code((String) getCode.invoke(applicationCommand))
                .description((String) getDescription.invoke(applicationCommand))
                .attributeType(attributeTypeObj != null ? 
                    DomainAttributeType.valueOf(attributeTypeObj.toString()) : null)
                .isRequired((Boolean) getIsRequired.invoke(applicationCommand))
                .isFilterable((Boolean) getIsFilterable.invoke(applicationCommand))
                .isSearchable((Boolean) getIsSearchable.invoke(applicationCommand))
                .unit((String) getUnit.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .options(domainOptions)
                .createdBy(userId)
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}