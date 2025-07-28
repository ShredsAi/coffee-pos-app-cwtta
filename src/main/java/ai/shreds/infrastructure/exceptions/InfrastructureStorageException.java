package ai.shreds.infrastructure.exceptions;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Exception thrown when operations related to external storage services (S3, CDN, etc.) fail.
 * This exception encapsulates technical storage failures that should be handled at the adapter layer.
 * Provides detailed context about the failed operation including file information and operation type.
 */
public class InfrastructureStorageException extends RuntimeException {

    private final String fileName;
    private final String operation;
    private final String fileUrl;
    private final Long fileSize;
    private final String contentType;
    private final Instant timestamp;
    private final Map<String, Object> additionalContext;

    /**
     * Creates a new storage exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InfrastructureStorageException(String message) {
        super(message);
        this.fileName = null;
        this.operation = null;
        this.fileUrl = null;
        this.fileSize = null;
        this.contentType = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new storage exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InfrastructureStorageException(String message, Throwable cause) {
        super(message, cause);
        this.fileName = null;
        this.operation = null;
        this.fileUrl = null;
        this.fileSize = null;
        this.contentType = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new storage exception with context about the operation being performed and the file being processed.
     *
     * @param operation the storage operation that failed (e.g., "upload", "delete")
     * @param fileName the name of the file that was being processed
     * @param cause the cause of the exception
     */
    public InfrastructureStorageException(String operation, String fileName, Throwable cause) {
        super(formatMessage(operation, fileName, null), cause);
        this.fileName = fileName;
        this.operation = operation;
        this.fileUrl = null;
        this.fileSize = null;
        this.contentType = null;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new storage exception with comprehensive file context.
     *
     * @param operation the storage operation that failed
     * @param fileName the name of the file
     * @param fileUrl the URL of the file (if available)
     * @param fileSize the size of the file in bytes
     * @param contentType the MIME type of the file
     * @param cause the cause of the exception
     */
    public InfrastructureStorageException(String operation, String fileName, String fileUrl, 
                                        Long fileSize, String contentType, Throwable cause) {
        super(formatMessage(operation, fileName, fileUrl), cause);
        this.fileName = fileName;
        this.operation = operation;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.timestamp = Instant.now();
        this.additionalContext = new HashMap<>();
    }

    /**
     * Creates a new storage exception with full context and additional metadata.
     *
     * @param operation the storage operation that failed
     * @param fileName the name of the file
     * @param fileUrl the URL of the file
     * @param fileSize the size of the file in bytes
     * @param contentType the MIME type of the file
     * @param additionalContext additional context information
     * @param cause the cause of the exception
     */
    public InfrastructureStorageException(String operation, String fileName, String fileUrl, 
                                        Long fileSize, String contentType, 
                                        Map<String, Object> additionalContext, Throwable cause) {
        super(formatMessage(operation, fileName, fileUrl), cause);
        this.fileName = fileName;
        this.operation = operation;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.timestamp = Instant.now();
        this.additionalContext = additionalContext != null ? new HashMap<>(additionalContext) : new HashMap<>();
    }

    /**
     * @return the name of the file involved in the operation, or null if not specified
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the operation that was being performed, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return the URL of the file, or null if not specified
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * @return the size of the file in bytes, or null if not specified
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @return the MIME type of the file, or null if not specified
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the timestamp when the exception occurred
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * @return additional context information as a map
     */
    public Map<String, Object> getAdditionalContext() {
        return new HashMap<>(additionalContext);
    }

    /**
     * Adds additional context information to the exception.
     *
     * @param key the context key
     * @param value the context value
     */
    public void addContext(String key, Object value) {
        if (key != null) {
            additionalContext.put(key, value);
        }
    }

    /**
     * Gets a specific context value.
     *
     * @param key the context key
     * @return the context value, or null if not found
     */
    public Object getContext(String key) {
        return additionalContext.get(key);
    }

    /**
     * @return a summary of the file information for logging purposes
     */
    public String getFileSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (fileName != null) {
            summary.append("File: ").append(fileName);
        }
        
        if (fileSize != null) {
            summary.append(", Size: ").append(formatFileSize(fileSize));
        }
        
        if (contentType != null) {
            summary.append(", Type: ").append(contentType);
        }
        
        if (fileUrl != null) {
            summary.append(", URL: ").append(fileUrl);
        }
        
        return summary.toString();
    }

    /**
     * @return whether this exception represents a file upload failure
     */
    public boolean isUploadFailure() {
        return "upload".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents a file deletion failure
     */
    public boolean isDeleteFailure() {
        return "delete".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents a file download failure
     */
    public boolean isDownloadFailure() {
        return "download".equalsIgnoreCase(operation);
    }

    /**
     * @return whether this exception represents a file metadata extraction failure
     */
    public boolean isMetadataFailure() {
        return "metadata".equalsIgnoreCase(operation) || "extract-metadata".equalsIgnoreCase(operation);
    }

    /**
     * Static factory methods for common storage exceptions
     */

    /**
     * Creates an upload failure exception
     * @param fileName the file name
     * @param cause the underlying cause
     * @return storage exception for upload failure
     */
    public static InfrastructureStorageException uploadFailed(String fileName, Throwable cause) {
        return new InfrastructureStorageException("upload", fileName, cause);
    }

    /**
     * Creates a deletion failure exception
     * @param fileUrl the file URL
     * @param cause the underlying cause
     * @return storage exception for deletion failure
     */
    public static InfrastructureStorageException deleteFailed(String fileUrl, Throwable cause) {
        InfrastructureStorageException exception = new InfrastructureStorageException("delete", null, cause);
        exception.addContext("fileUrl", fileUrl);
        return exception;
    }

    /**
     * Creates a metadata extraction failure exception
     * @param fileName the file name
     * @param contentType the content type
     * @param cause the underlying cause
     * @return storage exception for metadata extraction failure
     */
    public static InfrastructureStorageException metadataExtractionFailed(String fileName, String contentType, Throwable cause) {
        return new InfrastructureStorageException("extract-metadata", fileName, null, null, contentType, cause);
    }

    /**
     * Creates a file size validation failure exception
     * @param fileName the file name
     * @param fileSize the actual file size
     * @param maxSize the maximum allowed size
     * @return storage exception for size validation failure
     */
    public static InfrastructureStorageException fileSizeExceeded(String fileName, long fileSize, long maxSize) {
        InfrastructureStorageException exception = new InfrastructureStorageException(
                String.format("File size validation failed for %s: %s exceeds maximum allowed size of %s", 
                        fileName, formatFileSize(fileSize), formatFileSize(maxSize)));
        exception.addContext("actualSize", fileSize);
        exception.addContext("maxSize", maxSize);
        return exception;
    }

    /**
     * Helper methods
     */
    
    private static String formatMessage(String operation, String fileName, String fileUrl) {
        StringBuilder message = new StringBuilder();
        
        if (operation != null) {
            message.append(operation.substring(0, 1).toUpperCase())
                   .append(operation.substring(1).toLowerCase())
                   .append(" operation failed");
        } else {
            message.append("Storage operation failed");
        }
        
        if (fileName != null) {
            message.append(" for file: ").append(fileName);
        } else if (fileUrl != null) {
            message.append(" for URL: ").append(fileUrl);
        }
        
        return message.toString();
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
