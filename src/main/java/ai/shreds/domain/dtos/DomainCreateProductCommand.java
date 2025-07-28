package ai.shreds.domain.dtos;

import ai.shreds.domain.enums.DomainPublicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Domain Create Product Command
 * Command object containing all data needed to create a new product
 * Used within the domain layer for product creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCreateProductCommand {
    
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
    private String createdBy;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be null or empty");
        }
        
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Product slug cannot be null or empty");
        }
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        
        if (publicationStatus == null) {
            this.publicationStatus = DomainPublicationStatus.DRAFT;
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainCreateProductCommand fromApplicationCommand(Object applicationCommand, String userId) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
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
            
            Object publicationStatusObj = getPublicationStatus.invoke(applicationCommand);
            
            DomainCreateProductCommand command = DomainCreateProductCommand.builder()
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
                    DomainPublicationStatus.valueOf(publicationStatusObj.toString()) : DomainPublicationStatus.DRAFT)
                .createdBy(userId)
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}