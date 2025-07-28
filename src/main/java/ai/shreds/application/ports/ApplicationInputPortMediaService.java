package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedProductMediaDTO;
import ai.shreds.shared.dtos.ApplicationUploadMediaCommand;
import ai.shreds.shared.dtos.ApplicationUpdateMediaCommand;
import ai.shreds.shared.dtos.ApplicationReorderMediaCommand;

import java.util.List;
import java.util.UUID;

/**
 * Input port for media-related operations in the application layer.
 * Defines the contract for product media management use cases.
 */
public interface ApplicationInputPortMediaService {
    
    /**
     * Uploads media for a product based on the provided command.
     * 
     * @param command the command containing media upload data
     * @return the uploaded media as DTO
     */
    SharedProductMediaDTO uploadMedia(ApplicationUploadMediaCommand command);
    
    /**
     * Updates existing media based on the provided command.
     * 
     * @param command the command containing media update data
     * @return the updated media as DTO
     */
    SharedProductMediaDTO updateMedia(ApplicationUpdateMediaCommand command);
    
    /**
     * Retrieves media by product and media identifiers.
     * 
     * @param productId the product identifier
     * @param mediaId the media identifier
     * @return the media as DTO
     */
    SharedProductMediaDTO getMedia(UUID productId, UUID mediaId);
    
    /**
     * Lists all media for a specific product.
     * 
     * @param productId the product identifier
     * @return list of media DTOs for the product
     */
    List<SharedProductMediaDTO> listProductMedia(UUID productId);
    
    /**
     * Deletes media by product and media identifiers.
     * 
     * @param productId the product identifier
     * @param mediaId the media identifier to delete
     */
    void deleteMedia(UUID productId, UUID mediaId);
    
    /**
     * Reorders media for a product based on the provided command.
     * 
     * @param command the command containing reorder data
     * @return list of reordered media DTOs
     */
    List<SharedProductMediaDTO> reorderMedia(ApplicationReorderMediaCommand command);
}