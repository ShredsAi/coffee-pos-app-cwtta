package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedProductAttributeValueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain Product Attribute Value Entity
 * Represents actual attribute values assigned to products (EAV pattern)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainProductAttributeValueEntity {
    
    private UUID id;
    private UUID productId;
    private UUID attributeId;
    private DomainProductAttributeEntity attribute;
    private String textValue;
    private BigDecimal numericValue;
    private Boolean booleanValue;
    private LocalDate dateValue;
    
    @Builder.Default
    private List<DomainAttributeOptionEntity> selectedOptions = new ArrayList<>();
    
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    /**
     * Validates the attribute value against the attribute definition
     */
    public void validateAgainstAttribute() {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute definition is required for validation");
        }
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (attributeId == null) {
            throw new IllegalArgumentException("Attribute ID cannot be null");
        }
        
        // Check if required attribute has a value
        if (Boolean.TRUE.equals(attribute.getIsRequired()) && getValue() == null) {
            throw new IllegalArgumentException(
                String.format("Required attribute '%s' must have a value", attribute.getName())
            );
        }
        
        // Validate against attribute type
        Object value = getValue();
        if (value != null && !attribute.validateType(value)) {
            throw new IllegalArgumentException(
                String.format("Value type mismatch for attribute '%s' of type '%s'", 
                    attribute.getName(), attribute.getAttributeType())
            );
        }
        
        // Validate selected options for select types
        if (attribute.getAttributeType().isSelectType()) {
            validateSelectedOptions();
        }
    }
    
    /**
     * Validates selected options for select-type attributes
     */
    private void validateSelectedOptions() {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            if (Boolean.TRUE.equals(attribute.getIsRequired())) {
                throw new IllegalArgumentException(
                    String.format("Required select attribute '%s' must have at least one option selected", 
                        attribute.getName())
                );
            }
            return;
        }
        
        // Validate single selection for SELECT_SINGLE
        if (attribute.getAttributeType().name().equals("SELECT_SINGLE") && selectedOptions.size() > 1) {
            throw new IllegalArgumentException(
                String.format("Single-select attribute '%s' cannot have multiple options selected", 
                    attribute.getName())
            );
        }
        
        // Validate that all selected options belong to the attribute
        List<UUID> validOptionIds = attribute.getActiveOptions().stream()
            .map(DomainAttributeOptionEntity::getId)
            .toList();
        
        for (DomainAttributeOptionEntity option : selectedOptions) {
            if (!validOptionIds.contains(option.getId())) {
                throw new IllegalArgumentException(
                    String.format("Option '%s' is not valid for attribute '%s'", 
                        option.getValue(), attribute.getName())
                );
            }
        }
    }
    
    /**
     * Gets the actual value based on the attribute type
     */
    public Object getValue() {
        if (attribute == null) {
            return null;
        }
        
        switch (attribute.getAttributeType()) {
            case TEXT:
                return textValue;
            case NUMBER:
                return numericValue;
            case BOOLEAN:
                return booleanValue;
            case DATE:
                return dateValue;
            case SELECT_SINGLE:
                return selectedOptions.isEmpty() ? null : selectedOptions.get(0).getId();
            case SELECT_MULTIPLE:
                return selectedOptions.stream().map(DomainAttributeOptionEntity::getId).toList();
            default:
                return null;
        }
    }
    
    /**
     * Sets the value based on the attribute type
     */
    public void setValue(Object value) {
        if (attribute == null) {
            throw new IllegalStateException("Attribute definition is required to set value");
        }
        
        // Clear all values first
        clearValues();
        
        if (value == null) {
            return;
        }
        
        switch (attribute.getAttributeType()) {
            case TEXT:
                if (value instanceof String) {
                    this.textValue = (String) value;
                } else {
                    throw new IllegalArgumentException("Text attribute requires String value");
                }
                break;
            case NUMBER:
                if (value instanceof BigDecimal) {
                    this.numericValue = (BigDecimal) value;
                } else if (value instanceof Number) {
                    this.numericValue = new BigDecimal(value.toString());
                } else {
                    throw new IllegalArgumentException("Numeric attribute requires Number value");
                }
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    this.booleanValue = (Boolean) value;
                } else {
                    throw new IllegalArgumentException("Boolean attribute requires Boolean value");
                }
                break;
            case DATE:
                if (value instanceof LocalDate) {
                    this.dateValue = (LocalDate) value;
                } else {
                    throw new IllegalArgumentException("Date attribute requires LocalDate value");
                }
                break;
            case SELECT_SINGLE:
            case SELECT_MULTIPLE:
                setSelectedOptionsFromValue(value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported attribute type: " + attribute.getAttributeType());
        }
        
        this.updatedAt = Instant.now();
    }
    
    /**
     * Sets selected options from value (for select types)
     */
    private void setSelectedOptionsFromValue(Object value) {
        this.selectedOptions.clear();
        
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            for (Object val : values) {
                addSelectedOption((UUID) val);
            }
        } else if (value instanceof UUID) {
            addSelectedOption((UUID) value);
        } else {
            throw new IllegalArgumentException("Select attribute requires UUID or List<UUID> value");
        }
    }
    
    /**
     * Adds a selected option
     */
    private void addSelectedOption(UUID optionId) {
        DomainAttributeOptionEntity option = attribute.getActiveOptions().stream()
            .filter(opt -> opt.getId().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid option ID: " + optionId));
        
        this.selectedOptions.add(option);
    }
    
    /**
     * Clears all values
     */
    private void clearValues() {
        this.textValue = null;
        this.numericValue = null;
        this.booleanValue = null;
        this.dateValue = null;
        this.selectedOptions.clear();
    }
    
    /**
     * Updates the value and validates
     */
    public void updateValue(Object newValue) {
        setValue(newValue);
        validateAgainstAttribute();
    }
    
    /**
     * Deactivates the attribute value
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Activates the attribute value
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedProductAttributeValueDTO toDTO() {
        return SharedProductAttributeValueDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainProductAttributeValueEntity fromDTO(SharedProductAttributeValueDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainProductAttributeValueEntity.builder()
            .id(dto.getId())
            .productId(dto.getProductId())
            .attributeId(dto.getAttributeId())
            .attribute(dto.getAttribute() != null ? 
                DomainProductAttributeEntity.fromDTO(dto.getAttribute()) : null)
            .textValue(dto.getTextValue())
            .numericValue(dto.getNumericValue())
            .booleanValue(dto.getBooleanValue())
            .dateValue(dto.getDateValue())
            .selectedOptions(dto.getSelectedOptions() != null ?
                dto.getSelectedOptions().stream()
                    .map(DomainAttributeOptionEntity::fromDTO)
                    .toList() : new ArrayList<>())
            .isActive(dto.getIsActive())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a new attribute value
     */
    public static DomainProductAttributeValueEntity create(UUID productId, 
                                                          DomainProductAttributeEntity attribute, 
                                                          Object value) {
        
        Instant now = Instant.now();
        
        DomainProductAttributeValueEntity attributeValue = DomainProductAttributeValueEntity.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .attributeId(attribute.getId())
            .attribute(attribute)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .version(0L)
            .build();
        
        attributeValue.setValue(value);
        attributeValue.validateAgainstAttribute();
        
        return attributeValue;
    }
}