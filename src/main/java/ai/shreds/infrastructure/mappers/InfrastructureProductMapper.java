package ai.shreds.infrastructure.mappers;

import ai.shreds.domain.entities.*;
import ai.shreds.domain.enums.DomainPublicationStatus;
import ai.shreds.infrastructure.entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between product domain entities and JPA entities.
 * Handles the complex mapping including attributes, categories, and media.
 */
@Component
@RequiredArgsConstructor
public class InfrastructureProductMapper {

    private final InfrastructureAttributeMapper attributeMapper;
    private final InfrastructureCategoryMapper categoryMapper;
    private final InfrastructureMediaMapper mediaMapper;

    /**
     * Converts JPA entity to domain entity
     * @param jpaEntity the JPA entity
     * @return domain entity
     */
    public DomainProductEntity toDomainEntity(InfrastructureProductJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DomainProductEntity.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .shortDescription(jpaEntity.getShortDescription())
                .brand(jpaEntity.getBrand())
                .model(jpaEntity.getModel())
                .sku(jpaEntity.getSku())
                .slug(jpaEntity.getSlug())
                .publicationStatus(mapPublicationStatus(jpaEntity.getPublicationStatus()))
                .isActive(jpaEntity.getIsActive())
                .attributes(mapAttributeValues(jpaEntity.getAttributes()))
                .categories(mapCategories(jpaEntity.getCategories()))
                .media(mapMedia(jpaEntity.getMedia()))
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .createdBy(jpaEntity.getCreatedBy())
                .updatedBy(jpaEntity.getUpdatedBy())
                .version(jpaEntity.getVersion())
                .build();
    }

    /**
     * Converts domain entity to JPA entity
     * @param domainEntity the domain entity
     * @return JPA entity
     */
    public InfrastructureProductJpaEntity toJpaEntity(DomainProductEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        return InfrastructureProductJpaEntity.builder()
                .id(domainEntity.getId())
                .name(domainEntity.getName())
                .description(domainEntity.getDescription())
                .shortDescription(domainEntity.getShortDescription())
                .brand(domainEntity.getBrand())
                .model(domainEntity.getModel())
                .sku(domainEntity.getSku())
                .slug(domainEntity.getSlug())
                .publicationStatus(mapPublicationStatusToString(domainEntity.getPublicationStatus()))
                .isActive(domainEntity.getIsActive())
                .attributes(mapAttributeValuesToJpa(domainEntity.getAttributes()))
                .categories(mapCategoriesToJpa(domainEntity.getCategories()))
                .media(mapMediaToJpa(domainEntity.getMedia()))
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .createdBy(domainEntity.getCreatedBy())
                .updatedBy(domainEntity.getUpdatedBy())
                .version(domainEntity.getVersion())
                .build();
    }

    /**
     * Merges domain entity data into existing JPA entity
     * @param domain the domain entity source
     * @param jpa the JPA entity target
     */
    public void mergeDomainToJpa(DomainProductEntity domain, InfrastructureProductJpaEntity jpa) {
        if (domain == null || jpa == null) {
            return;
        }

        // Update basic fields
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setShortDescription(domain.getShortDescription());
        jpa.setBrand(domain.getBrand());
        jpa.setModel(domain.getModel());
        jpa.setSku(domain.getSku());
        jpa.setSlug(domain.getSlug());
        jpa.setPublicationStatus(mapPublicationStatusToString(domain.getPublicationStatus()));
        jpa.setIsActive(domain.getIsActive());
        jpa.setUpdatedAt(Instant.now());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setVersion(domain.getVersion());

        // Update collections - clear and rebuild
        updateAttributeValues(domain, jpa);
        updateCategories(domain, jpa);
        updateMedia(domain, jpa);
    }

    /**
     * Helper methods for mapping collections
     */
    
    private List<DomainProductAttributeValueEntity> mapAttributeValues(List<InfrastructureProductAttributeValueJpaEntity> jpaValues) {
        if (jpaValues == null || jpaValues.isEmpty()) {
            return new ArrayList<>();
        }
        
        return jpaValues.stream()
                .map(this::mapAttributeValue)
                .filter(value -> value != null)
                .collect(Collectors.toList());
    }
    
