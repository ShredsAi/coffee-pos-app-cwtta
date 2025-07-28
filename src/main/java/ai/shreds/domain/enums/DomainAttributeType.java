package ai.shreds.domain.enums;

import java.util.Set;

/**
 * Domain Attribute Type Enum
 * Represents the different types of product attributes supported
 */
public enum DomainAttributeType {
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    SELECT_SINGLE,
    SELECT_MULTIPLE;
    
    /**
     * Checks if this attribute type is a select type (single or multiple)
     */
    public boolean isSelectType() {
        return this == SELECT_SINGLE || this == SELECT_MULTIPLE;
    }
    
    /**
     * Checks if this attribute type is numeric
     */
    public boolean isNumericType() {
        return this == NUMBER;
    }
    
    /**
     * Checks if this attribute type supports multiple values
     */
    public boolean supportsMultipleValues() {
        return this == SELECT_MULTIPLE;
    }
    
    /**
     * Checks if this attribute type requires options to be defined
     */
    public boolean requiresOptions() {
        return isSelectType();
    }
    
    /**
     * Gets the Java class type for this attribute type
     */
    public Class<?> getJavaType() {
        switch (this) {
            case TEXT:
                return String.class;
            case NUMBER:
                return java.math.BigDecimal.class;
            case BOOLEAN:
                return Boolean.class;
            case DATE:
                return java.time.LocalDate.class;
            case SELECT_SINGLE:
            case SELECT_MULTIPLE:
                return java.util.UUID.class; // Option IDs
            default:
                return Object.class;
        }
    }
    
    /**
     * Gets supported units for this attribute type
     */
    public Set<String> getSupportedUnits() {
        if (this == NUMBER) {
            return Set.of("kg", "g", "lb", "oz", "m", "cm", "mm", "in", "ft", "l", "ml", "gal", "pcs", "%");
        }
        return Set.of();
    }
    
    /**
     * Checks if this attribute type supports the given unit
     */
    public boolean supportsUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return true; // Unit is optional
        }
        return getSupportedUnits().contains(unit.toLowerCase());
    }
    
    /**
     * Checks if this attribute type can be used for filtering
     */
    public boolean canBeFilterable() {
        // All types can potentially be filterable
        return true;
    }
    
    /**
     * Checks if this attribute type can be used for full-text search
     */
    public boolean canBeSearchable() {
        // Only text attributes are truly searchable
        return this == TEXT;
    }
}