package ai.shreds.domain.dtos;

import ai.shreds.domain.value_objects.DomainFileMetadataValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Upload Media Command
 * Command object containing all data needed to upload media to a product
 * Used within the domain layer for media upload operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainUploadMediaCommand {
    
    private UUID productId;
    private String url;
    private String fileName;
    private DomainFileMetadataValue fileMetadata;
    private String altText;
    private String title;
    private Boolean isPrimary;
    private Integer sortOrder;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        
        if (fileMetadata == null) {
            throw new IllegalArgumentException("File metadata cannot be null");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
        
        // Validate file metadata
        if (!fileMetadata.isValidForUpload()) {
            throw new IllegalArgumentException("File metadata is invalid for upload");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainUploadMediaCommand fromApplicationCommand(Object applicationCommand, String url, DomainFileMetadataValue metadata) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getProductId = applicationCommand.getClass().getMethod("getProductId");
            java.lang.reflect.Method getFileName = applicationCommand.getClass().getMethod("getFileName");
            java.lang.reflect.Method getAltText = applicationCommand.getClass().getMethod("getAltText");
            java.lang.reflect.Method getTitle = applicationCommand.getClass().getMethod("getTitle");
            java.lang.reflect.Method getIsPrimary = applicationCommand.getClass().getMethod("getIsPrimary");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            
            DomainUploadMediaCommand command = DomainUploadMediaCommand.builder()
                .productId((UUID) getProductId.invoke(applicationCommand))
                .url(url)
                .fileName((String) getFileName.invoke(applicationCommand))
                .fileMetadata(metadata)
                .altText((String) getAltText.invoke(applicationCommand))
                .title((String) getTitle.invoke(applicationCommand))
                .isPrimary((Boolean) getIsPrimary.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}