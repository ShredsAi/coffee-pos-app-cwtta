package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedAttributeOptionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Attribute Option Entity
 * Represents predefined options for select-type attributes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainAttributeOptionEntity {
    
    private UUID id;
    private UUID attributeId;
    private String value;
    private String code;
    private Integer sortOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    /**
     * Validates the option
     */
    public void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Option value cannot be null or empty");
        }
        
        if (attributeId == null) {
            throw new IllegalArgumentException("Attribute ID cannot be null");
        }
        
        // Ensure code is not empty if provided
        if (code != null && code.trim().isEmpty()) {
            code = null;
        }
    }
    
    /**
     * Updates the option properties
     */
    public void update(String value, String code, Integer sortOrder) {
        if (value != null && !value.trim().isEmpty()) {
            this.value = value;
        }
        
        if (code != null) {
            this.code = code.trim().isEmpty() ? null : code;
        }
        
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        
        this.updatedAt = Instant.now();
        
        this.validate();
    }
    
    /**
     * Deactivates the option
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Activates the option
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Converts domain entity to shared DTO
     */
    public SharedAttributeOptionDTO toDTO() {
        return SharedAttributeOptionDTO.fromEntity(this);
    }
    
    /**
     * Creates domain entity from shared DTO
     */
    public static DomainAttributeOptionEntity fromDTO(SharedAttributeOptionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return DomainAttributeOptionEntity.builder()
            .id(dto.getId())
            .attributeId(dto.getAttributeId())
            .value(dto.getValue())
            .code(dto.getCode())
            .sortOrder(dto.getSortOrder())
            .isActive(dto.getIsActive())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .version(dto.getVersion())
            .build();
    }
    
    /**
     * Factory method to create a new attribute option
     */
    public static DomainAttributeOptionEntity create(UUID attributeId, String value, 
                                                    String code, Integer sortOrder) {
        
        Instant now = Instant.now();
        
        DomainAttributeOptionEntity option = DomainAttributeOptionEntity.builder()
            .id(UUID.randomUUID())
            .attributeId(attributeId)
            .value(value)
            .code(code)
            .sortOrder(sortOrder != null ? sortOrder : 0)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .version(0L)
            .build();
        
        option.validate();
        return option;
    }
}