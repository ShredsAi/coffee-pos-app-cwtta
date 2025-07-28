package ai.shreds.infrastructure.repositories;

import ai.shreds.infrastructure.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository interface for audit operations.
 * This interface provides methods to save audit records for different entity types.
 * Each audit entity has its own table but they all follow the same pattern.
 */
@Repository
public interface InfrastructureAuditJpaRepository {

    /**
     * Saves a product audit record
     * @param audit the product audit entity to save
     */
    default void saveProductAudit(InfrastructureProductAuditJpaEntity audit) {
        getProductAuditRepository().save(audit);
    }

    /**
     * Saves a category audit record
     * @param audit the category audit entity to save
     */
    default void saveCategoryAudit(InfrastructureCategoryAuditJpaEntity audit) {
        getCategoryAuditRepository().save(audit);
    }

    /**
     * Saves an attribute audit record
     * @param audit the attribute audit entity to save
     */
    default void saveAttributeAudit(InfrastructureAttributeAuditJpaEntity audit) {
        getAttributeAuditRepository().save(audit);
    }

    /**
     * Saves a media audit record
     * @param audit the media audit entity to save
     */
    default void saveMediaAudit(InfrastructureMediaAuditJpaEntity audit) {
        getMediaAuditRepository().save(audit);
    }

    // Repository getters - to be implemented by the concrete implementation
    JpaRepository<InfrastructureProductAuditJpaEntity, Long> getProductAuditRepository();
    JpaRepository<InfrastructureCategoryAuditJpaEntity, Long> getCategoryAuditRepository();
    JpaRepository<InfrastructureAttributeAuditJpaEntity, Long> getAttributeAuditRepository();
    JpaRepository<InfrastructureMediaAuditJpaEntity, Long> getMediaAuditRepository();
}