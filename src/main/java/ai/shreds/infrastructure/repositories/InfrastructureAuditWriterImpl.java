package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.ports.DomainOutputPortAuditWriter;
import ai.shreds.domain.value_objects.DomainAuditEntry;
import ai.shreds.infrastructure.entities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Map;
import java.util.List;

/**
 * Infrastructure implementation of the audit writer.
 * Implements the domain output port using Spring Data JPA repositories.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InfrastructureAuditWriterImpl implements DomainOutputPortAuditWriter {

    private final InfrastructureProductAuditRepository productAuditRepository;
    private final InfrastructureCategoryAuditRepository categoryAuditRepository;
    private final InfrastructureAttributeAuditRepository attributeAuditRepository;
    private final InfrastructureMediaAuditRepository mediaAuditRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeProductAudit(DomainAuditEntry entry) {
        log.debug("Writing product audit entry for entity ID: {}, change type: {}", 
                entry.getEntityId(), entry.getChangeType());
        
        try {
            InfrastructureProductAuditJpaEntity auditEntity = createProductAuditEntity(entry);
            productAuditRepository.save(auditEntity);
            
            log.debug("Successfully wrote product audit entry with ID: {}", auditEntity.getAuditId());
            
        } catch (Exception e) {
            log.error("Error writing product audit entry for entity ID: {}", entry.getEntityId(), e);
            // Don't propagate audit failures - they shouldn't break main operations
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeCategoryAudit(DomainAuditEntry entry) {
        log.debug("Writing category audit entry for entity ID: {}, change type: {}", 
                entry.getEntityId(), entry.getChangeType());
        
        try {
            InfrastructureCategoryAuditJpaEntity auditEntity = createCategoryAuditEntity(entry);
            categoryAuditRepository.save(auditEntity);
            
            log.debug("Successfully wrote category audit entry with ID: {}", auditEntity.getAuditId());
            
        } catch (Exception e) {
            log.error("Error writing category audit entry for entity ID: {}", entry.getEntityId(), e);
            // Don't propagate audit failures - they shouldn't break main operations
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAttributeAudit(DomainAuditEntry entry) {
        log.debug("Writing attribute audit entry for entity ID: {}, change type: {}", 
                entry.getEntityId(), entry.getChangeType());
        
        try {
            InfrastructureAttributeAuditJpaEntity auditEntity = createAttributeAuditEntity(entry);
            attributeAuditRepository.save(auditEntity);
            
            log.debug("Successfully wrote attribute audit entry with ID: {}", auditEntity.getAuditId());
            
        } catch (Exception e) {
            log.error("Error writing attribute audit entry for entity ID: {}", entry.getEntityId(), e);
            // Don't propagate audit failures - they shouldn't break main operations
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeMediaAudit(DomainAuditEntry entry) {
        log.debug("Writing media audit entry for entity ID: {}, change type: {}", 
                entry.getEntityId(), entry.getChangeType());
        
        try {
            InfrastructureMediaAuditJpaEntity auditEntity = createMediaAuditEntity(entry);
            mediaAuditRepository.save(auditEntity);
            
            log.debug("Successfully wrote media audit entry with ID: {}", auditEntity.getAuditId());
            
        } catch (Exception e) {
            log.error("Error writing media audit entry for entity ID: {}", entry.getEntityId(), e);
            // Don't propagate audit failures - they shouldn't break main operations
        }
    }

    /**
     * Generic audit write for any entity type
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAudit(DomainAuditEntry entry, String entityType) {
        if (entityType == null) {
            log.warn("Entity type is null for generic audit, skipping audit");
            return;
        }
        String type = entityType.trim().toLowerCase();
        switch (type) {
            case "product":
                writeProductAudit(entry);
                break;
            case "category":
                writeCategoryAudit(entry);
                break;
            case "attribute":
                writeAttributeAudit(entry);
                break;
            case "media":
                writeMediaAudit(entry);
                break;
            default:
                log.warn("Unknown entity type '{}' for generic audit, skipping specific audit. Logging raw entry", entityType);
                // Optionally, handle default or store raw
                break;
        }
    }

    /**
     * Helper methods to create audit entities from domain audit entries
     */
    
    private InfrastructureProductAuditJpaEntity createProductAuditEntity(DomainAuditEntry entry) {
        return InfrastructureProductAuditJpaEntity.builder()
                .productId(parseEntityId(entry.getEntityId()))
                .changeType(parseChangeType(entry.getChangeType()))
                .beforeState(serializeState(entry.getBeforeState()))
                .afterState(serializeState(entry.getAfterState()))
                .changedAt(entry.getChangedAt())
                .changedBy(entry.getChangedBy())
                .build();
    }

    private InfrastructureCategoryAuditJpaEntity createCategoryAuditEntity(DomainAuditEntry entry) {
        return InfrastructureCategoryAuditJpaEntity.builder()
                .categoryId(parseEntityId(entry.getEntityId()))
                .changeType(parseCategoryChangeType(entry.getChangeType()))
                .beforeState(serializeState(entry.getBeforeState()))
                .afterState(serializeState(entry.getAfterState()))
                .changedAt(entry.getChangedAt())
                .changedBy(entry.getChangedBy())
                .build();
    }

    private InfrastructureAttributeAuditJpaEntity createAttributeAuditEntity(DomainAuditEntry entry) {
        return InfrastructureAttributeAuditJpaEntity.builder()
                .attributeId(parseEntityId(entry.getEntityId()))
                .changeType(parseAttributeChangeType(entry.getChangeType()))
                .beforeState(serializeState(entry.getBeforeState()))
                .afterState(serializeState(entry.getAfterState()))
                .changedAt(entry.getChangedAt())
                .changedBy(entry.getChangedBy())
                .build();
    }

    private InfrastructureMediaAuditJpaEntity createMediaAuditEntity(DomainAuditEntry entry) {
        return InfrastructureMediaAuditJpaEntity.builder()
                .mediaId(parseEntityId(entry.getEntityId()))
                .changeType(parseMediaChangeType(entry.getChangeType()))
                .beforeState(serializeState(entry.getBeforeState()))
                .afterState(serializeState(entry.getAfterState()))
                .changedAt(entry.getChangedAt())
                .changedBy(entry.getChangedBy())
                .build();
    }

    /**
     * Helper methods for parsing and serialization
     */
    
    private UUID parseEntityId(UUID entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID cannot be null for audit entry");
        }
        return entityId;
    }

    private InfrastructureProductAuditJpaEntity.ChangeType parseChangeType(String changeType) {
        try {
            return InfrastructureProductAuditJpaEntity.ChangeType.valueOf(changeType.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown change type for product audit: {}, defaulting to UPDATE", changeType);
            return InfrastructureProductAuditJpaEntity.ChangeType.UPDATE;
        }
    }

    private InfrastructureCategoryAuditJpaEntity.ChangeType parseCategoryChangeType(String changeType) {
        try {
            return InfrastructureCategoryAuditJpaEntity.ChangeType.valueOf(changeType.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown change type for category audit: {}, defaulting to UPDATE", changeType);
            return InfrastructureCategoryAuditJpaEntity.ChangeType.UPDATE;
        }
    }

    private InfrastructureAttributeAuditJpaEntity.ChangeType parseAttributeChangeType(String changeType) {
        try {
            return InfrastructureAttributeAuditJpaEntity.ChangeType.valueOf(changeType.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown change type for attribute audit: {}, defaulting to UPDATE", changeType);
            return InfrastructureAttributeAuditJpaEntity.ChangeType.UPDATE;
        }
    }

    private InfrastructureMediaAuditJpaEntity.ChangeType parseMediaChangeType(String changeType) {
        try {
            return InfrastructureMediaAuditJpaEntity.ChangeType.valueOf(changeType.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown change type for media audit: {}, defaulting to UPDATE", changeType);
            return InfrastructureMediaAuditJpaEntity.ChangeType.UPDATE;
        }
    }

    private String serializeState(java.util.Map<String, Object> state) {
        if (state == null || state.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit state to JSON", e);
            return "{\"error\":\"Failed to serialize state\"}";
        }
    }

    /**
     * Utility methods for retrieving audit history
     */
    
    /**
     * Retrieves audit history for a product
     * @param productId the product ID
     * @return list of audit entries ordered by change timestamp
     */
    public java.util.List<InfrastructureProductAuditJpaEntity> getProductAuditHistory(UUID productId) {
        return productAuditRepository.findByProductIdOrderByChangedAtDesc(productId);
    }
    
    /**
     * Retrieves audit history for a category
     * @param categoryId the category ID
     * @return list of audit entries ordered by change timestamp
     */
    public java.util.List<InfrastructureCategoryAuditJpaEntity> getCategoryAuditHistory(UUID categoryId) {
        return categoryAuditRepository.findByCategoryIdOrderByChangedAtDesc(categoryId);
    }
    
    /**
     * Retrieves audit history for an attribute
     * @param attributeId the attribute ID
     * @return list of audit entries ordered by change timestamp
     */
    public java.util.List<InfrastructureAttributeAuditJpaEntity> getAttributeAuditHistory(UUID attributeId) {
        return attributeAuditRepository.findByAttributeIdOrderByChangedAtDesc(attributeId);
    }
    
    /**
     * Retrieves audit history for media
     * @param mediaId the media ID
     * @return list of audit entries ordered by change timestamp
     */
    public java.util.List<InfrastructureMediaAuditJpaEntity> getMediaAuditHistory(UUID mediaId) {
        return mediaAuditRepository.findByMediaIdOrderByChangedAtDesc(mediaId);
    }
    
    /**
     * Retrieves recent changes across all entities
     * @param since the timestamp to check changes since
     * @return combined list of recent changes
     */
    public java.util.Map<String, Object> getRecentChanges(java.time.Instant since) {
        return Map.of(
                "products", productAuditRepository.findRecentChanges(since),
                "categories", categoryAuditRepository.findRecentChanges(since),
                "attributes", attributeAuditRepository.findRecentChanges(since),
                "media", mediaAuditRepository.findRecentChanges(since)
        );
    }
}
