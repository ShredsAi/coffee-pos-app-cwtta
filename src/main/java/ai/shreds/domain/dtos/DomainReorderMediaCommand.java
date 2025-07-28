package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Domain Reorder Media Command
 * Command object containing all data needed to reorder media items for a product
 * Used within the domain layer for media reordering operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainReorderMediaCommand {
    
    private UUID productId;
    private Map<UUID, Integer> mediaOrder; // Map of media ID to sort order
    private UUID primaryMediaId;
    
    /**
     * Validates the command data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (mediaOrder == null || mediaOrder.isEmpty()) {
            throw new IllegalArgumentException("Media order map cannot be null or empty");
        }
        
        // Validate that all sort orders are non-negative
        for (Map.Entry<UUID, Integer> entry : mediaOrder.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Media ID cannot be null in order map");
            }
            if (entry.getValue() == null || entry.getValue() < 0) {
                throw new IllegalArgumentException("Sort order cannot be null or negative");
            }
        }
        
        // Validate that primary media ID is in the order map if specified
        if (primaryMediaId != null && !mediaOrder.containsKey(primaryMediaId)) {
            throw new IllegalArgumentException("Primary media ID must be included in the media order map");
        }
    }
    
    /**
     * Factory method to create command from application layer command
     */
    public static DomainReorderMediaCommand fromApplicationCommand(Object applicationCommand) {
        if (applicationCommand == null) {
            throw new IllegalArgumentException("Application command cannot be null");
        }
        
        try {
            java.lang.reflect.Method getProductId = applicationCommand.getClass().getMethod("getProductId");
            java.lang.reflect.Method getMediaOrder = applicationCommand.getClass().getMethod("getMediaOrder");
            java.lang.reflect.Method getPrimaryMediaId = applicationCommand.getClass().getMethod("getPrimaryMediaId");
            
            DomainReorderMediaCommand command = DomainReorderMediaCommand.builder()
                .productId((UUID) getProductId.invoke(applicationCommand))
                .mediaOrder((Map<UUID, Integer>) getMediaOrder.invoke(applicationCommand))
                .primaryMediaId((UUID) getPrimaryMediaId.invoke(applicationCommand))
                .build();
            
            command.validate();
            return command;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain command from application command", e);
        }
    }
}