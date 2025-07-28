package ai.shreds.domain.value_objects;

import java.util.List;

/**
 * Domain Page Interface
 * Represents a page of data with pagination information
 * Domain-specific alternative to Spring's Page to maintain framework independence
 */
public interface DomainPage<T> {
    
    /**
     * Gets the content of the page
     * @return List of content items
     */
    List<T> getContent();
    
    /**
     * Gets the current page number (0-based)
     * @return Page number
     */
    int getNumber();
    
    /**
     * Gets the size of the page
     * @return Page size
     */
    int getSize();
    
    /**
     * Gets the total number of elements across all pages
     * @return Total elements
     */
    long getTotalElements();
    
    /**
     * Gets the total number of pages
     * @return Total pages
     */
    int getTotalPages();
    
    /**
     * Checks if there is a next page
     * @return true if there is a next page
     */
    boolean hasNext();
    
    /**
     * Checks if there is a previous page
     * @return true if there is a previous page
     */
    boolean hasPrevious();
    
    /**
     * Checks if this is the first page
     * @return true if this is the first page
     */
    boolean isFirst();
    
    /**
     * Checks if this is the last page
     * @return true if this is the last page
     */
    boolean isLast();
    
    /**
     * Implementation of DomainPage
     */
    static <T> DomainPage<T> of(List<T> content, int page, int size, long totalElements) {
        return new DomainPageImpl<>(content, page, size, totalElements);
    }
    
    /**
     * Creates an empty page
     */
    static <T> DomainPage<T> empty() {
        return new DomainPageImpl<>(List.of(), 0, 0, 0);
    }
}

/**
 * Default implementation of DomainPage
 */
class DomainPageImpl<T> implements DomainPage<T> {
    
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    
    public DomainPageImpl(List<T> content, int page, int size, long totalElements) {
        this.content = content != null ? content : List.of();
        this.page = Math.max(0, page);
        this.size = Math.max(0, size);
        this.totalElements = Math.max(0, totalElements);
    }
    
    @Override
    public List<T> getContent() {
        return content;
    }
    
    @Override
    public int getNumber() {
        return page;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public long getTotalElements() {
        return totalElements;
    }
    
    @Override
    public int getTotalPages() {
        if (size == 0) {
            return totalElements > 0 ? 1 : 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
    
    @Override
    public boolean hasNext() {
        return page + 1 < getTotalPages();
    }
    
    @Override
    public boolean hasPrevious() {
        return page > 0;
    }
    
    @Override
    public boolean isFirst() {
        return page == 0;
    }
    
    @Override
    public boolean isLast() {
        return page == getTotalPages() - 1 || getTotalPages() == 0;
    }
    
    @Override
    public String toString() {
        return String.format("Page %d of %d containing %d elements (total: %d)", 
            page + 1, getTotalPages(), content.size(), totalElements);
    }
}