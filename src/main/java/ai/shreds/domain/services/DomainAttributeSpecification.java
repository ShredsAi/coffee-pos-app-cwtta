package ai.shreds.domain.services;

import ai.shreds.domain.enums.DomainAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Attribute Specification
 * Encapsulates filtering and search criteria for product attributes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainAttributeSpecification {
    
    private Integer page;
    private Integer size;
    private String sort;
    private String attributeType;
    private Boolean isFilterable;
    private Boolean isSearchable;
    private Boolean isRequired;
    private Boolean isActive;
    private String search;
    
    // Default pagination values
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    
    /**
     * Gets the effective page number (with default)
     */
    public int getEffectivePage() {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }
    
    /**
     * Gets the effective page size (with default and max limit)
     */
    public int getEffectiveSize() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
    
    /**
     * Gets the effective sort direction and field
     */
    public String getEffectiveSort() {
        if (sort == null || sort.trim().isEmpty()) {
            return "name,asc"; // Default sort by name ascending
        }
        return sort;
    }
    
    /**
     * Checks if attribute type filtering is requested
     */
    public boolean hasAttributeTypeFilter() {
        return attributeType != null && !attributeType.trim().isEmpty();
    }
    
    /**
     * Checks if filterable status filtering is requested
     */
    public boolean hasFilterableFilter() {
        return isFilterable != null;
    }
    
    /**
     * Checks if searchable status filtering is requested
     */
    public boolean hasSearchableFilter() {
        return isSearchable != null;
    }
    
    /**
     * Checks if required status filtering is requested
     */
    public boolean hasRequiredFilter() {
        return isRequired != null;
    }
    
    /**
     * Checks if active status filtering is requested
     */
    public boolean hasActiveFilter() {
        return isActive != null;
    }
    
    /**
     * Checks if search text filtering is requested
     */
    public boolean hasSearchFilter() {
        return search != null && !search.trim().isEmpty();
    }
    
    /**
     * Checks if any filtering criteria is specified
     */
    public boolean hasFilters() {
        return hasAttributeTypeFilter() || hasFilterableFilter() || hasSearchableFilter() 
            || hasRequiredFilter() || hasActiveFilter() || hasSearchFilter();
    }
    
    /**
     * Creates a specification for active attributes only
     */
    public static DomainAttributeSpecification activeAttributes() {
        return DomainAttributeSpecification.builder()
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for filterable attributes only
     */
    public static DomainAttributeSpecification filterableAttributes() {
        return DomainAttributeSpecification.builder()
            .isFilterable(true)
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for searchable attributes only
     */
    public static DomainAttributeSpecification searchableAttributes() {
        return DomainAttributeSpecification.builder()
            .isSearchable(true)
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for required attributes only
     */
    public static DomainAttributeSpecification requiredAttributes() {
        return DomainAttributeSpecification.builder()
            .isRequired(true)
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for attributes of a specific type
     */
    public static DomainAttributeSpecification byType(DomainAttributeType type) {
        return DomainAttributeSpecification.builder()
            .attributeType(type.name())
            .build();
    }
    
    /**
     * Creates a specification for attribute search
     */
    public static DomainAttributeSpecification search(String searchTerm) {
        return DomainAttributeSpecification.builder()
            .search(searchTerm)
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification to find all attributes
     */
    public static DomainAttributeSpecification findAll() {
        return DomainAttributeSpecification.builder()
            .page(0)
            .size(20)
            .build();
    }
}