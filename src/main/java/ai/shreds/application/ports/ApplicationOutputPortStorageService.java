package ai.shreds.application.ports;

import ai.shreds.application.value_objects.ApplicationFileMetadata;

/**
 * Output port for storage operations in the application layer.
 * Defines the contract for external file storage services.
 */
public interface ApplicationOutputPortStorageService {
    
    /**
     * Uploads a file to the storage service.
     * 
     * @param file the file content as byte array
     * @param fileName the name of the file
     * @param contentType the MIME type of the file
     * @return the URL of the uploaded file
     */
    String uploadFile(byte[] file, String fileName, String contentType);
    
    /**
     * Deletes a file from the storage service.
     * 
     * @param url the URL of the file to delete
     */
    void deleteFile(String url);
    
    /**
     * Extracts metadata from a file.
     * 
     * @param file the file content as byte array
     * @param contentType the MIME type of the file
     * @return file metadata including dimensions, size, type, etc.
     */
    ApplicationFileMetadata extractMetadata(byte[] file, String contentType);
}