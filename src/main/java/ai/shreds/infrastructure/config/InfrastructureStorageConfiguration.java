package ai.shreds.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Configuration for external storage services (S3-compatible storage).
 * Configures S3 client for file upload operations.
 */
@Configuration
public class InfrastructureStorageConfiguration {

    @Value("${storage.s3.bucket}")
    private String bucketName;

    @Value("${storage.s3.region}")
    private String region;

    @Value("${storage.s3.endpoint:}")
    private String endpoint;

    @Value("${storage.s3.access-key}")
    private String accessKey;

    @Value("${storage.s3.secret-key}")
    private String secretKey;

    @Value("${storage.cdn.base-url}")
    private String cdnBaseUrl;

    /**
     * Creates and configures the S3 client bean.
     * Supports both AWS S3 and S3-compatible services (MinIO, etc.)
     *
     * @return configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        
        // If endpoint is specified, use it (for MinIO or other S3-compatible services)
        if (endpoint != null && !endpoint.trim().isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
            // Enable path-style access for MinIO compatibility
            builder.forcePathStyle(true);
        }
        
        return builder.build();
    }

    /**
     * @return the configured S3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * @return the CDN base URL for serving files
     */
    public String getCdnBaseUrl() {
        return cdnBaseUrl;
    }
}