package ai.shreds.domain.exceptions;

import ai.shreds.domain.enums.DomainMediaType;

/**
 * Domain Media Validation Exception
 * Thrown when media validation fails
 */
public class DomainMediaValidationException extends RuntimeException {
    
    private final String reason;
    private final DomainMediaType mediaType;
    
    /**
     * Constructor with message
     * 
     * @param message The validation error message
     */
    public DomainMediaValidationException(String message) {
        super(message);
        this.reason = message;
        this.mediaType = null;
    }
    
    /**
     * Constructor with media type and reason
     * 
     * @param mediaType The media type involved in the validation error
     * @param reason The specific reason for the validation failure
     */
    public DomainMediaValidationException(DomainMediaType mediaType, String reason) {
        super(String.format("Media validation failed for type %s: %s", mediaType, reason));
        this.mediaType = mediaType;
        this.reason = reason;
    }
    
    /**
     * Gets the media type involved in the validation error
     * 
     * @return The media type or null if not specific to one type
     */
    public DomainMediaType getMediaType() {
        return mediaType;
    }
    
    /**
     * Gets the specific reason for the validation failure
     * 
     * @return The reason for the validation failure
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Factory method for file size validation errors
     * 
     * @param mediaType The media type
     * @param fileSize The actual file size
     * @param maxSize The maximum allowed file size
     * @return The exception instance
     */
    public static DomainMediaValidationException fileSizeExceeded(DomainMediaType mediaType, long fileSize, long maxSize) {
        return new DomainMediaValidationException(mediaType, 
            String.format("File size %d bytes exceeds maximum allowed size %d bytes", fileSize, maxSize));
    }
    
    /**
     * Factory method for MIME type validation errors
     * 
     * @param mediaType The media type
     * @param mimeType The invalid MIME type
     * @return The exception instance
     */
    public static DomainMediaValidationException invalidMimeType(DomainMediaType mediaType, String mimeType) {
        return new DomainMediaValidationException(mediaType, 
            String.format("MIME type '%s' is not allowed for media type %s", mimeType, mediaType));
    }
    
    /**
     * Factory method for dimension validation errors
     * 
     * @param dimension The dimension name (width/height)
     * @param value The invalid dimension value
     * @return The exception instance
     */
    public static DomainMediaValidationException invalidDimension(String dimension, Integer value) {
        return new DomainMediaValidationException(
            String.format("%s must be positive, got %d", dimension, value));
    }
    
    /**
     * Factory method for missing required URL
     * 
     * @return The exception instance
     */
    public static DomainMediaValidationException missingUrl() {
        return new DomainMediaValidationException("Media URL cannot be null or empty");
    }
    
    /**
     * Factory method for invalid sort order
     * 
     * @param sortOrder The invalid sort order
     * @return The exception instance
     */
    public static DomainMediaValidationException invalidSortOrder(Integer sortOrder) {
        return new DomainMediaValidationException(
            String.format("Sort order cannot be negative, got %d", sortOrder));
    }
}