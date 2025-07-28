package ai.shreds.domain.enums;

import java.util.Set;

/**
 * Domain Media Type Enum
 * Represents the different types of media files supported
 */
public enum DomainMediaType {
    IMAGE,
    VIDEO,
    DOCUMENT;
    
    /**
     * Gets the allowed MIME types for this media type
     */
    public Set<String> getAllowedMimeTypes() {
        switch (this) {
            case IMAGE:
                return Set.of(
                    "image/jpeg",
                    "image/jpg", 
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "image/bmp",
                    "image/svg+xml"
                );
            case VIDEO:
                return Set.of(
                    "video/mp4",
                    "video/mpeg",
                    "video/quicktime",
                    "video/x-msvideo", // AVI
                    "video/x-ms-wmv",  // WMV
                    "video/webm"
                );
            case DOCUMENT:
                return Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
                    "text/plain",
                    "text/csv"
                );
            default:
                return Set.of();
        }
    }
    
    /**
     * Gets the maximum file size allowed for this media type (in bytes)
     */
    public long getMaxFileSize() {
        switch (this) {
            case IMAGE:
                return 10 * 1024 * 1024; // 10 MB
            case VIDEO:
                return 100 * 1024 * 1024; // 100 MB
            case DOCUMENT:
                return 25 * 1024 * 1024; // 25 MB
            default:
                return 10 * 1024 * 1024; // 10 MB default
        }
    }
    
    /**
     * Gets the file extension patterns for this media type
     */
    public Set<String> getFileExtensions() {
        switch (this) {
            case IMAGE:
                return Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg");
            case VIDEO:
                return Set.of(".mp4", ".mpeg", ".mov", ".avi", ".wmv", ".webm");
            case DOCUMENT:
                return Set.of(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv");
            default:
                return Set.of();
        }
    }
    
    /**
     * Checks if this media type supports dimensions (width/height)
     */
    public boolean supportsDimensions() {
        return this == IMAGE || this == VIDEO;
    }
    
    /**
     * Checks if this media type requires alt text for accessibility
     */
    public boolean requiresAltText() {
        return this == IMAGE;
    }
    
    /**
     * Determines media type from MIME type
     */
    public static DomainMediaType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        String lowerMimeType = mimeType.toLowerCase();
        
        for (DomainMediaType type : values()) {
            if (type.getAllowedMimeTypes().contains(lowerMimeType)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Determines media type from file extension
     */
    public static DomainMediaType fromFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        for (DomainMediaType type : values()) {
            for (String extension : type.getFileExtensions()) {
                if (lowerFileName.endsWith(extension)) {
                    return type;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Validates if a MIME type is supported by this media type
     */
    public boolean supports(String mimeType) {
        return mimeType != null && getAllowedMimeTypes().contains(mimeType.toLowerCase());
    }
    
    /**
     * Gets the recommended quality settings for this media type
     */
    public String getQualityRecommendations() {
        switch (this) {
            case IMAGE:
                return "Use JPEG for photos, PNG for graphics with transparency, WebP for modern browsers";
            case VIDEO:
                return "Use MP4 with H.264 codec for best compatibility, WebM for modern browsers";
            case DOCUMENT:
                return "Use PDF for formatted documents, ensure documents are accessible";
            default:
                return "Follow general file optimization guidelines";
        }
    }
}