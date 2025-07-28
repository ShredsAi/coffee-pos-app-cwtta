package ai.shreds.application.value_objects;

import lombok.Builder;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ai.shreds.shared.enums.SharedMediaType;
import ai.shreds.domain.value_objects.DomainFileMetadataValue;

@Getter
@Builder
@EqualsAndHashCode
@ToString
public class ApplicationFileMetadata {
    
    private final String fileName;
    private final Long fileSize;
    private final String mimeType;
    private final Integer width;
    private final Integer height;
    private final SharedMediaType mediaType;
    
    /**
     * Validates if the file is suitable for upload based on type, size, and dimensions
     * @return boolean indicating if the file is valid
     */
    public boolean isValidForUpload() {
        // Basic validation rules
        if (fileSize == null || fileSize <= 0) {
            return false;
        }
        
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        
        // For images, validate dimensions
        if (mediaType == SharedMediaType.IMAGE) {
            if (width == null || height == null || width <= 0 || height <= 0) {
                return false;
            }
            
            // Maximum file size for images (10MB)
            if (fileSize > 10_000_000) {
                return false;
            }
            
            // Maximum dimensions (to prevent extremely large images)
            if (width > 5000 || height > 5000) {
                return false;
            }
        }
        
        // For videos, validate size
        if (mediaType == SharedMediaType.VIDEO) {
            // Maximum file size for videos (100MB)
            if (fileSize > 100_000_000) {
                return false;
            }
        }
        
        // For documents, validate size
        if (mediaType == SharedMediaType.DOCUMENT) {
            // Maximum file size for documents (20MB)
            if (fileSize > 20_000_000) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Converts application metadata to domain metadata value object
     * @return DomainFileMetadataValue
     */
    public DomainFileMetadataValue toDomainValue() {
        return DomainFileMetadataValue.builder()
            .fileName(fileName)
            .fileSize(fileSize)
            .mimeType(mimeType)
            .width(width)
            .height(height)
            .mediaType(mapMediaType())
            .build();
    }
    
    private ai.shreds.domain.enums.DomainMediaType mapMediaType() {
        if (mediaType == null) {
            return null;
        }
        
        switch (mediaType) {
            case IMAGE:
                return ai.shreds.domain.enums.DomainMediaType.IMAGE;
            case VIDEO:
                return ai.shreds.domain.enums.DomainMediaType.VIDEO;
            case DOCUMENT:
                return ai.shreds.domain.enums.DomainMediaType.DOCUMENT;
            default:
                throw new IllegalArgumentException("Unknown media type: " + mediaType);
        }
    }
    
    /**
     * Factory method to create metadata from domain value
     * @param domainValue Domain metadata value object
     * @return Application metadata
     */
    public static ApplicationFileMetadata fromDomainValue(DomainFileMetadataValue domainValue) {
        if (domainValue == null) {
            return null;
        }
        
        return ApplicationFileMetadata.builder()
            .fileName(domainValue.getFileName())
            .fileSize(domainValue.getFileSize())
            .mimeType(domainValue.getMimeType())
            .width(domainValue.getWidth())
            .height(domainValue.getHeight())
            .mediaType(mapDomainMediaType(domainValue.getMediaType()))
            .build();
    }
    
    private static SharedMediaType mapDomainMediaType(ai.shreds.domain.enums.DomainMediaType domainMediaType) {
        if (domainMediaType == null) {
            return null;
        }
        
        switch (domainMediaType) {
            case IMAGE:
                return SharedMediaType.IMAGE;
            case VIDEO:
                return SharedMediaType.VIDEO;
            case DOCUMENT:
                return SharedMediaType.DOCUMENT;
            default:
                throw new IllegalArgumentException("Unknown domain media type: " + domainMediaType);
        }
    }
}