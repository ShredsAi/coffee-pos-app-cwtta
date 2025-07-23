package ai.shreds.shared.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing pagination information for paginated responses.
 * Contains current page information and navigation flags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedPaginationDTO {

    @NotNull(message = "Page number must not be null")
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page;
    
    @NotNull(message = "Page size must not be null")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size;
    
    @NotNull(message = "Total elements must not be null")
    @Min(value = 0, message = "Total elements must be non-negative")
    private Long totalElements;
    
    @NotNull(message = "Total pages must not be null")
    @Min(value = 0, message = "Total pages must be non-negative")
    private Integer totalPages;
    
    @NotNull(message = "Has next flag must not be null")
    private Boolean hasNext;
    
    @NotNull(message = "Has previous flag must not be null")
    private Boolean hasPrevious;
    
    /**
     * Creates a pagination DTO with calculated values.
     *
     * @param page Current page number (0-based)
     * @param size Number of elements per page
     * @param totalElements Total number of elements available
     * @return Pagination DTO with calculated navigation flags and total pages
     */
    public static SharedPaginationDTO create(int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return SharedPaginationDTO.builder()
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(hasNext)
            .hasPrevious(hasPrevious)
            .build();
    }
    
    /**
     * Gets the offset for database queries.
     * 
     * @return The offset calculated as page * size
     */
    public int getOffset() {
        return page * size;
    }
    
    /**
     * Checks if this is the first page.
     *
     * @return true if this is the first page (page = 0)
     */
    public boolean isFirstPage() {
        return page == 0;
    }
    
    /**
     * Checks if this is the last page.
     *
     * @return true if this is the last page
     */
    public boolean isLastPage() {
        return !hasNext;
    }
    
    /**
     * Gets the next page number.
     *
     * @return the next page number, or current page if no next page exists
     */
    public int getNextPage() {
        return hasNext ? page + 1 : page;
    }
    
    /**
     * Gets the previous page number.
     *
     * @return the previous page number, or current page if no previous page exists
     */
    public int getPreviousPage() {
        return hasPrevious ? page - 1 : page;
    }
}