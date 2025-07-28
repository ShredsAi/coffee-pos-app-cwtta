package ai.shreds.domain.dtos;

import ai.shreds.domain.enums.DomainPublicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Domain Update Product Command
 * Command object containing all data needed to update an existing product
 * Used within the domain layer for product updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainUpdateProductCommand {
    
    private UUID id;
    private String name;
    private String description;
    private String shortDescription;
    private String brand;
    private String model;
    private String sku;
    private String slug;
    private DomainPublicationStatus publicationStatus;
    private List<UUID> categoryIds;
    private UUID primaryCategoryId;
    private Boolean isActive;
    private Long version;
    private String updatedBy;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null for update");
        }
        
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null for update (optimistic locking)");
        }
        
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Updated by cannot be null or empty");
        }
        
        // Validate non-null fields
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        
        if (sku != null && sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be empty");
        }
        
        if (slug != null && slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Product slug cannot be empty");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainUpdateProductCommand fromApplicationCommand(Object applicationCommand, String userId) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getId = applicationCommand.getClass().getMethod("getId");
            java.lang.reflect.Method getName = applicationCommand.getClass().getMethod("getName");
            java.lang.reflect.Method getDescription = applicationCommand.getClass().getMethod("getDescription");
            java.lang.reflect.Method getShortDescription = applicationCommand.getClass().getMethod("getShortDescription");
            java.lang.reflect.Method getBrand = applicationCommand.getClass().getMethod("getBrand");
            java.lang.reflect.Method getModel = applicationCommand.getClass().getMethod("getModel");
            java.lang.reflect.Method getSku = applicationCommand.getClass().getMethod("getSku");
            java.lang.reflect.Method getSlug = applicationCommand.getClass().getMethod("getSlug");
            java.lang.reflect.Method getCategoryIds = applicationCommand.getClass().getMethod("getCategoryIds");
            java.lang.reflect.Method getPrimaryCategoryId = applicationCommand.getClass().getMethod("getPrimaryCategoryId");
            java.lang.reflect.Method getPublicationStatus = applicationCommand.getClass().getMethod("getPublicationStatus");
            java.lang.reflect.Method getIsActive = applicationCommand.getClass().getMethod("getIsActive");
            java.lang.reflect.Method getVersion = applicationCommand.getClass().getMethod("getVersion");
            
            Object publicationStatusObj = getPublicationStatus.invoke(applicationCommand);
            
            DomainUpdateProductCommand command = DomainUpdateProductCommand.builder()
                .id((UUID) getId.invoke(applicationCommand))
                .name((String) getName.invoke(applicationCommand))
                .description((String) getDescription.invoke(applicationCommand))
                .shortDescription((String) getShortDescription.invoke(applicationCommand))
                .brand((String) getBrand.invoke(applicationCommand))
                .model((String) getModel.invoke(applicationCommand))
                .sku((String) getSku.invoke(applicationCommand))
                .slug((String) getSlug.invoke(applicationCommand))
                .categoryIds((List<UUID>) getCategoryIds.invoke(applicationCommand))
                .primaryCategoryId((UUID) getPrimaryCategoryId.invoke(applicationCommand))
                .publicationStatus(publicationStatusObj != null ? 
                    DomainPublicationStatus.valueOf(publicationStatusObj.toString()) : null)
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