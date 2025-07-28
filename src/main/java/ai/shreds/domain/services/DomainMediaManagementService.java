package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.ports.DomainInputPortMediaManagementService;
import ai.shreds.domain.ports.DomainOutputPortMediaRepository;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditWriter;
import ai.shreds.domain.exceptions.DomainProductNotFoundException;
import ai.shreds.domain.exceptions.DomainMediaValidationException;
import ai.shreds.domain.exceptions.DomainMediaNotFoundException;
import ai.shreds.domain.value_objects.DomainAuditEntry;
import ai.shreds.domain.dtos.DomainUploadMediaCommand;
import ai.shreds.domain.dtos.DomainUpdateMediaCommand;
import ai.shreds.domain.dtos.DomainReorderMediaCommand;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain Media Management Service
 * Implements the domain business logic for product media management
 */
@Service
public class DomainMediaManagementService implements DomainInputPortMediaManagementService {

    private final DomainOutputPortMediaRepository mediaRepository;
    private final DomainOutputPortProductRepository productRepository;
    private final DomainOutputPortAuditWriter auditWriter;

    /**
     * Constructor with dependencies
     */
    public DomainMediaManagementService(
            DomainOutputPortMediaRepository mediaRepository,
            DomainOutputPortProductRepository productRepository,
            DomainOutputPortAuditWriter auditWriter) {
        this.mediaRepository = mediaRepository;
        this.productRepository = productRepository;
        this.auditWriter = auditWriter;
    }

    @Override
    public DomainProductMediaEntity uploadMedia(DomainUploadMediaCommand command) {
        // Validate that product exists
        DomainProductEntity product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainProductNotFoundException(command.getProductId()));
        
        // Create media entity
        DomainProductMediaEntity media = DomainProductMediaEntity.create(
                command.getProductId(),
                command.getFileName(),
                command.getUrl(),
                command.getFileMetadata().getMediaType(),
                command.getFileMetadata().getMimeType(),
                command.getFileMetadata().getFileSize(),
                command.getFileMetadata().getWidth(),
                command.getFileMetadata().getHeight(),
                command.getAltText(),
                command.getTitle(),
                command.getSortOrder(),
                command.getIsPrimary());
        
        // Validate media constraints
        validateMediaConstraints(media);
        
        // If this is set as primary, ensure single primary rule
        if (Boolean.TRUE.equals(command.getIsPrimary())) {
            ensureSinglePrimary(command.getProductId(), null);
        }
        
        // Save the media
        DomainProductMediaEntity savedMedia = mediaRepository.save(media);
        
        // Add media to product
        product.addMedia(savedMedia);
        productRepository.save(product);
        
        // Write audit
        auditWriter.writeMediaAudit(DomainAuditEntry.forCreate(
                savedMedia, "MEDIA", "system"));
        
