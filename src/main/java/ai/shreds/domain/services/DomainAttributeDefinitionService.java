package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.domain.ports.DomainInputPortAttributeDefinitionService;
import ai.shreds.domain.ports.DomainOutputPortAttributeRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditWriter;
import ai.shreds.domain.exceptions.DomainAttributeValidationException;
import ai.shreds.domain.exceptions.DomainAttributeNotFoundException;
import ai.shreds.domain.value_objects.DomainAuditEntry;
import ai.shreds.domain.dtos.DomainCreateAttributeCommand;
import ai.shreds.domain.dtos.DomainUpdateAttributeCommand;
import ai.shreds.domain.dtos.DomainCreateOptionCommand;
import ai.shreds.domain.specifications.DomainAttributeSpecification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain Attribute Definition Service
 * Implements the domain business logic for attribute definition management
 */
@Service
public class DomainAttributeDefinitionService implements DomainInputPortAttributeDefinitionService {

    private final DomainOutputPortAttributeRepository attributeRepository;
    private final DomainValidationService validationService;
    private final DomainOutputPortAuditWriter auditWriter;

    /**
     * Constructor with dependencies
     */
    public DomainAttributeDefinitionService(
            DomainOutputPortAttributeRepository attributeRepository,
            DomainValidationService validationService,
            DomainOutputPortAuditWriter auditWriter) {
        this.attributeRepository = attributeRepository;
        this.validationService = validationService;
        this.auditWriter = auditWriter;
    }

    @Override
    public DomainProductAttributeEntity createAttribute(DomainCreateAttributeCommand command) {
        // Validate attribute code uniqueness
        validationService.validateAttributeUniqueness(command.getCode(), null);
        
        // Create attribute entity
        DomainProductAttributeEntity attribute = DomainProductAttributeEntity.create(
                command.getName(), 
                command.getCode(), 
                command.getDescription(),
                command.getAttributeType(),
                command.getIsRequired(),
                command.getIsFilterable(),
                command.getIsSearchable(),
                command.getUnit(),
                command.getSortOrder(),
                command.getCreatedBy());
        
        // Add options if provided and attribute type supports them
        if (command.getOptions() != null && !command.getOptions().isEmpty()) {
            if (!command.getAttributeType().isSelectType()) {
                throw new DomainAttributeValidationException(
                        command.getCode(), "Options can only be added to select-type attributes");
            }
            
            for (var optionCommand : command.getOptions()) {
                DomainAttributeOptionEntity option = DomainAttributeOptionEntity.create(
                        null, // Will be set after attribute is saved
                        optionCommand.getValue(),
                        optionCommand.getCode(),
                        optionCommand.getSortOrder());
                
                attribute.addOption(option);
            }
        }
        
        // Save the attribute
        DomainProductAttributeEntity savedAttribute = attributeRepository.save(attribute);
        
        // Write audit
        auditWriter.writeAttributeAudit(DomainAuditEntry.forCreate(
                savedAttribute, "ATTRIBUTE", command.getCreatedBy()));
        
        return savedAttribute;
    }

    @Override
    public DomainProductAttributeEntity updateAttribute(DomainUpdateAttributeCommand command) {
        // Find existing attribute
        DomainProductAttributeEntity existingAttribute = attributeRepository.findById(command.getId())
                .orElseThrow(() -> new DomainAttributeNotFoundException("id", command.getId().toString()));
        
        // Check version for optimistic locking
        if (!existingAttribute.getVersion().equals(command.getVersion())) {
            throw new IllegalStateException("Attribute has been modified by another user");
        }
        
        // Store the before state for audit
        DomainProductAttributeEntity beforeState = cloneAttribute(existingAttribute);
        
        // Update properties (only modifiable fields)
        existingAttribute.update(
                command.getName(),
                command.getDescription(),
                command.getIsRequired(),
                command.getIsFilterable(),
                command.getIsSearchable(),
                command.getUnit(),
                command.getSortOrder(),
                command.getUpdatedBy());
        
        // Update active status if provided
        if (command.getIsActive() != null) {
            if (command.getIsActive()) {
                existingAttribute.activate(command.getUpdatedBy());
            } else {
                existingAttribute.deactivate(command.getUpdatedBy());
            }
        }
        
        // Save the updated attribute
        DomainProductAttributeEntity updatedAttribute = attributeRepository.save(existingAttribute);
        
        // Write audit
        auditWriter.writeAttributeAudit(DomainAuditEntry.forUpdate(
                beforeState, updatedAttribute, "ATTRIBUTE", command.getUpdatedBy()));
        
        return updatedAttribute;
    }

