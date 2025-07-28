package ai.shreds.domain.specifications;

import ai.shreds.domain.enums.DomainPublicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain Product Specification
 * Encapsulates filtering and pagination criteria for product queries
 * Used by domain services and repositories for product searches
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainProductSpecification {
    
    private Integer page;
    private Integer size;
    private String sort;
    private UUID categoryId;
    private String brand;
    private DomainPublicationStatus publicationStatus;
    private String search;
    private Boolean isActive;
    
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
     * Checks if category filtering is requested
     */
    public boolean hasCategoryFilter() {
        return categoryId != null;
    }
    
    /**
     * Checks if brand filtering is requested
     */
    public boolean hasBrandFilter() {
        return brand != null && !brand.trim().isEmpty();
    }
    
    /**
     * Checks if publication status filtering is requested
     */
    public boolean hasPublicationStatusFilter() {
        return publicationStatus != null;
    }
    
    /**
     * Checks if search text filtering is requested
     */
    public boolean hasSearchFilter() {
        return search != null && !search.trim().isEmpty();
    }
    
    /**
     * Checks if active status filtering is requested
     */
    public boolean hasActiveFilter() {
        return isActive != null;
    }
    
    /**
     * Checks if any filtering criteria is specified
     */
    public boolean hasFilters() {
        return hasCategoryFilter() || hasBrandFilter() || hasPublicationStatusFilter() 
            || hasSearchFilter() || hasActiveFilter();
    }
    
    /**
     * Factory method to create specification from application layer
     */
    public static DomainProductSpecification fromApplicationSpecification(Object appSpec) {
        if (appSpec == null) {
            return new DomainProductSpecification();
        }
        
        try {
            java.lang.reflect.Method getPage = appSpec.getClass().getMethod("getPage");
            java.lang.reflect.Method getSize = appSpec.getClass().getMethod("getSize");
            java.lang.reflect.Method getSort = appSpec.getClass().getMethod("getSort");
            java.lang.reflect.Method getCategoryId = appSpec.getClass().getMethod("getCategoryId");
            java.lang.reflect.Method getBrand = appSpec.getClass().getMethod("getBrand");
            java.lang.reflect.Method getPublicationStatus = appSpec.getClass().getMethod("getPublicationStatus");
            java.lang.reflect.Method getSearch = appSpec.getClass().getMethod("getSearch");
            java.lang.reflect.Method getIsActive = appSpec.getClass().getMethod("getIsActive");
            
            Object pubStatusObj = getPublicationStatus.invoke(appSpec);
            
            return DomainProductSpecification.builder()
                .page((Integer) getPage.invoke(appSpec))
                .size((Integer) getSize.invoke(appSpec))
                .sort((String) getSort.invoke(appSpec))
                .categoryId((UUID) getCategoryId.invoke(appSpec))
                .brand((String) getBrand.invoke(appSpec))
                .publicationStatus(pubStatusObj != null ? 
                    DomainPublicationStatus.valueOf(pubStatusObj.toString()) : null)
                .search((String) getSearch.invoke(appSpec))
                .isActive((Boolean) getIsActive.invoke(appSpec))
                .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create domain specification from application specification", e);
        }
    }
    
    /**
     * Creates a specification for active products only
     */
    public static DomainProductSpecification activeProducts() {
        return DomainProductSpecification.builder()
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for published products only
     */
    public static DomainProductSpecification publishedProducts() {
        return DomainProductSpecification.builder()
            .publicationStatus(DomainPublicationStatus.PUBLISHED)
            .isActive(true)
            .build();
    }
    
    /**
     * Creates a specification for products in a specific category
     */
    public static DomainProductSpecification inCategory(UUID categoryId) {
        return DomainProductSpecification.builder()
            .categoryId(categoryId)
            .build();
    }
    
    /**
     * Creates a specification for products with a specific brand
     */
    public static DomainProductSpecification byBrand(String brand) {
        return DomainProductSpecification.builder()
            .brand(brand)
            .build();
    }
}