        return savedMedia;
    }

    @Override
    public DomainProductMediaEntity updateMedia(DomainUpdateMediaCommand command) {
        // Find existing media
        DomainProductMediaEntity existingMedia = mediaRepository.findById(command.getMediaId())
                .orElseThrow(() -> new DomainMediaNotFoundException(command.getMediaId()));
        
        // Validate that media belongs to the specified product
        if (!existingMedia.getProductId().equals(command.getProductId())) {
            throw new IllegalArgumentException("Media does not belong to the specified product");
        }
        
        // Check version for optimistic locking
        if (!existingMedia.getVersion().equals(command.getVersion())) {
            throw new IllegalStateException("Media has been modified by another user");
        }
        
        // Store the before state for audit
        DomainProductMediaEntity beforeState = cloneMedia(existingMedia);
        
        // Update metadata
        existingMedia.updateMetadata(
                command.getAltText(),
                command.getTitle(),
                command.getIsPrimary(),
                command.getSortOrder());
        
        // If this is set as primary, ensure single primary rule
        if (Boolean.TRUE.equals(command.getIsPrimary())) {
            ensureSinglePrimary(command.getProductId(), command.getMediaId());
        }
        
        // Save the updated media
        DomainProductMediaEntity updatedMedia = mediaRepository.save(existingMedia);
        
        // Write audit
        auditWriter.writeMediaAudit(DomainAuditEntry.forUpdate(
                beforeState, updatedMedia, "MEDIA", "system"));
        
        return updatedMedia;
    }

    @Override
    public DomainProductMediaEntity getMedia(UUID productId, UUID mediaId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (mediaId == null) {
            throw new IllegalArgumentException("Media ID cannot be null");
        }
        
        // Validate that product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Find the media
        DomainProductMediaEntity media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new DomainMediaNotFoundException(mediaId));
        
        // Validate that media belongs to the product
        if (!media.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Media does not belong to the specified product");
        }
        
        return media;
    }

    @Override
    public List<DomainProductMediaEntity> listProductMedia(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // Validate that product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Get all media for the product
        return mediaRepository.findByProductId(productId);
    }

    @Override
    public void deleteMedia(UUID productId, UUID mediaId) {
        // Get the media to delete
        DomainProductMediaEntity media = getMedia(productId, mediaId);
        
        // Create audit before deletion
        DomainAuditEntry auditEntry = DomainAuditEntry.forDelete(
                media, "MEDIA", "system");
        
        // Delete the media
        mediaRepository.delete(mediaId);
        
        // Update product media list
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        product.getMedia().removeIf(m -> m.getId().equals(mediaId));
        productRepository.save(product);
        
        // Write audit after deletion
        auditWriter.writeMediaAudit(auditEntry);
    }

    @Override
    public List<DomainProductMediaEntity> reorderMedia(DomainReorderMediaCommand command) {
        if (command.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // Validate that product exists
        productRepository.findById(command.getProductId())
                .orElseThrow(() -> new DomainProductNotFoundException(command.getProductId()));
        
        // Get current media list
        List<DomainProductMediaEntity> currentMedia = mediaRepository.findByProductId(command.getProductId());
        
        // Build the order map and find primary
        Map<UUID, Integer> orderMap = command.getMediaOrder();
        UUID primaryMediaId = command.getPrimaryMediaId();
        
        // Validate that all media IDs in the order exist
        for (UUID mediaId : orderMap.keySet()) {
            boolean exists = currentMedia.stream()
                    .anyMatch(media -> media.getId().equals(mediaId));
            
            if (!exists) {
                throw new DomainMediaNotFoundException(mediaId);
            }
        }
        
        // Update sort orders and primary flags
        for (DomainProductMediaEntity media : currentMedia) {
            UUID mediaId = media.getId();
            
            // Update sort order if provided
            if (orderMap.containsKey(mediaId)) {
                Integer sortOrder = orderMap.get(mediaId);
                if (sortOrder != null && sortOrder >= 0) {
                    media.setSortOrder(sortOrder);
                }
            }
            
            // Update primary flag
            boolean shouldBePrimary = mediaId.equals(primaryMediaId);
            if (shouldBePrimary != Boolean.TRUE.equals(media.getIsPrimary())) {
                if (shouldBePrimary) {
                    media.setPrimary();
                } else {
                    media.removePrimary();
                }
            }
        }
        
        // Use repository batch update for efficiency
        mediaRepository.updateOrder(command.getProductId(), orderMap);
        
        // Set primary media if specified
        if (primaryMediaId != null) {
            ensureSinglePrimary(command.getProductId(), primaryMediaId);
        }
        
        // Return updated media list
        return mediaRepository.findByProductId(command.getProductId());
    }
    
    /**
     * Validates media constraints such as file size and MIME type
     */
    private void validateMediaConstraints(DomainProductMediaEntity media) {
        try {
            media.validate();
        } catch (IllegalArgumentException e) {
            throw new DomainMediaValidationException(e.getMessage());
        }
    }
    
    /**
     * Ensures only one media item is marked as primary per product
     */
    private void ensureSinglePrimary(UUID productId, UUID newPrimaryId) {
        // Remove primary flag from all other media
        mediaRepository.removePrimaryFlags(productId);
        
        // Set the new primary if specified
        if (newPrimaryId != null) {
            mediaRepository.setPrimary(productId, newPrimaryId);
        }
    }
    
    /**
     * Creates a shallow clone of media for audit purposes
     */
    private DomainProductMediaEntity cloneMedia(DomainProductMediaEntity media) {
        return DomainProductMediaEntity.builder()
                .id(media.getId())
                .productId(media.getProductId())
                .fileName(media.getFileName())
                .url(media.getUrl())
                .mediaType(media.getMediaType())
                .mimeType(media.getMimeType())
                .altText(media.getAltText())
                .title(media.getTitle())
                .sortOrder(media.getSortOrder())
                .width(media.getWidth())
                .height(media.getHeight())
                .fileSize(media.getFileSize())
                .isPrimary(media.getIsPrimary())
                .uploadedAt(media.getUploadedAt())
                .version(media.getVersion())
                .build();
    }
}