    @Override
    public DomainProductAttributeEntity getAttribute(UUID id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new DomainAttributeNotFoundException("id", id.toString()));
    }

    @Override
    public List<DomainProductAttributeEntity> findAttributes(DomainAttributeSpecification specification) {
        return attributeRepository.findAll(
                specification, 
                specification.getEffectivePage(), 
                specification.getEffectiveSize())
                .getContent();
    }

    @Override
    public void deleteAttribute(UUID id) {
        DomainProductAttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new DomainAttributeNotFoundException("id", id.toString()));
        
        // Check if attribute is being used by products
        if (attributeRepository.isAttributeInUse(id)) {
            throw new DomainAttributeValidationException(
                    attribute.getCode(), "Cannot delete attribute that is being used by products");
        }
        
        // Create audit before deletion
        DomainAuditEntry auditEntry = DomainAuditEntry.forDelete(
                attribute, "ATTRIBUTE", attribute.getUpdatedBy());
        
        // Delete the attribute
        attributeRepository.delete(id);
        
        // Write audit after deletion
        auditWriter.writeAttributeAudit(auditEntry);
    }

    @Override
    public DomainAttributeOptionEntity addOption(DomainCreateOptionCommand command) {
        // Find the attribute
        DomainProductAttributeEntity attribute = attributeRepository.findById(command.getAttributeId())
                .orElseThrow(() -> new DomainAttributeNotFoundException("id", command.getAttributeId().toString()));
        
        // Validate that attribute is select type
        if (!attribute.isSelectType()) {
            throw new DomainAttributeValidationException(
                    attribute.getCode(), "Options can only be added to select-type attributes");
        }
        
        // Create and add the option
        DomainAttributeOptionEntity option = DomainAttributeOptionEntity.create(
                command.getAttributeId(),
                command.getValue(),
                command.getCode(),
                command.getSortOrder());
        
        // Validate option uniqueness within the attribute
        attribute.addOption(option);
        
        // Save the option
        DomainAttributeOptionEntity savedOption = attributeRepository.saveOption(option);
        
        // Update the attribute to include the new option
        attributeRepository.save(attribute);
        
        // Write audit for the attribute update
        auditWriter.writeAttributeAudit(DomainAuditEntry.forUpdate(
                attribute, attribute, "ATTRIBUTE", "system"));
        
        return savedOption;
    }
    
    /**
     * Creates a shallow clone of an attribute for audit purposes
     */
    private DomainProductAttributeEntity cloneAttribute(DomainProductAttributeEntity attribute) {
        return DomainProductAttributeEntity.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .code(attribute.getCode())
                .description(attribute.getDescription())
                .attributeType(attribute.getAttributeType())
                .isRequired(attribute.getIsRequired())
                .isFilterable(attribute.getIsFilterable())
                .isSearchable(attribute.getIsSearchable())
                .unit(attribute.getUnit())
                .sortOrder(attribute.getSortOrder())
                .isActive(attribute.getIsActive())
                .createdAt(attribute.getCreatedAt())
                .updatedAt(attribute.getUpdatedAt())
                .createdBy(attribute.getCreatedBy())
                .updatedBy(attribute.getUpdatedBy())
                .version(attribute.getVersion())
                .build();
    }
}