package ai.shreds.domain.specifications;

import ai.shreds.domain.enums.DomainAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Attribute Specification
 * Encapsulates filtering and pagination criteria for attribute queries
 * Used by domain services and repositories for attribute searches
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainAttributeSpecification {
    
    private Integer page;
    private Integer size;
    private String sort;
    private DomainAttributeType attributeType;
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
    public boolean hasTypeFilter() {
        return attributeType != null;
    }
    
    /**
     * Checks if filterable filtering is requested
     */
    public boolean hasFilterableFilter() {
        return isFilterable != null;
    }
    
    /**
     * Checks if searchable filtering is requested
     */
    public boolean hasSearchableFilter() {
        return isSearchable != null;
    }
    
    /**
     * Checks if required filtering is requested
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
        return hasTypeFilter() || hasFilterableFilter() || hasSearchableFilter() 
            || hasRequiredFilter() || hasActiveFilter() || hasSearchFilter();
    }
    
    /**
     * Factory method to create specification from application layer
     */
    public static DomainAttributeSpecification fromApplicationSpecification(Object appSpec) {
        if (appSpec == null) {
            return new DomainAttributeSpecification();
        }
        
        try {
            java.lang.reflect.Method getPage = appSpec.getClass().getMethod("getPage");
            java.lang.reflect.Method getSize = appSpec.getClass().getMethod("getSize");
            java.lang.reflect.Method getSort = appSpec.getClass().getMethod("getSort");
            java.lang.reflect.Method getAttributeType = appSpec.getClass().getMethod("getAttributeType");
            java.lang.reflect.Method getIsFilterable = appSpec.getClass().getMethod("getIsFilterable");
            java.lang.reflect.Method getIsSearchable = appSpec.getClass().getMethod("getIsSearchable");
            java.lang.reflect.Method getIsRequired = appSpec.getClass().getMethod("getIsRequired");
            java.lang.reflect.Method getIsActive = appSpec.getClass().getMethod("getIsActive");
            java.lang.reflect.Method getSearch = appSpec.getClass().getMethod("getSearch");
            
            Object typeObj = getAttributeType.invoke(appSpec);
            
            return DomainAttributeSpecification.builder()
                .page((Integer) getPage.invoke(appSpec))
                .size((Integer) getSize.invoke(appSpec))
                .sort((String) getSort.invoke(appSpec))
                .attributeType(typeObj != null ? 
                    DomainAttributeType.valueOf(typeObj.toString()) : null)
                .isFilterable((Boolean) getIsFilterable.invoke(appSpec))
                .isSearchable((Boolean) getIsSearchable.invoke(appSpec))
                .isRequired((Boolean) getIsRequired.invoke(appSpec))
                .isActive((Boolean) getIsActive.invoke(appSpec))
                .search((String) getSearch.invoke(appSpec))
                .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain specification from application specification", e);
        }
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
            .attributeType(type)
            .isActive(true)
            .build();
    }
}