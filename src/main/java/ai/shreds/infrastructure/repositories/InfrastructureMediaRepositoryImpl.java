package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.domain.ports.DomainOutputPortMediaRepository;
import ai.shreds.infrastructure.entities.InfrastructureProductMediaJpaEntity;
import ai.shreds.infrastructure.mappers.InfrastructureMediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the media repository.
 * Implements the domain output port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InfrastructureMediaRepositoryImpl implements DomainOutputPortMediaRepository {

    private final InfrastructureMediaJpaRepository mediaJpaRepository;
    private final InfrastructureMediaMapper mediaMapper;

    @Override
    @Transactional
    public DomainProductMediaEntity save(DomainProductMediaEntity media) {
        log.debug("Saving media with ID: {} for product ID: {}", media.getId(), media.getProductId());
        
        try {
            InfrastructureProductMediaJpaEntity jpaEntity;
            
            if (media.getId() != null && mediaJpaRepository.existsById(media.getId())) {
                // Update existing media
                jpaEntity = mediaJpaRepository.findById(media.getId())
                        .orElseThrow(() -> new IllegalStateException("Media not found for update: " + media.getId()));
                mediaMapper.mergeDomainToJpa(media, jpaEntity);
            } else {
                // Create new media
                jpaEntity = mediaMapper.toJpaEntity(media);
                if (jpaEntity.getId() == null) {
                    jpaEntity.setId(UUID.randomUUID());
                }
            }
            
            // Handle primary media constraint - only one media can be primary per product
            if (Boolean.TRUE.equals(jpaEntity.getIsPrimary())) {
                removePrimaryFlags(jpaEntity.getProductId());
            }
            
            InfrastructureProductMediaJpaEntity savedEntity = mediaJpaRepository.save(jpaEntity);
            DomainProductMediaEntity result = mediaMapper.toDomainEntity(savedEntity);
            
            log.debug("Successfully saved media with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error saving media with ID: {}", media.getId(), e);
            throw new RuntimeException("Failed to save media: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductMediaEntity> findById(UUID id) {
        log.debug("Finding media by ID: {}", id);
        
        return mediaJpaRepository.findById(id)
                .map(mediaMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainProductMediaEntity> findByProductId(UUID productId) {
        log.debug("Finding media by product ID: {}", productId);
        
        return mediaJpaRepository.findByProductIdOrderBySortOrder(productId)
                .stream()
                .map(mediaMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting media with ID: {}", id);
        
        try {
            Optional<InfrastructureProductMediaJpaEntity> mediaOpt = mediaJpaRepository.findById(id);
            if (mediaOpt.isEmpty()) {
                log.warn("Attempted to delete non-existent media: {}", id);
                return;
            }
            
            InfrastructureProductMediaJpaEntity media = mediaOpt.get();
            UUID productId = media.getProductId();
            boolean wasPrimary = Boolean.TRUE.equals(media.getIsPrimary());
            
            // Delete the media
            mediaJpaRepository.deleteById(id);
            
            // If this was the primary media, set another media as primary if available
            if (wasPrimary) {
                setNewPrimaryMediaIfAvailable(productId);
            }
            
            log.debug("Successfully deleted media with ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting media with ID: {}", id, e);
            throw new RuntimeException("Failed to delete media: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateOrder(UUID productId, Map<UUID, Integer> mediaOrder) {
        log.debug("Updating media order for product ID: {} with {} items", productId, mediaOrder.size());
        
        try {
            // Use the updateOrder method defined in the JPA repository interface
            mediaJpaRepository.updateOrder(productId, mediaOrder);
            
            log.debug("Successfully updated media order for product ID: {}", productId);
            
        } catch (Exception e) {
            log.error("Error updating media order for product ID: {}", productId, e);
            throw new RuntimeException("Failed to update media order: " + e.getMessage(), e);
        }
    }

    /**
     * Additional methods required by domain interface but not explicitly defined in UML
     */
     
    @Transactional(readOnly = true)
    public Optional<DomainProductMediaEntity> findPrimaryByProductId(UUID productId) {
        log.debug("Finding primary media for product ID: {}", productId);
        
        return mediaJpaRepository.findByProductIdAndIsPrimaryTrue(productId)
                .map(mediaMapper::toDomainEntity);
    }

    @Transactional(readOnly = true)
    public long countByProductId(UUID productId) {
        log.debug("Counting media for product ID: {}", productId);
        
        return mediaJpaRepository.countByProductId(productId);
    }

    @Transactional
    public void removePrimaryFlags(UUID productId) {
        log.debug("Removing primary flags for product ID: {}", productId);
        
        try {
            mediaJpaRepository.removePrimaryFlagForProduct(productId);
            log.debug("Successfully removed primary flags for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error removing primary flags for product ID: {}", productId, e);
            throw new RuntimeException("Failed to remove primary flags: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void setPrimary(UUID productId, UUID mediaId) {
        log.debug("Setting media {} as primary for product {}", mediaId, productId);
        
        try {
            // First remove primary flag from all media for the product
            removePrimaryFlags(productId);
            
            // Then set the specified media as primary
            mediaJpaRepository.setPrimaryMedia(productId, mediaId);
            
            log.debug("Successfully set media {} as primary for product {}", mediaId, productId);
        } catch (Exception e) {
            log.error("Error setting media {} as primary for product {}", mediaId, productId, e);
            throw new RuntimeException("Failed to set primary media: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteAllByProductId(UUID productId) {
        log.debug("Deleting all media for product ID: {}", productId);
        
        try {
            mediaJpaRepository.deleteByProductId(productId);
            log.debug("Successfully deleted all media for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error deleting all media for product ID: {}", productId, e);
            throw new RuntimeException("Failed to delete all media for product: " + e.getMessage(), e);
        }
    }

    /**
     * Additional helper methods for the infrastructure layer
     */
    
    /**
     * Finds images for a product
     * @param productId the product ID
     * @return list of image media
     */
    public List<DomainProductMediaEntity> findImagesByProductId(UUID productId) {
        return mediaJpaRepository.findImagesByProductId(productId)
                .stream()
                .map(mediaMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds videos for a product
     * @param productId the product ID
     * @return list of video media
     */
    public List<DomainProductMediaEntity> findVideosByProductId(UUID productId) {
        return mediaJpaRepository.findVideosByProductId(productId)
                .stream()
                .map(mediaMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds documents for a product
     * @param productId the product ID
     * @return list of document media
     */
    public List<DomainProductMediaEntity> findDocumentsByProductId(UUID productId) {
        return mediaJpaRepository.findDocumentsByProductId(productId)
                .stream()
                .map(mediaMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if a product has any media
     * @param productId the product ID
     * @return true if the product has media
     */
    public boolean hasMedia(UUID productId) {
        return mediaJpaRepository.existsByProductId(productId);
    }
    
    /**
     * Finds media by URL
     * @param url the media URL
     * @return Optional containing the media if found
     */
    public Optional<DomainProductMediaEntity> findByUrl(String url) {
        return mediaJpaRepository.findByUrl(url)
                .map(mediaMapper::toDomainEntity);
    }
    
    /**
     * Gets the next sort order for a product's media
     * @param productId the product ID
     * @return the next available sort order
     */
    public int getNextSortOrder(UUID productId) {
        return mediaJpaRepository.findMaxSortOrderForProduct(productId)
                .map(maxOrder -> maxOrder + 1)
                .orElse(0);
    }
    
    /**
     * Sets a new primary media if available when the current primary is deleted
     * @param productId the product ID
     */
    private void setNewPrimaryMediaIfAvailable(UUID productId) {
        List<InfrastructureProductMediaJpaEntity> remainingMedia = 
                mediaJpaRepository.findByProductIdOrderBySortOrder(productId);
        
        if (!remainingMedia.isEmpty()) {
            // Set the first media (by sort order) as primary
            InfrastructureProductMediaJpaEntity firstMedia = remainingMedia.get(0);
            setPrimary(productId, firstMedia.getId());
            log.debug("Set media {} as new primary for product {} after deletion", 
                    firstMedia.getId(), productId);
        }
    }
}