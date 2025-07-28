package ai.shreds.infrastructure.mappers;

import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.enums.DomainAttributeType;
import ai.shreds.infrastructure.entities.InfrastructureAttributeOptionJpaEntity;
import ai.shreds.infrastructure.entities.InfrastructureProductAttributeJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between attribute domain entities and JPA entities.
 * Handles both attribute definitions and their options.
 */
@Component
public class InfrastructureAttributeMapper {

    /**
     * Converts JPA entity to domain entity
     * @param jpaEntity the JPA entity
     * @return domain entity
     */
    public DomainProductAttributeEntity toDomainEntity(InfrastructureProductAttributeJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return DomainProductAttributeEntity.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .code(jpaEntity.getCode())
                .description(jpaEntity.getDescription())
                .attributeType(mapAttributeTypeToDomain(jpaEntity.getAttributeType()))
                .isRequired(jpaEntity.getIsRequired())
                .isFilterable(jpaEntity.getIsFilterable())
                .isSearchable(jpaEntity.getIsSearchable())
                .unit(jpaEntity.getUnit())
                .sortOrder(jpaEntity.getSortOrder())
                .options(mapOptionsToDomain(jpaEntity.getOptions()))
                .isActive(jpaEntity.getIsActive())
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
    public InfrastructureProductAttributeJpaEntity toJpaEntity(DomainProductAttributeEntity domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        return InfrastructureProductAttributeJpaEntity.builder()
                .id(domainEntity.getId())
                .name(domainEntity.getName())
                .code(domainEntity.getCode())
                .description(domainEntity.getDescription())
                .attributeType(mapAttributeTypeToJpa(domainEntity.getAttributeType()))
                .isRequired(domainEntity.getIsRequired())
                .isFilterable(domainEntity.getIsFilterable())
                .isSearchable(domainEntity.getIsSearchable())
                .unit(domainEntity.getUnit())
                .sortOrder(domainEntity.getSortOrder())
                .options(mapOptionsToJpa(domainEntity.getOptions()))
                .isActive(domainEntity.getIsActive())
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
    public void mergeDomainToJpa(DomainProductAttributeEntity domain, InfrastructureProductAttributeJpaEntity jpa) {
        if (domain == null || jpa == null) {
            return;
        }

        // Update basic fields
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setAttributeType(mapAttributeTypeToJpa(domain.getAttributeType()));
        jpa.setIsRequired(domain.getIsRequired());
        jpa.setIsFilterable(domain.getIsFilterable());
        jpa.setIsSearchable(domain.getIsSearchable());
        jpa.setUnit(domain.getUnit());
        jpa.setSortOrder(domain.getSortOrder());
        jpa.setIsActive(domain.getIsActive());
        jpa.setUpdatedAt(Instant.now());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setVersion(domain.getVersion());

        // Update options collection
        updateOptions(domain, jpa);
    }

    /**
     * Maps an attribute option from JPA to domain
     * @param jpaOption the JPA option entity
     * @return domain option entity
     */
    public DomainAttributeOptionEntity mapOption(InfrastructureAttributeOptionJpaEntity jpaOption) {
        if (jpaOption == null) {
            return null;
        }

        return DomainAttributeOptionEntity.builder()
                .id(jpaOption.getId())
                .attributeId(jpaOption.getAttributeId())
                .value(jpaOption.getValue())
                .code(jpaOption.getCode())
                .sortOrder(jpaOption.getSortOrder())
                .isActive(jpaOption.getIsActive())
                .createdAt(jpaOption.getCreatedAt())
                .updatedAt(jpaOption.getUpdatedAt())
                .version(jpaOption.getVersion())
                .build();
    }

    /**
     * Maps an attribute option from domain to JPA
     * @param domainOption the domain option entity
     * @return JPA option entity
     */
    public InfrastructureAttributeOptionJpaEntity mapOptionToJpa(DomainAttributeOptionEntity domainOption) {
        if (domainOption == null) {
            return null;
        }

        return InfrastructureAttributeOptionJpaEntity.builder()
                .id(domainOption.getId())
                .attributeId(domainOption.getAttributeId())
                .value(domainOption.getValue())
                .code(domainOption.getCode())
                .sortOrder(domainOption.getSortOrder())
                .isActive(domainOption.getIsActive())
                .createdAt(domainOption.getCreatedAt())
                .updatedAt(domainOption.getUpdatedAt())
                .version(domainOption.getVersion())
                .build();
    }

    /**
     * Merges domain option data into existing JPA option entity
     * @param domain the domain option source
     * @param jpa the JPA option target
     */
    public void mergeOptionDomainToJpa(DomainAttributeOptionEntity domain, InfrastructureAttributeOptionJpaEntity jpa) {
        if (domain == null || jpa == null) {
            return;
        }

        jpa.setValue(domain.getValue());
        jpa.setCode(domain.getCode());
        jpa.setSortOrder(domain.getSortOrder());
        jpa.setIsActive(domain.getIsActive());
        jpa.setUpdatedAt(Instant.now());
        jpa.setVersion(domain.getVersion());
    }

    /**
     * Helper methods for mapping collections and enums
     */
    
    private List<DomainAttributeOptionEntity> mapOptionsToDomain(List<InfrastructureAttributeOptionJpaEntity> jpaOptions) {
        if (jpaOptions == null || jpaOptions.isEmpty()) {
            return new ArrayList<>();
        }
        
        return jpaOptions.stream()
                .map(this::mapOption)
                .collect(Collectors.toList());
    }
    
    private List<InfrastructureAttributeOptionJpaEntity> mapOptionsToJpa(List<DomainAttributeOptionEntity> domainOptions) {
        if (domainOptions == null || domainOptions.isEmpty()) {
            return new ArrayList<>();
        }
        
        return domainOptions.stream()
                .map(this::mapOptionToJpa)
                .collect(Collectors.toList());
    }
    
    private void updateOptions(DomainProductAttributeEntity domain, InfrastructureProductAttributeJpaEntity jpa) {
        // Clear existing options
        if (jpa.getOptions() != null) {
            jpa.getOptions().clear();
        } else {
            jpa.setOptions(new ArrayList<>());
        }
        
        // Add new options
        if (domain.getOptions() != null) {
            List<InfrastructureAttributeOptionJpaEntity> newOptions = 
                    mapOptionsToJpa(domain.getOptions());
            
            // Set attribute reference for each option
            for (InfrastructureAttributeOptionJpaEntity option : newOptions) {
                option.setAttribute(jpa);
                option.setAttributeId(jpa.getId());
            }
            
            jpa.getOptions().addAll(newOptions);
        }
    }
    
    /**
     * Maps attribute type between domain and JPA representations
     */
    
    private DomainAttributeType mapAttributeTypeToDomain(InfrastructureProductAttributeJpaEntity.AttributeType jpaType) {
        if (jpaType == null) {
            return DomainAttributeType.TEXT;
        }
        
        try {
            return DomainAttributeType.valueOf(jpaType.name());
        } catch (IllegalArgumentException e) {
            return DomainAttributeType.TEXT; // Default fallback
        }
    }
    
    private InfrastructureProductAttributeJpaEntity.AttributeType mapAttributeTypeToJpa(DomainAttributeType domainType) {
        if (domainType == null) {
            return InfrastructureProductAttributeJpaEntity.AttributeType.TEXT;
        }
        
        return InfrastructureProductAttributeJpaEntity.AttributeType.valueOf(domainType.name());
    }
    
    /**
     * Utility methods
     */
    
    /**
     * Generates a new attribute ID if not present
     * @return new UUID
     */
    public UUID generateNewAttributeId() {
        return UUID.randomUUID();
    }
    
    /**
     * Generates a new option ID if not present
     * @return new UUID
     */
    public UUID generateNewOptionId() {
        return UUID.randomUUID();
    }
    
    /**
     * Sets audit fields for new attribute creation
     * @param jpaEntity the JPA entity
     * @param createdBy the user creating the attribute
     */
    public void setCreationAuditFields(InfrastructureProductAttributeJpaEntity jpaEntity, String createdBy) {
        Instant now = Instant.now();
        jpaEntity.setCreatedAt(now);
        jpaEntity.setUpdatedAt(now);
        jpaEntity.setCreatedBy(createdBy);
        jpaEntity.setUpdatedBy(createdBy);
        
        if (jpaEntity.getIsActive() == null) {
            jpaEntity.setIsActive(true);
        }
        if (jpaEntity.getIsRequired() == null) {
            jpaEntity.setIsRequired(false);
        }
        if (jpaEntity.getIsFilterable() == null) {
            jpaEntity.setIsFilterable(false);
        }
        if (jpaEntity.getIsSearchable() == null) {
            jpaEntity.setIsSearchable(false);
        }
        if (jpaEntity.getSortOrder() == null) {
            jpaEntity.setSortOrder(0);
        }
    }
    
    /**
     * Sets audit fields for attribute updates
     * @param jpaEntity the JPA entity
     * @param updatedBy the user updating the attribute
     */
    public void setUpdateAuditFields(InfrastructureProductAttributeJpaEntity jpaEntity, String updatedBy) {
        jpaEntity.setUpdatedAt(Instant.now());
        jpaEntity.setUpdatedBy(updatedBy);
    }
    
    /**
     * Sets audit fields for new option creation
     * @param jpaOption the JPA option entity
     */
    public void setOptionCreationAuditFields(InfrastructureAttributeOptionJpaEntity jpaOption) {
        Instant now = Instant.now();
        jpaOption.setCreatedAt(now);
        jpaOption.setUpdatedAt(now);
        
        if (jpaOption.getIsActive() == null) {
            jpaOption.setIsActive(true);
        }
        if (jpaOption.getSortOrder() == null) {
            jpaOption.setSortOrder(0);
        }
    }
    
    /**
     * Sets audit fields for option updates
     * @param jpaOption the JPA option entity
     */
    public void setOptionUpdateAuditFields(InfrastructureAttributeOptionJpaEntity jpaOption) {
        jpaOption.setUpdatedAt(Instant.now());
    }
    
    /**
     * Validates attribute type compatibility with options
     * @param attributeType the attribute type as string
     * @param hasOptions whether the attribute has options
     * @return true if valid combination
     */
    public boolean isValidAttributeTypeForOptions(String attributeType, boolean hasOptions) {
        if (attributeType == null) {
            return false;
        }
        
        boolean isSelectType = "SELECT_SINGLE".equals(attributeType) || "SELECT_MULTIPLE".equals(attributeType);
        
        // Select types should have options, non-select types should not
        return isSelectType == hasOptions;
    }
}
