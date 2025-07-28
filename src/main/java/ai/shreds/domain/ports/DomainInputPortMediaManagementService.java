package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.domain.dtos.DomainUploadMediaCommand;
import ai.shreds.domain.dtos.DomainUpdateMediaCommand;
import ai.shreds.domain.dtos.DomainReorderMediaCommand;

import java.util.List;
import java.util.UUID;

/**
 * Domain Input Port Media Management Service
 * Interface for product media management operations within the domain
 * Implemented by domain service layer to handle media business logic
 */
public interface DomainInputPortMediaManagementService {
    
    /**
     * Uploads media for a product
     * 
     * @param command The media upload command with file data and metadata
     * @return The created media entity
     * @throws IllegalArgumentException if command is invalid
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainMediaValidationException if media validation fails
     */
    DomainProductMediaEntity uploadMedia(DomainUploadMediaCommand command);
    
    /**
     * Updates media metadata
     * 
     * @param command The media update command with new metadata
     * @return The updated media entity
     * @throws IllegalArgumentException if command is invalid
     * @throws ai.shreds.domain.exceptions.DomainMediaNotFoundException if media not found
     * @throws ai.shreds.domain.exceptions.DomainMediaValidationException if validation fails
     */
    DomainProductMediaEntity updateMedia(DomainUpdateMediaCommand command);
    
    /**
     * Gets a specific media item
     * 
     * @param productId The product ID
     * @param mediaId The media ID
     * @return The media entity
     * @throws IllegalArgumentException if productId or mediaId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainMediaNotFoundException if media not found
     */
    DomainProductMediaEntity getMedia(UUID productId, UUID mediaId);
    
    /**
     * Lists all media for a product
     * 
     * @param productId The product ID
     * @return List of media entities ordered by sort order
     * @throws IllegalArgumentException if productId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     */
    List<DomainProductMediaEntity> listProductMedia(UUID productId);
    
    /**
     * Deletes a media item
     * 
     * @param productId The product ID
     * @param mediaId The media ID to delete
     * @throws IllegalArgumentException if productId or mediaId is null
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainMediaNotFoundException if media not found
     */
    void deleteMedia(UUID productId, UUID mediaId);
    
    /**
     * Reorders media items for a product
     * 
     * @param command The reorder command with new ordering
     * @return List of reordered media entities
     * @throws IllegalArgumentException if command is invalid
     * @throws ai.shreds.domain.exceptions.DomainProductNotFoundException if product not found
     * @throws ai.shreds.domain.exceptions.DomainMediaNotFoundException if any media not found
     */
    List<DomainProductMediaEntity> reorderMedia(DomainReorderMediaCommand command);
}