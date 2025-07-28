package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedMediaType;
import ai.shreds.domain.value_objects.DomainFileMetadataValue;
import lombok.Builder;

/**
 * Represents metadata for uploaded files used in the application layer.
 */
@Builder
public class ApplicationFileMetadata {

    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private SharedMediaType mediaType;

    public ApplicationFileMetadata() {}

    public ApplicationFileMetadata(String fileName, Long fileSize, String mimeType, Integer width, Integer height, SharedMediaType mediaType) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.mediaType = mediaType;
    }

    /**
     * Basic validation for upload.
     */
    public boolean isValidForUpload() {
        return fileName != null && !fileName.trim().isEmpty()
            && fileSize != null && fileSize > 0
            && mimeType != null && !mimeType.trim().isEmpty();
    }

    /**
     * Convert to domain value object.
     */
    public DomainFileMetadataValue toDomainValue() {
        return DomainFileMetadataValue.fromApplicationValue(this);
    }

    // Getters and setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public SharedMediaType getMediaType() { return mediaType; }
    public void setMediaType(SharedMediaType mediaType) { this.mediaType = mediaType; }
}