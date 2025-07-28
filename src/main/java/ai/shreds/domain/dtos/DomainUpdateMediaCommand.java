package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Update Media Command
 * Command object containing all data needed to update media metadata
 * Used within the domain layer for media update operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainUpdateMediaCommand {
    
    private UUID productId;
    private UUID mediaId;
    private String altText;
    private String title;
    private Boolean isPrimary;
    private Integer sortOrder;
    private Long version;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (mediaId == null) {
            throw new IllegalArgumentException("Media ID cannot be null");
        }
        
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null for update (optimistic locking)");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainUpdateMediaCommand fromApplicationCommand(Object applicationCommand) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getProductId = applicationCommand.getClass().getMethod("getProductId");
            java.lang.reflect.Method getMediaId = applicationCommand.getClass().getMethod("getMediaId");
            java.lang.reflect.Method getAltText = applicationCommand.getClass().getMethod("getAltText");
            java.lang.reflect.Method getTitle = applicationCommand.getClass().getMethod("getTitle");
            java.lang.reflect.Method getIsPrimary = applicationCommand.getClass().getMethod("getIsPrimary");
            java.lang.reflect.Method getSortOrder = applicationCommand.getClass().getMethod("getSortOrder");
            java.lang.reflect.Method getVersion = applicationCommand.getClass().getMethod("getVersion");
            
            DomainUpdateMediaCommand command = DomainUpdateMediaCommand.builder()
                .productId((UUID) getProductId.invoke(applicationCommand))
                .mediaId((UUID) getMediaId.invoke(applicationCommand))
                .altText((String) getAltText.invoke(applicationCommand))
                .title((String) getTitle.invoke(applicationCommand))
                .isPrimary((Boolean) getIsPrimary.invoke(applicationCommand))
                .sortOrder((Integer) getSortOrder.invoke(applicationCommand))
                .version((Long) getVersion.invoke(applicationCommand))
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}