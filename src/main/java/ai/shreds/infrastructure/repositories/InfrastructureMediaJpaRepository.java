package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.InfrastructureProductMediaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for product media entities.
 * Provides CRUD operations and custom query methods for product media.
 */
@Repository
public interface InfrastructureMediaJpaRepository extends JpaRepository<InfrastructureProductMediaJpaEntity, UUID> {

    /**
     * Finds all media for a specific product, ordered by sort order
     * @param productId the product ID
     * @return list of media for the product
     */
    List<InfrastructureProductMediaJpaEntity> findByProductIdOrderBySortOrder(UUID productId);

    /**
     * Finds primary media for a product
     * @param productId the product ID
     * @return Optional containing the primary media if found
     */
    Optional<InfrastructureProductMediaJpaEntity> findByProductIdAndIsPrimaryTrue(UUID productId);

    /**
     * Finds non-primary media for a product
     * @param productId the product ID
     * @return list of non-primary media for the product
     */
    List<InfrastructureProductMediaJpaEntity> findByProductIdAndIsPrimaryFalseOrderBySortOrder(UUID productId);

    /**
     * Finds media by product and media type
     * @param productId the product ID
     * @param mediaType the media type
     * @return list of media of the specified type
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId AND m.mediaType = :mediaType ORDER BY m.sortOrder")
    List<InfrastructureProductMediaJpaEntity> findByProductIdAndMediaType(@Param("productId") UUID productId, @Param("mediaType") String mediaType);

    /**
     * Finds all images for a product
     * @param productId the product ID
     * @return list of image media
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId AND m.mediaType = 'IMAGE' ORDER BY m.sortOrder")
    List<InfrastructureProductMediaJpaEntity> findImagesByProductId(@Param("productId") UUID productId);

    /**
     * Finds all videos for a product
     * @param productId the product ID
     * @return list of video media
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId AND m.mediaType = 'VIDEO' ORDER BY m.sortOrder")
    List<InfrastructureProductMediaJpaEntity> findVideosByProductId(@Param("productId") UUID productId);

    /**
     * Finds all documents for a product
     * @param productId the product ID
     * @return list of document media
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId AND m.mediaType = 'DOCUMENT' ORDER BY m.sortOrder")
    List<InfrastructureProductMediaJpaEntity> findDocumentsByProductId(@Param("productId") UUID productId);

    /**
     * Counts media for a product
     * @param productId the product ID
     * @return count of media for the product
     */
    long countByProductId(UUID productId);

    /**
     * Counts primary media for a product (should be 0 or 1)
     * @param productId the product ID
     * @param isPrimary the primary flag
     * @return count of primary media
     */
    long countByProductIdAndIsPrimary(UUID productId, Boolean isPrimary);

    /**
     * Counts media by type for a product
     * @param productId the product ID
     * @param mediaType the media type
     * @return count of media of the specified type
     */
    @Query("SELECT COUNT(m) FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId AND m.mediaType = :mediaType")
    long countByProductIdAndMediaType(@Param("productId") UUID productId, @Param("mediaType") String mediaType);

    /**
     * Checks if a product has any media
     * @param productId the product ID
     * @return true if the product has media
     */
    boolean existsByProductId(UUID productId);

    /**
     * Checks if a product has primary media
     * @param productId the product ID
     * @return true if the product has primary media
     */
    boolean existsByProductIdAndIsPrimaryTrue(UUID productId);

    /**
     * Deletes all media for a product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);

    /**
     * Removes primary flag from all media for a product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductMediaJpaEntity m SET m.isPrimary = false WHERE m.productId = :productId")
    void removePrimaryFlagForProduct(@Param("productId") UUID productId);

    /**
     * Sets specific media as primary for a product
     * @param productId the product ID
     * @param mediaId the media ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductMediaJpaEntity m SET m.isPrimary = true WHERE m.productId = :productId AND m.id = :mediaId")
    void setPrimaryMedia(@Param("productId") UUID productId, @Param("mediaId") UUID mediaId);

    /**
     * Updates sort order for media
     * @param mediaId the media ID
     * @param sortOrder the new sort order
     */
    @Modifying
    @Transactional
    @Query("UPDATE InfrastructureProductMediaJpaEntity m SET m.sortOrder = :sortOrder WHERE m.id = :mediaId")
    void updateSortOrder(@Param("mediaId") UUID mediaId, @Param("sortOrder") Integer sortOrder);

    /**
     * Updates media order for multiple media items
     * @param productId the product ID
     * @param mediaOrder map of media ID to sort order
     */
    @Modifying
    @Transactional
    default void updateOrder(UUID productId, java.util.Map<UUID, Integer> mediaOrder) {
        mediaOrder.forEach(this::updateSortOrder);
    }

    /**
     * Finds media by URL
     * @param url the media URL
     * @return Optional containing the media if found
     */
    Optional<InfrastructureProductMediaJpaEntity> findByUrl(String url);

    /**
     * Finds media by file name pattern
     * @param fileNamePattern the file name pattern
     * @return list of media matching the pattern
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.fileName LIKE :fileNamePattern")
    List<InfrastructureProductMediaJpaEntity> findByFileNameLike(@Param("fileNamePattern") String fileNamePattern);

    /**
     * Finds the maximum sort order for a product's media
     * @param productId the product ID
     * @return the maximum sort order value
     */
    @Query("SELECT MAX(m.sortOrder) FROM InfrastructureProductMediaJpaEntity m WHERE m.productId = :productId")
    Optional<Integer> findMaxSortOrderForProduct(@Param("productId") UUID productId);

    /**
     * Finds media larger than specified file size
     * @param fileSize the file size threshold in bytes
     * @return list of media larger than the threshold
     */
    @Query("SELECT m FROM InfrastructureProductMediaJpaEntity m WHERE m.fileSize > :fileSize")
    List<InfrastructureProductMediaJpaEntity> findLargeFiles(@Param("fileSize") Long fileSize);

    /**
     * Finds media upload statistics by type
     * @return list of arrays containing [mediaType, count]
     */
    @Query("SELECT m.mediaType, COUNT(m) FROM InfrastructureProductMediaJpaEntity m GROUP BY m.mediaType")
    List<Object[]> getMediaStatisticsByType();
}