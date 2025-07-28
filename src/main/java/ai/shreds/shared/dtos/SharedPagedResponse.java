package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedPagedResponse<T> {
    
    private List<T> content;
    
    private Integer page;
    
    private Integer size;
    
    private Long totalElements;
    
    private Integer totalPages;
    
    private Boolean hasNext;
    
    private Boolean hasPrevious;
    
    public static <T> SharedPagedResponse<T> of(List<T> content, Integer page, Integer size, Long totalElements) {
        Integer calculatedTotalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        Boolean calculatedHasNext = page < calculatedTotalPages - 1;
        Boolean calculatedHasPrevious = page > 0;
        
        return SharedPagedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(calculatedTotalPages)
                .hasNext(calculatedHasNext)
                .hasPrevious(calculatedHasPrevious)
                .build();
    }
}