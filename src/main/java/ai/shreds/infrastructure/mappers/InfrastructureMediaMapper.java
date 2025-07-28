package ai.shreds.infrastructure.mappers;

import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.domain.enums.DomainMediaType;
import ai.shreds.infrastructure.entities.InfrastructureProductMediaJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between media domain entities and JPA entities.
 * Handles the media type conversions and metadata mapping.
 */
@Component
public class InfrastructureMediaMapper {

    /**
     * Converts JPA entity to domain entity
     * @param jpaEntity the JPA entity
     * @return domain entity
     */
    public DomainProductMediaEntity toDomainEntity(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DomainProductMediaEntity.builder()
                .id(jpaEntity.getId())
                .productId(jpaEntity.getProductId())
                .fileName(jpaEntity.getFileName())
                .url(jpaEntity.getUrl())
                .mediaType(mapMediaTypeToDomain(jpaEntity.getMediaType()))
                .mimeType(jpaEntity.getMimeType())
                .altText(jpaEntity.getAltText())
                .title(jpaEntity.getTitle())
                .sortOrder(jpaEntity.getSortOrder())
                .width(jpaEntity.getWidth())
                .height(jpaEntity.getHeight())
                .fileSize(jpaEntity.getFileSize())
                .isPrimary(jpaEntity.getIsPrimary())
                .uploadedAt(jpaEntity.getUploadedAt())
                .version(jpaEntity.getVersion())
                .build();
    }

    /**
     * Converts domain entity to JPA entity
     * @param domainEntity the domain entity
     * @return JPA entity
     */
    public InfrastructureProductMediaJpaEntity toJpaEntity(DomainProductMediaEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        return InfrastructureProductMediaJpaEntity.builder()
                .id(domainEntity.getId())
                .productId(domainEntity.getProductId())
                .fileName(domainEntity.getFileName())
                .url(domainEntity.getUrl())
                .mediaType(mapMediaTypeToJpa(domainEntity.getMediaType()))
                .mimeType(domainEntity.getMimeType())
                .altText(domainEntity.getAltText())
                .title(domainEntity.getTitle())
                .sortOrder(domainEntity.getSortOrder())
                .width(domainEntity.getWidth())
                .height(domainEntity.getHeight())
                .fileSize(domainEntity.getFileSize())
                .isPrimary(domainEntity.getIsPrimary())
                .uploadedAt(domainEntity.getUploadedAt())
                .version(domainEntity.getVersion())
                .build();
    }

    /**
     * Merges domain entity data into existing JPA entity
     * @param domain the domain entity source
     * @param jpa the JPA entity target
     */
    public void mergeDomainToJpa(DomainProductMediaEntity domain, InfrastructureProductMediaJpaEntity jpa) {
        if (domain == null || jpa == null) {
            return;
        }

        // Update basic fields (keep immutable fields like productId, fileName, url unchanged)
        jpa.setAltText(domain.getAltText());
        jpa.setTitle(domain.getTitle());
        jpa.setSortOrder(domain.getSortOrder());
        jpa.setIsPrimary(domain.getIsPrimary());
        jpa.setVersion(domain.getVersion());
        
        // Note: uploadedAt is immutable once set
        // Note: file metadata (fileName, url, mediaType, mimeType, width, height, fileSize) 
        //       are typically immutable after upload
    }