    private DomainProductAttributeValueEntity mapAttributeValue(InfrastructureProductAttributeValueJpaEntity jpaValue) {
        if (jpaValue == null) {
            return null;
        }
        
        return DomainProductAttributeValueEntity.builder()
                .id(jpaValue.getId())
                .productId(jpaValue.getProductId())
                .attributeId(jpaValue.getAttributeId())
                .attribute(jpaValue.getAttribute() != null ? 
                    attributeMapper.toDomainEntity(jpaValue.getAttribute()) : null)
                .textValue(jpaValue.getTextValue())
                .numericValue(jpaValue.getNumericValue())
                .booleanValue(jpaValue.getBooleanValue())
                .dateValue(jpaValue.getDateValue())
                .selectedOptions(jpaValue.getSelectedOptions() != null ?
                    jpaValue.getSelectedOptions().stream()
                        .map(attributeMapper::mapOption)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .isActive(jpaValue.getIsActive())
                .createdAt(jpaValue.getCreatedAt())
                .updatedAt(jpaValue.getUpdatedAt())
                .version(jpaValue.getVersion())
                .build();
    }
    
    private List<DomainProductCategoryEntity> mapCategories(List<InfrastructureProductCategoryJpaEntity> jpaCategories) {
        if (jpaCategories == null || jpaCategories.isEmpty()) {
            return new ArrayList<>();
        }
        
        return jpaCategories.stream()
                .map(this::mapCategory)
                .filter(category -> category != null)
                .collect(Collectors.toList());
    }
    
    private DomainProductCategoryEntity mapCategory(InfrastructureProductCategoryJpaEntity jpaCategory) {
        if (jpaCategory == null) {
            return null;
        }
        
        return DomainProductCategoryEntity.builder()
                .id(jpaCategory.getId())
                .productId(jpaCategory.getProductId())
                .categoryId(jpaCategory.getCategoryId())
                .category(jpaCategory.getCategory() != null ?
                    categoryMapper.toDomainEntity(jpaCategory.getCategory()) : null)
                .isPrimary(jpaCategory.getIsPrimary())
                .assignedAt(jpaCategory.getAssignedAt())
                .build();
    }
    
    private List<DomainProductMediaEntity> mapMedia(List<InfrastructureProductMediaJpaEntity> jpaMedia) {
        if (jpaMedia == null || jpaMedia.isEmpty()) {
            return new ArrayList<>();
        }
        
        return jpaMedia.stream()
                .map(mediaMapper::toDomainEntity)
                .filter(media -> media != null)
                .collect(Collectors.toList());
    }
    
    private List<InfrastructureProductAttributeValueJpaEntity> mapAttributeValuesToJpa(List<DomainProductAttributeValueEntity> domainValues) {
        if (domainValues == null || domainValues.isEmpty()) {
            return new ArrayList<>();
        }
        
        return domainValues.stream()
                .map(this::mapAttributeValueToJpa)
                .filter(value -> value != null)
                .collect(Collectors.toList());
    }
    
    private InfrastructureProductAttributeValueJpaEntity mapAttributeValueToJpa(DomainProductAttributeValueEntity domainValue) {
        if (domainValue == null) {
            return null;
        }
        
        return InfrastructureProductAttributeValueJpaEntity.builder()
                .id(domainValue.getId())
                .productId(domainValue.getProductId())
                .attributeId(domainValue.getAttributeId())
                .attribute(domainValue.getAttribute() != null ?
                    attributeMapper.toJpaEntity(domainValue.getAttribute()) : null)
                .textValue(domainValue.getTextValue())
                .numericValue(domainValue.getNumericValue())
                .booleanValue(domainValue.getBooleanValue())
                .dateValue(domainValue.getDateValue())
                .selectedOptions(domainValue.getSelectedOptions() != null ?
                    domainValue.getSelectedOptions().stream()
                        .map(attributeMapper::mapOptionToJpa)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .isActive(domainValue.getIsActive())
                .createdAt(domainValue.getCreatedAt())
                .updatedAt(domainValue.getUpdatedAt())
                .version(domainValue.getVersion())
                .build();
    }
    
    private List<InfrastructureProductCategoryJpaEntity> mapCategoriesToJpa(List<DomainProductCategoryEntity> domainCategories) {
        if (domainCategories == null || domainCategories.isEmpty()) {
            return new ArrayList<>();
        }
        
        return domainCategories.stream()
                .map(this::mapCategoryToJpa)
                .filter(category -> category != null)
                .collect(Collectors.toList());
    }
    
    private InfrastructureProductCategoryJpaEntity mapCategoryToJpa(DomainProductCategoryEntity domainCategory) {
        if (domainCategory == null) {
            return null;
        }
        
        return InfrastructureProductCategoryJpaEntity.builder()
                .id(domainCategory.getId())
                .productId(domainCategory.getProductId())
                .categoryId(domainCategory.getCategoryId())
                .category(domainCategory.getCategory() != null ?
                    categoryMapper.toJpaEntity(domainCategory.getCategory()) : null)
                .isPrimary(domainCategory.getIsPrimary())
                .assignedAt(domainCategory.getAssignedAt())
                .build();
    }
    
    private List<InfrastructureProductMediaJpaEntity> mapMediaToJpa(List<DomainProductMediaEntity> domainMedia) {
        if (domainMedia == null || domainMedia.isEmpty()) {
            return new ArrayList<>();
        }
        
        return domainMedia.stream()
                .map(mediaMapper::toJpaEntity)
                .filter(media -> media != null)
                .collect(Collectors.toList());
    }
    
    private void updateAttributeValues(DomainProductEntity domain, InfrastructureProductJpaEntity jpa) {
        // Clear existing attributes
        if (jpa.getAttributes() != null) {
            jpa.getAttributes().clear();
        } else {
            jpa.setAttributes(new ArrayList<>());
        }
        
        // Add new attributes
        if (domain.getAttributes() != null) {
            List<InfrastructureProductAttributeValueJpaEntity> newAttributes = 
                    mapAttributeValuesToJpa(domain.getAttributes());
            jpa.getAttributes().addAll(newAttributes);
        }
    }
    
    private void updateCategories(DomainProductEntity domain, InfrastructureProductJpaEntity jpa) {
        // Clear existing categories
        if (jpa.getCategories() != null) {
            jpa.getCategories().clear();
        } else {
            jpa.setCategories(new ArrayList<>());
        }
        
        // Add new categories
        if (domain.getCategories() != null) {
            List<InfrastructureProductCategoryJpaEntity> newCategories = 
                    mapCategoriesToJpa(domain.getCategories());
            jpa.getCategories().addAll(newCategories);
        }
    }
    
    private void updateMedia(DomainProductEntity domain, InfrastructureProductJpaEntity jpa) {
        // Clear existing media
        if (jpa.getMedia() != null) {
            jpa.getMedia().clear();
        } else {
            jpa.setMedia(new ArrayList<>());
        }
        
        // Add new media
        if (domain.getMedia() != null) {
            List<InfrastructureProductMediaJpaEntity> newMedia = 
                    mapMediaToJpa(domain.getMedia());
            jpa.getMedia().addAll(newMedia);
        }
    }
    
    /**
     * Maps publication status between domain and infrastructure representations
     */
    
    private DomainPublicationStatus mapPublicationStatus(String jpaStatus) {
        if (jpaStatus == null || jpaStatus.trim().isEmpty()) {
            return DomainPublicationStatus.DRAFT;
        }
        
        try {
            return DomainPublicationStatus.valueOf(jpaStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DomainPublicationStatus.DRAFT;
        }
    }
    
    private String mapPublicationStatusToString(DomainPublicationStatus domainStatus) {
        return domainStatus != null ? domainStatus.name() : DomainPublicationStatus.DRAFT.name();
    }
    
    /**
     * Utility methods
     */
    
    /**
     * Creates a new product ID if not present
     * @return new UUID
     */
    public UUID generateNewProductId() {
        return UUID.randomUUID();
    }
    
    /**
     * Sets audit fields for new product creation
     * @param jpaEntity the JPA entity
     * @param createdBy the user creating the product
     */
    public void setCreationAuditFields(InfrastructureProductJpaEntity jpaEntity, String createdBy) {
        Instant now = Instant.now();
        jpaEntity.setCreatedAt(now);
        jpaEntity.setUpdatedAt(now);
        jpaEntity.setCreatedBy(createdBy);
        jpaEntity.setUpdatedBy(createdBy);
        
        if (jpaEntity.getIsActive() == null) {
            jpaEntity.setIsActive(true);
        }
    }
    
    /**
     * Sets audit fields for product updates
     * @param jpaEntity the JPA entity
     * @param updatedBy the user updating the product
     */
    public void setUpdateAuditFields(InfrastructureProductJpaEntity jpaEntity, String updatedBy) {
        jpaEntity.setUpdatedAt(Instant.now());
        jpaEntity.setUpdatedBy(updatedBy);
    }
}