package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing product media in the database.
 * Stores metadata about media files (images, videos, documents) associated with products.
 */
@Entity
@Table(name = "product_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureProductMediaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "file_name")
    private String fileName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "alt_text")
    private String altText;

    @Column
    private String title;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Version
    private Long version;

    /**
     * Enumeration for media types
     */
    public enum MediaType {
        IMAGE,
        VIDEO,
        DOCUMENT;

        /**
         * Gets allowed MIME types for this media type
         * @return array of allowed MIME types
         */
        public String[] getAllowedMimeTypes() {
            return switch (this) {
                case IMAGE -> new String[]{
                    "image/jpeg", "image/jpg", "image/png", "image/gif", 
                    "image/webp", "image/svg+xml", "image/bmp"
                };
                case VIDEO -> new String[]{
                    "video/mp4", "video/avi", "video/mov", "video/wmv", 
                    "video/webm", "video/mkv"
                };
                case DOCUMENT -> new String[]{
                    "application/pdf", "application/msword", 
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain", "text/csv"
                };
            };
        }

        /**
         * Gets the maximum file size for this media type in bytes
         * @return maximum file size in bytes
         */
        public long getMaxFileSize() {
            return switch (this) {
                case IMAGE -> 10 * 1024 * 1024; // 10MB
                case VIDEO -> 100 * 1024 * 1024; // 100MB
                case DOCUMENT -> 5 * 1024 * 1024; // 5MB
            };
        }

        /**
         * Checks if the MIME type is allowed for this media type
         * @param mimeType the MIME type to check
         * @return true if allowed
         */
        public boolean isAllowedMimeType(String mimeType) {
            if (mimeType == null) return false;
            String[] allowed = getAllowedMimeTypes();
            for (String allowedType : allowed) {
                if (allowedType.equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Helper method to check if this is an image
     * @return true if this is an image media
     */
    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    /**
     * Helper method to check if this is a video
     * @return true if this is a video media
     */
    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    /**
     * Helper method to check if this is a document
     * @return true if this is a document media
     */
    public boolean isDocument() {
        return mediaType == MediaType.DOCUMENT;
    }

    /**
     * Helper method to get formatted file size
     * @return formatted file size string
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Helper method to get dimensions as string
     * @return dimensions as "WIDTHxHEIGHT" or null
     */
    public String getDimensions() {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }

    /**
     * Validates the media entity
     * @throws IllegalArgumentException if validation fails
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
        if (mimeType == null || mimeType.trim().isEmpty()) {
            throw new IllegalArgumentException("MIME type cannot be null or empty");
        }
        if (!mediaType.isAllowedMimeType(mimeType)) {
            throw new IllegalArgumentException(
                String.format("MIME type %s is not allowed for media type %s", mimeType, mediaType)
            );
        }
        if (fileSize != null && fileSize > mediaType.getMaxFileSize()) {
            throw new IllegalArgumentException(
                String.format("File size %d exceeds maximum allowed size %d for media type %s", 
                    fileSize, mediaType.getMaxFileSize(), mediaType)
            );
        }
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        validate();
    }

    /**
     * Pre-update hook for validation
     */
    @PreUpdate
    public void preUpdate() {
        validate();
    }
}
