package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainMediaType;
import ai.shreds.shared.dtos.SharedProductMediaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Product Media Entity
 * Represents media files (images, videos, documents) associated with products
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainProductMediaEntity {
    
    private UUID id;
    private UUID productId;
    private String fileName;
    private String url;
    private DomainMediaType mediaType;
    private String mimeType;
    private String altText;
    private String title;
    private Integer sortOrder;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private Boolean isPrimary;
    private Instant uploadedAt;
    private Long version;
    
    /**
     * Validates file size against media type constraints
     */
    public void validateFileSize() {
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        
        long maxSize = mediaType != null ? mediaType.getMaxFileSize() : 10 * 1024 * 1024; // 10MB default
        
        if (fileSize > maxSize) {
            throw new IllegalArgumentException(
                String.format("File size %d bytes exceeds maximum allowed size %d bytes for %s", 
                    fileSize, maxSize, mediaType)
            );
        }
    }
    
    /**
     * Validates MIME type against media type
     */
    public void validateMimeType() {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            throw new IllegalArgumentException("MIME type cannot be null or empty");
        }
        
        if (mediaType != null && !mediaType.getAllowedMimeTypes().contains(mimeType)) {
            throw new IllegalArgumentException(
                String.format("MIME type '%s' is not allowed for media type %s", 
                    mimeType, mediaType)
            );
        }
    }
    
    /**
     * Validates all media constraints
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        if (mediaType == null) {
            throw new IllegalArgumentException("Media type cannot be null");
        }
        
        validateFileSize();
        validateMimeType();
        
        // Validate dimensions for images/videos
        if (mediaType == DomainMediaType.IMAGE || mediaType == DomainMediaType.VIDEO) {
            if (width != null && width <= 0) {
                throw new IllegalArgumentException("Width must be positive");
            }
            
            if (height != null && height <= 0) {
                throw new IllegalArgumentException("Height must be positive");
            }
        }
        
        // Validate sort order
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
    
    /**
     * Updates media metadata
     */
    public void updateMetadata(String altText, String title, Boolean isPrimary, Integer sortOrder) {
        this.altText = altText;
        this.title = title;
        
        if (isPrimary != null) {
            this.isPrimary = isPrimary;
        }
        
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }
    
    /**
     * Sets this media as primary (caller should handle ensuring only one primary per product)
     */
    public void setPrimary() {
        this.isPrimary = true;
    }
    
    /**
     * Removes primary flag from this media
     */
    public void removePrimary() {
        this.isPrimary = false;
    }
    
    /**
     * Checks if this is an image
     */
    public boolean isImage() {
        return mediaType == DomainMediaType.IMAGE;
    }
    
    /**
     * Checks if this is a video
     */
    public boolean isVideo() {
        return mediaType == DomainMediaType.VIDEO;
    }
    
    /**
     * Checks if this is a document
     */
    public boolean isDocument() {
        return mediaType == DomainMediaType.DOCUMENT;
    }
    
    /**
     * Gets file size in KB
     */
    public long getFileSizeInKB() {
        return fileSize != null ? fileSize / 1024 : 0;
    }
    
    /**
     * Gets file size in MB
     */
    public double getFileSizeInMB() {
        return fileSize != null ? fileSize / (1024.0 * 1024.0) : 0.0;
    }
    
    /**
     * Gets aspect ratio for images/videos
     */
    public Double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return null;
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedProductMediaDTO toDTO() {
        return SharedProductMediaDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainProductMediaEntity fromDTO(SharedProductMediaDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainProductMediaEntity.builder()
            .id(dto.getId())
            .productId(dto.getProductId())
            .fileName(dto.getFileName())
            .url(dto.getUrl())
            .mediaType(DomainMediaType.valueOf(dto.getMediaType().name()))
            .mimeType(dto.getMimeType())
            .altText(dto.getAltText())
            .title(dto.getTitle())
            .sortOrder(dto.getSortOrder())
            .width(dto.getWidth())
            .height(dto.getHeight())
            .fileSize(dto.getFileSize())
            .isPrimary(dto.getIsPrimary())
            .uploadedAt(dto.getUploadedAt())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a new media entity
     */
    public static DomainProductMediaEntity create(UUID productId, String fileName, String url, 
                                                DomainMediaType mediaType, String mimeType, 
                                                Long fileSize, Integer width, Integer height,
                                                String altText, String title, Integer sortOrder, 
                                                Boolean isPrimary) {
        
        DomainProductMediaEntity media = DomainProductMediaEntity.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .fileName(fileName)
            .url(url)
            .mediaType(mediaType)
            .mimeType(mimeType)
            .fileSize(fileSize)
            .width(width)
            .height(height)
            .altText(altText)
            .title(title)
            .sortOrder(sortOrder != null ? sortOrder : 0)
            .isPrimary(isPrimary != null ? isPrimary : false)
            .uploadedAt(Instant.now())
            .version(0L)
            .build();
        
        media.validate();
        return media;
    }
}