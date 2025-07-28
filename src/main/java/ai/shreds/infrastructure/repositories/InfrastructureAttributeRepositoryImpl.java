package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.ports.DomainOutputPortAttributeRepository;
import ai.shreds.domain.specifications.DomainAttributeSpecification;
import ai.shreds.domain.value_objects.DomainPage;
import ai.shreds.infrastructure.entities.InfrastructureAttributeOptionJpaEntity;
import ai.shreds.infrastructure.entities.InfrastructureProductAttributeJpaEntity;
import ai.shreds.infrastructure.mappers.InfrastructureAttributeMapper;
import ai.shreds.infrastructure.specifications.InfrastructureAttributeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the attribute repository.
 * Implements the domain output port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InfrastructureAttributeRepositoryImpl implements DomainOutputPortAttributeRepository {

    private final InfrastructureAttributeJpaRepository attributeJpaRepository;
    private final InfrastructureAttributeOptionJpaRepository optionJpaRepository;
    private final InfrastructureProductAttributeValueJpaRepository attributeValueJpaRepository;
    private final InfrastructureAttributeMapper attributeMapper;

    @Override
    @Transactional
    public DomainProductAttributeEntity save(DomainProductAttributeEntity attribute) {
        log.debug("Saving attribute with ID: {}", attribute.getId());
        
        try {
            InfrastructureProductAttributeJpaEntity jpaEntity;
            
            if (attribute.getId() != null && attributeJpaRepository.existsById(attribute.getId())) {
                // Update existing attribute
                jpaEntity = attributeJpaRepository.findById(attribute.getId())
                        .orElseThrow(() -> new IllegalStateException("Attribute not found for update: " + attribute.getId()));
                attributeMapper.mergeDomainToJpa(attribute, jpaEntity);
            } else {
                // Create new attribute
                jpaEntity = attributeMapper.toJpaEntity(attribute);
                if (jpaEntity.getId() == null) {
                    jpaEntity.setId(UUID.randomUUID());
                }
            }
            
            InfrastructureProductAttributeJpaEntity savedEntity = attributeJpaRepository.save(jpaEntity);
            DomainProductAttributeEntity result = attributeMapper.toDomainEntity(savedEntity);
            
            log.debug("Successfully saved attribute with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error saving attribute with ID: {}", attribute.getId(), e);
            throw new RuntimeException("Failed to save attribute: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DomainAttributeOptionEntity saveOption(DomainAttributeOptionEntity option) {
        log.debug("Saving attribute option with ID: {}, for attribute ID: {}", option.getId(), option.getAttributeId());
        
        try {
            InfrastructureAttributeOptionJpaEntity jpaEntity;
            
            if (option.getId() != null && optionJpaRepository.existsById(option.getId())) {
                // Update existing option
                jpaEntity = optionJpaRepository.findById(option.getId())
                        .orElseThrow(() -> new IllegalStateException("Attribute option not found for update: " + option.getId()));
                attributeMapper.mergeOptionDomainToJpa(option, jpaEntity);
            } else {
                // Create new option
                jpaEntity = attributeMapper.mapOptionToJpa(option);
                if (jpaEntity.getId() == null) {
                    jpaEntity.setId(UUID.randomUUID());
                }
            }
            
            // Ensure attribute ID is set
            if (jpaEntity.getAttributeId() == null) {
                if (option.getAttributeId() != null) {
                    jpaEntity.setAttributeId(option.getAttributeId());
                } else {
                    throw new IllegalArgumentException("Attribute ID is required for attribute option");
                }
            }
            
            InfrastructureAttributeOptionJpaEntity savedEntity = optionJpaRepository.save(jpaEntity);
            DomainAttributeOptionEntity result = attributeMapper.mapOption(savedEntity);
            
            log.debug("Successfully saved attribute option with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error saving attribute option with ID: {}", option.getId(), e);
            throw new RuntimeException("Failed to save attribute option: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductAttributeEntity> findById(UUID id) {
        log.debug("Finding attribute by ID: {}", id);
        
        return attributeJpaRepository.findById(id)
                .map(attributeMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductAttributeEntity> findByCode(String code) {
        log.debug("Finding attribute by code: {}", code);
        
        return attributeJpaRepository.findByCode(code)
                .map(attributeMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public DomainPage<DomainProductAttributeEntity> findAll(DomainAttributeSpecification specification, Integer page, Integer size) {
        log.debug("Finding attributes with specification, page: {}, size: {}", page, size);
        
        try {
            // Create specification converter using static method
            Specification<InfrastructureProductAttributeJpaEntity> jpaSpec = 
                    (root, query, criteriaBuilder) -> InfrastructureAttributeSpecification.toCriteria(specification, criteriaBuilder, root);
            
            // Create page request with default sort (sort order, then name)
            Sort defaultSort = Sort.by(Sort.Direction.ASC, "sortOrder")
                    .and(Sort.by(Sort.Direction.ASC, "name"));
            PageRequest pageRequest = PageRequest.of(
                specification != null ? specification.getEffectivePage() : (page != null ? page : 0), 
                specification != null ? specification.getEffectiveSize() : (size != null ? size : 20), 
                defaultSort
            );
            
            // Execute query and get Spring Page
            Page<InfrastructureProductAttributeJpaEntity> jpaPage = 
                    attributeJpaRepository.findAll(jpaSpec, pageRequest);
            
            // Convert JPA entities to domain entities
            List<DomainProductAttributeEntity> domainEntities = jpaPage.getContent()
                    .stream()
                    .map(attributeMapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            // Convert Spring Page to DomainPage
            return DomainPage.of(
                domainEntities,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements()
            );
            
        } catch (Exception e) {
            log.error("Error finding attributes with specification", e);
            throw new RuntimeException("Failed to find attributes: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting attribute with ID: {}", id);
        
        try {
            if (!attributeJpaRepository.existsById(id)) {
                log.warn("Attempted to delete non-existent attribute: {}", id);
                return;
            }
            
            // Check if attribute is used by any products
            if (attributeValueJpaRepository.existsByAttributeId(id)) {
                log.warn("Cannot delete attribute {} as it is in use", id);
                throw new IllegalStateException("Cannot delete attribute that is in use by products.");
            }
            
            // Delete options first by finding and deleting them individually
            List<InfrastructureAttributeOptionJpaEntity> options = optionJpaRepository.findByAttributeId(id);
            for (InfrastructureAttributeOptionJpaEntity option : options) {
                optionJpaRepository.delete(option);
            }
            
            // Delete the attribute
            attributeJpaRepository.deleteById(id);
            
            log.debug("Successfully deleted attribute with ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting attribute with ID: {}", id, e);
            throw new RuntimeException("Failed to delete attribute: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        log.debug("Checking if attribute exists with code: {}", code);
        return attributeJpaRepository.existsByCode(code);
    }

    /**
     * Additional helper methods for the infrastructure layer
     */
    
    /**
     * Deletes an attribute option
     * @param optionId the option ID to delete
     */
    public void deleteOption(UUID optionId) {
        log.debug("Deleting attribute option with ID: {}", optionId);
        
        try {
            if (!optionJpaRepository.existsById(optionId)) {
                log.warn("Attempted to delete non-existent option: {}", optionId);
                return;
            }

            optionJpaRepository.deleteById(optionId);
            log.debug("Successfully deleted attribute option with ID: {}", optionId);
            
        } catch (Exception e) {
            log.error("Error deleting attribute option with ID: {}", optionId, e);
            throw new RuntimeException("Failed to delete attribute option: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if an attribute is in use by any products
     * @param attributeId the attribute ID to check
     * @return true if the attribute is in use
     */
    public boolean isAttributeInUse(UUID attributeId) {
        log.debug("Checking if attribute {} is in use", attributeId);
        
        try {
            return attributeValueJpaRepository.existsByAttributeId(attributeId);
        } catch (Exception e) {
            log.error("Error checking if attribute {} is in use", attributeId, e);
            return true; // Err on the safe side
        }
    }
    
    /**
     * Finds all attribute options for a specific attribute
     * @param attributeId the attribute ID
     * @return list of attribute options
     */
    public List<DomainAttributeOptionEntity> findOptionsByAttributeId(UUID attributeId) {
        return optionJpaRepository.findByAttributeId(attributeId)
                .stream()
                .map(attributeMapper::mapOption)
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if an attribute code is unique (excluding a specific attribute)
     * @param code the code to check
     * @param excludeId the attribute ID to exclude from the check
     * @return true if code is unique
     */
    public boolean isAttributeCodeUnique(String code, UUID excludeId) {
        if (excludeId != null) {
            return attributeJpaRepository.findByCode(code)
                    .map(attr -> !attr.getId().equals(excludeId))
                    .orElse(true);
        } else {
            return !attributeJpaRepository.existsByCode(code);
        }
    }
}