package ai.shreds.domain.value_objects;

import ai.shreds.domain.enums.DomainMediaType;
import ai.shreds.shared.value_objects.ApplicationFileMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain File Metadata Value Object
 * Represents file metadata information within the domain layer
 * Contains file properties and validation logic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainFileMetadataValue {
    
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private DomainMediaType mediaType;
    
    /**
     * Validates if the file is valid for upload
     * @return true if file is valid for upload
     */
    public boolean isValidForUpload() {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        if (fileSize == null || fileSize <= 0) {
            return false;
        }
        
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return false;
        }
        
        if (mediaType == null) {
            return false;
        }
        
        // Check if mime type is allowed for this media type
        if (!mediaType.getAllowedMimeTypes().contains(mimeType)) {
            return false;
        }
        
        // Check file size limit
        if (fileSize > mediaType.getMaxFileSize()) {
            return false;
        }
        
        // For images, width and height should be present
        if (mediaType == DomainMediaType.IMAGE) {
            if (width == null || height == null || width <= 0 || height <= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates file size against a maximum size limit
     * @param maxSize maximum allowed file size in bytes
     * @throws IllegalArgumentException if file size exceeds limit
     */
    public void validateSize(Long maxSize) {
        if (maxSize == null) {
            throw new IllegalArgumentException("Maximum size cannot be null");
        }
        
        if (fileSize == null) {
            throw new IllegalArgumentException("File size cannot be null");
        }
        
        if (fileSize > maxSize) {
            throw new IllegalArgumentException(
                String.format("File size %d bytes exceeds maximum allowed size %d bytes", 
                    fileSize, maxSize)
            );
        }
    }
    
    /**
     * Factory method to create domain value object from application layer value
     * @param value ApplicationFileMetadata from application layer
     * @return DomainFileMetadataValue instance
     */
    public static DomainFileMetadataValue fromApplicationValue(ApplicationFileMetadata value) {
        if (value == null) {
            throw new IllegalArgumentException("ApplicationFileMetadata cannot be null");
        }
        
        // Convert SharedMediaType to DomainMediaType
        DomainMediaType domainMediaType = convertToDomainMediaType(value.getMediaType());
        
        return DomainFileMetadataValue.builder()
            .fileName(value.getFileName())
            .fileSize(value.getFileSize())
            .mimeType(value.getMimeType())
            .width(value.getWidth())
            .height(value.getHeight())
            .mediaType(domainMediaType)
            .build();
    }
    
    /**
     * Converts SharedMediaType to DomainMediaType
     * @param sharedMediaType from application layer
     * @return corresponding DomainMediaType
     */
    private static DomainMediaType convertToDomainMediaType(ai.shreds.shared.enums.SharedMediaType sharedMediaType) {
        if (sharedMediaType == null) {
            throw new IllegalArgumentException("SharedMediaType cannot be null");
        }
        
        switch (sharedMediaType) {
            case IMAGE:
                return DomainMediaType.IMAGE;
            case VIDEO:
                return DomainMediaType.VIDEO;
            case DOCUMENT:
                return DomainMediaType.DOCUMENT;
            default:
                throw new IllegalArgumentException("Unknown media type: " + sharedMediaType);
        }
    }
    
    /**
     * Gets formatted file size string
     * @return human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown size";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets image dimensions as string
     * @return dimensions in format "widthxheight" or empty string if not applicable
     */
    public String getDimensions() {
        if (width != null && height != null && width > 0 && height > 0) {
            return width + "x" + height;
        }
        return "";
    }
    
    /**
     * Checks if this is an image file
     * @return true if media type is IMAGE
     */
    public boolean isImage() {
        return mediaType == DomainMediaType.IMAGE;
    }
    
    /**
     * Checks if this is a video file
     * @return true if media type is VIDEO
     */
    public boolean isVideo() {
        return mediaType == DomainMediaType.VIDEO;
    }
    
    /**
     * Checks if this is a document file
     * @return true if media type is DOCUMENT
     */
    public boolean isDocument() {
        return mediaType == DomainMediaType.DOCUMENT;
    }
}