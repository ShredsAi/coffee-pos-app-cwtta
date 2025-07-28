package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainAttributeType;
import ai.shreds.shared.dtos.SharedProductAttributeDTO;
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
 * Domain Product Attribute Entity
 * Represents attribute definitions that can be applied to products
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainProductAttributeEntity {
    
    private UUID id;
    private String name;
    private String code;
    private String description;
    private DomainAttributeType attributeType;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Boolean isSearchable;
    private String unit;
    private Integer sortOrder;
    
    @Builder.Default
    private List<DomainAttributeOptionEntity> options = new ArrayList<>();
    
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    
    /**
     * Validates if a value is compatible with this attribute's type
     */
    public boolean validateType(Object value) {
        if (value == null) {
            return !Boolean.TRUE.equals(this.isRequired);
        }
        
        switch (this.attributeType) {
            case TEXT:
                return value instanceof String;
            case NUMBER:
                return value instanceof BigDecimal || value instanceof Number;
            case BOOLEAN:
                return value instanceof Boolean;
            case DATE:
                return value instanceof LocalDate;
            case SELECT_SINGLE:
            case SELECT_MULTIPLE:
                // For select types, validate against available options
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    return values.stream().allMatch(this::isValidOption);
                } else {
                    return isValidOption(value);
                }
            default:
                return false;
        }
    }
    
    /**
     * Checks if a value is a valid option for select-type attributes
     */
    private boolean isValidOption(Object value) {
        if (!(value instanceof UUID)) {
            return false;
        }
        
        UUID optionId = (UUID) value;
        return this.options.stream()
            .anyMatch(option -> option.getId().equals(optionId) && Boolean.TRUE.equals(option.getIsActive()));
    }
    
    /**
     * Adds an option to this attribute (for select types)
     */
    public void addOption(DomainAttributeOptionEntity option) {
        if (option == null) {
            throw new IllegalArgumentException("Option cannot be null");
        }
        
        if (!this.attributeType.isSelectType()) {
            throw new IllegalStateException("Options can only be added to select-type attributes");
        }
        
        // Check for duplicate codes
        boolean codeExists = this.options.stream()
            .anyMatch(existing -> existing.getCode().equals(option.getCode()));
        
        if (codeExists) {
            throw new IllegalArgumentException(
                String.format("Option with code '%s' already exists", option.getCode())
            );
        }
        
        option.setAttributeId(this.id);
        this.options.add(option);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Removes an option from this attribute
     */
    public void removeOption(UUID optionId) {
        if (optionId == null) {
            throw new IllegalArgumentException("Option ID cannot be null");
        }
        
        boolean removed = this.options.removeIf(option -> option.getId().equals(optionId));
        
        if (removed) {
            this.updatedAt = Instant.now();
        }
    }
    
    /**
     * Gets active options for this attribute
     */
    public List<DomainAttributeOptionEntity> getActiveOptions() {
        return this.options.stream()
            .filter(option -> Boolean.TRUE.equals(option.getIsActive()))
            .toList();
    }
    
    /**
     * Validates the attribute definition
     */
    public void validate() {
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
        
        if (this.code == null || this.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute code cannot be null or empty");
        }
        
        if (this.attributeType == null) {
            throw new IllegalArgumentException("Attribute type cannot be null");
        }
        
        // Validate that select types have options
        if (this.attributeType.isSelectType() && this.options.isEmpty()) {
            throw new IllegalArgumentException("Select-type attributes must have at least one option");
        }
        
        // Validate unit for numeric attributes
        if (this.attributeType == DomainAttributeType.NUMBER && 
            this.unit != null && this.unit.trim().isEmpty()) {
            this.unit = null; // Clean up empty unit
        }
    }
    
    /**
     * Updates the attribute properties
     */
    public void update(String name, String description, Boolean isRequired, 
                      Boolean isFilterable, Boolean isSearchable, String unit, 
                      Integer sortOrder, String updatedBy) {
        
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        
        this.description = description;
        this.isRequired = isRequired;
        this.isFilterable = isFilterable;
        this.isSearchable = isSearchable;
        
        // Only update unit for numeric types
        if (this.attributeType == DomainAttributeType.NUMBER) {
            this.unit = unit;
        }
        
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
        
        this.validate();
    }
    
    /**
     * Deactivates the attribute
     */
    public void deactivate(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Reactivates the attribute
     */
    public void activate(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Checks if this attribute supports multiple values
     */
    public boolean supportsMultipleValues() {
        return this.attributeType == DomainAttributeType.SELECT_MULTIPLE;
    }
    
    /**
     * Checks if this attribute is a select type
     */
    public boolean isSelectType() {
        return this.attributeType.isSelectType();
    }
    
    /**
     * Checks if this attribute is numeric
     */
    public boolean isNumericType() {
        return this.attributeType.isNumericType();
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedProductAttributeDTO toDTO() {
        return SharedProductAttributeDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainProductAttributeEntity fromDTO(SharedProductAttributeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainProductAttributeEntity.builder()
            .id(dto.getId())
            .name(dto.getName())
            .code(dto.getCode())
            .description(dto.getDescription())
            .attributeType(DomainAttributeType.valueOf(dto.getAttributeType().name()))
            .isRequired(dto.getIsRequired())
            .isFilterable(dto.getIsFilterable())
            .isSearchable(dto.getIsSearchable())
            .unit(dto.getUnit())
            .sortOrder(dto.getSortOrder())
            .isActive(dto.getIsActive())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .createdBy(dto.getCreatedBy())
            .updatedBy(dto.getUpdatedBy())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a new attribute
     */
    public static DomainProductAttributeEntity create(String name, String code, String description,
                                                     DomainAttributeType attributeType, Boolean isRequired,
                                                     Boolean isFilterable, Boolean isSearchable, String unit,
                                                     Integer sortOrder, String createdBy) {
        
        Instant now = Instant.now();
        
        DomainProductAttributeEntity attribute = DomainProductAttributeEntity.builder()
            .id(UUID.randomUUID())
            .name(name)
            .code(code)
            .description(description)
            .attributeType(attributeType)
            .isRequired(isRequired != null ? isRequired : false)
            .isFilterable(isFilterable != null ? isFilterable : false)
            .isSearchable(isSearchable != null ? isSearchable : false)
            .unit(unit)
            .sortOrder(sortOrder != null ? sortOrder : 0)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy)
            .updatedBy(createdBy)
            .version(0L)
            .build();
        
        attribute.validate();
        return attribute;
    }
}