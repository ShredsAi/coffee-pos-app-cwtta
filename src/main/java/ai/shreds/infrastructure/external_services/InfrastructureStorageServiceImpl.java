package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortStorageService;
import ai.shreds.application.value_objects.ApplicationFileMetadata;
import ai.shreds.shared.enums.SharedMediaType;
import ai.shreds.infrastructure.config.InfrastructureStorageConfiguration;
import ai.shreds.infrastructure.exceptions.InfrastructureStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Infrastructure implementation of the storage service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureStorageServiceImpl implements ApplicationOutputPortStorageService {

    private final S3Client s3Client;
    private final InfrastructureStorageConfiguration storageConfig;

    @Override
    public String uploadFile(byte[] file, String fileName, String contentType) {
        log.debug("Uploading file: {} with content type: {} and size: {} bytes", fileName, contentType, file.length);
        
        try {
            String s3Key = generateS3Key(fileName);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) file.length)
                    .build();
            
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));
            
            String fileUrl = buildFileUrl(s3Key);
            log.info("File uploaded successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Error uploading file: {}", fileName, e);
            throw new InfrastructureStorageException("Failed to upload file", fileName, e);
        }
    }

    @Override
    public void deleteFile(String url) {
        log.debug("Deleting file: {}", url);
        
        try {
            String s3Key = extractS3KeyFromUrl(url);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", url);
            
        } catch (Exception e) {
            log.error("Error deleting file: {}", url, e);
            throw new InfrastructureStorageException("Failed to delete file", e);
        }
    }

    @Override
    public ApplicationFileMetadata extractMetadata(byte[] file, String contentType) {
        log.debug("Extracting metadata for file with content type: {} and size: {} bytes", contentType, file.length);
        try {
            ApplicationFileMetadata.ApplicationFileMetadataBuilder builder = ApplicationFileMetadata.builder()
                    .fileSize((long) file.length)
                    .mimeType(contentType)
                    .mediaType(determineMediaType(contentType));
            if (contentType.toLowerCase().startsWith("image/")) {
                extractImageMetadata(file, builder);
            }
            return builder.build();
        } catch (Exception e) {
            log.error("Error extracting file metadata", e);
            return ApplicationFileMetadata.builder()
                    .fileSize((long) file.length)
                    .mimeType(contentType)
                    .mediaType(determineMediaType(contentType))
                    .build();
        }
    }

    /**
     * Determines SharedMediaType based on content type string.
     */
    private SharedMediaType determineMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) {
            return SharedMediaType.IMAGE;
        } else if (lower.startsWith("video/")) {
            return SharedMediaType.VIDEO;
        } else {
            return SharedMediaType.DOCUMENT;
        }
    }

    /**
     * Extracts image width and height and sets on builder.
     */
    private void extractImageMetadata(byte[] file, ApplicationFileMetadata.ApplicationFileMetadataBuilder builder) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file));
            if (image != null) {
                builder.width(image.getWidth());
                builder.height(image.getHeight());
            }
        } catch (IOException e) {
            log.warn("Error extracting image metadata", e);
        }
    }

    /**
     * Generates a unique S3 key for the file.
     */
    private String generateS3Key(String fileName) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s/%s_%s", timestamp, uuid, fileName);
    }

    /**
     * Builds the complete file URL using CDN base URL.
     */
    private String buildFileUrl(String s3Key) {
        String baseUrl = storageConfig.getCdnBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + s3Key;
    }

    /**
     * Extracts S3 key from the complete file URL.
     */
    private String extractS3KeyFromUrl(String url) {
        String baseUrl = storageConfig.getCdnBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        if (url.startsWith(baseUrl + "/")) {
            return url.substring(baseUrl.length() + 1);
        }
        
        // Fallback: assume the URL path is the S3 key
        return url.substring(url.lastIndexOf("/") + 1);
    }
}