    /**
     * Maps a list of domain media to JPA entities
     * @param domainMedia the domain media list
     * @return list of JPA entities
     */
    public List<InfrastructureProductMediaJpaEntity> toJpaEntities(List<DomainProductMediaEntity> domainMedia) {
        if (domainMedia == null || domainMedia.isEmpty()) {
            return new ArrayList<>();
        }

        return domainMedia.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of JPA media to domain entities
     * @param jpaMedia the JPA media list
     * @return list of domain entities
     */
    public List<DomainProductMediaEntity> toDomainEntities(List<InfrastructureProductMediaJpaEntity> jpaMedia) {
        if (jpaMedia == null || jpaMedia.isEmpty()) {
            return new ArrayList<>();
        }

        return jpaMedia.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * Maps media type between domain and JPA representations
     */
    
    private DomainMediaType mapMediaTypeToDomain(InfrastructureProductMediaJpaEntity.MediaType jpaType) {
        if (jpaType == null) {
            return DomainMediaType.IMAGE;
        }
        
        try {
            return DomainMediaType.valueOf(jpaType.name());
        } catch (IllegalArgumentException e) {
            return DomainMediaType.IMAGE; // Default fallback
        }
    }
    
    private InfrastructureProductMediaJpaEntity.MediaType mapMediaTypeToJpa(DomainMediaType domainType) {
        if (domainType == null) {
            return InfrastructureProductMediaJpaEntity.MediaType.IMAGE;
        }
        
        return InfrastructureProductMediaJpaEntity.MediaType.valueOf(domainType.name());
    }
    
    /**
     * Utility methods
     */
    
    /**
     * Generates a new media ID if not present
     * @return new UUID
     */
    public UUID generateNewMediaId() {
        return UUID.randomUUID();
    }
    
    /**
     * Sets audit fields for new media creation
     * @param jpaEntity the JPA entity
     */
    public void setCreationAuditFields(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity.getUploadedAt() == null) {
            jpaEntity.setUploadedAt(Instant.now());
        }
        
        if (jpaEntity.getIsPrimary() == null) {
            jpaEntity.setIsPrimary(false);
        }
        if (jpaEntity.getSortOrder() == null) {
            jpaEntity.setSortOrder(0);
        }
    }
    
    /**
     * Creates a media entity from upload parameters
     * @param productId the product ID
     * @param fileName the original file name
     * @param url the stored file URL
     * @param mediaType the media type
     * @param mimeType the MIME type
     * @param fileSize the file size in bytes
     * @param width the image/video width (can be null)
     * @param height the image/video height (can be null)
     * @param altText the alt text
     * @param title the title
     * @param isPrimary whether this is the primary media
     * @param sortOrder the sort order
     * @return new domain media entity
     */
    public DomainProductMediaEntity createFromUpload(
            UUID productId,
            String fileName,
            String url,
            DomainMediaType mediaType,
            String mimeType,
            Long fileSize,
            Integer width,
            Integer height,
            String altText,
            String title,
            Boolean isPrimary,
            Integer sortOrder) {
        
        return DomainProductMediaEntity.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .fileName(fileName)
                .url(url)
                .mediaType(mediaType)
                .mimeType(mimeType)
                .altText(altText)
                .title(title)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .width(width)
                .height(height)
                .fileSize(fileSize)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .uploadedAt(Instant.now())
                .version(0L)
                .build();
    }
    
    /**
     * Validates media entity data
     * @param jpaEntity the JPA entity to validate
     * @return true if valid
     */
    public boolean isValid(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return false;
        }
        
        // Check required fields
        if (jpaEntity.getProductId() == null ||
            jpaEntity.getUrl() == null || jpaEntity.getUrl().trim().isEmpty() ||
            jpaEntity.getMediaType() == null ||
            jpaEntity.getMimeType() == null || jpaEntity.getMimeType().trim().isEmpty()) {
            return false;
        }
        
        return true; // Basic validation passed
    }
    
    /**
     * Gets display name for media
     * @param jpaEntity the JPA entity
     * @return display name
     */
    public String getDisplayName(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return "Unknown Media";
        }
        
        if (jpaEntity.getTitle() != null && !jpaEntity.getTitle().trim().isEmpty()) {
            return jpaEntity.getTitle();
        }
        
        if (jpaEntity.getFileName() != null && !jpaEntity.getFileName().trim().isEmpty()) {
            return jpaEntity.getFileName();
        }
        
        return "Media " + jpaEntity.getId();
    }
    
    /**
     * Checks if media is an image
     * @param jpaEntity the JPA entity
     * @return true if this is an image
     */
    public boolean isImage(InfrastructureProductMediaJpaEntity jpaEntity) {
        return jpaEntity != null && "IMAGE".equals(jpaEntity.getMediaType().name());
    }
    
    /**
     * Checks if media is a video
     * @param jpaEntity the JPA entity
     * @return true if this is a video
     */
    public boolean isVideo(InfrastructureProductMediaJpaEntity jpaEntity) {
        return jpaEntity != null && "VIDEO".equals(jpaEntity.getMediaType().name());
    }
    
    /**
     * Checks if media is a document
     * @param jpaEntity the JPA entity
     * @return true if this is a document
     */
    public boolean isDocument(InfrastructureProductMediaJpaEntity jpaEntity) {
        return jpaEntity != null && "DOCUMENT".equals(jpaEntity.getMediaType().name());
    }
    
    /**
     * Gets formatted file size
     * @param jpaEntity the JPA entity
     * @return formatted file size string
     */
    public String getFormattedFileSize(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity == null || jpaEntity.getFileSize() == null) {
            return "Unknown";
        }
        
        long size = jpaEntity.getFileSize();
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets dimensions as string
     * @param jpaEntity the JPA entity
     * @return dimensions string or null
     */
    public String getDimensions(InfrastructureProductMediaJpaEntity jpaEntity) {
        if (jpaEntity == null || jpaEntity.getWidth() == null || jpaEntity.getHeight() == null) {
            return null;
        }
        
        return jpaEntity.getWidth() + "x" + jpaEntity.getHeight();
    }
    
    /**
     * Checks if media has dimensions
     * @param jpaEntity the JPA entity
     * @return true if width and height are both set
     */
    public boolean hasDimensions(InfrastructureProductMediaJpaEntity jpaEntity) {
        return jpaEntity != null && 
               jpaEntity.getWidth() != null && jpaEntity.getWidth() > 0 &&
               jpaEntity.getHeight() != null && jpaEntity.getHeight() > 0;
    }
}
