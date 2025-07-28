package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainProductMediaEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Output Port Media Repository
 * Interface for media persistence operations
 * Implemented by infrastructure layer to handle media data access
 */
public interface DomainOutputPortMediaRepository {
    
    /**
     * Saves a media entity (for create or update)
     * 
     * @param media The media entity to save
     * @return The saved media entity with any generated IDs, timestamps, etc.
     */
    DomainProductMediaEntity save(DomainProductMediaEntity media);
    
    /**
     * Finds a media entity by its ID
     * 
     * @param id The media ID to find
     * @return Optional containing the media if found
     */
    Optional<DomainProductMediaEntity> findById(UUID id);
    
    /**
     * Finds all media for a specific product
     * 
     * @param productId The product ID to find media for
     * @return List of media entities ordered by sort order
     */
    List<DomainProductMediaEntity> findByProductId(UUID productId);
    
    /**
     * Deletes a media entity by its ID
     * 
     * @param id The media ID to delete
     */
    void delete(UUID id);
    
    /**
     * Updates the sort order for multiple media items
     * 
     * @param productId The product ID
     * @param mediaOrder Map of media ID to new sort order
     */
    void updateOrder(UUID productId, Map<UUID, Integer> mediaOrder);
    
    /**
     * Finds the primary media for a product
     * 
     * @param productId The product ID
     * @return Optional containing the primary media if found
     */
    Optional<DomainProductMediaEntity> findPrimaryByProductId(UUID productId);
    
    /**
     * Counts media items for a product
     * 
     * @param productId The product ID
     * @return Number of media items for the product
     */
    long countByProductId(UUID productId);
    
    /**
     * Removes primary flag from all media of a product
     * 
     * @param productId The product ID
     */
    void removePrimaryFlags(UUID productId);
    
    /**
     * Sets a media item as primary for a product
     * 
     * @param productId The product ID
     * @param mediaId The media ID to set as primary
     */
    void setPrimary(UUID productId, UUID mediaId);
    
    /**
     * Deletes all media for a product
     * 
     * @param productId The product ID
     */
    void deleteAllByProductId(UUID productId);
}