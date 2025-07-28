package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedAttributeFilterParams {
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    private String sort;
    
    private SharedAttributeType attributeType;
    
    private Boolean isFilterable;
    
    private Boolean isSearchable;
    
    private Boolean isRequired;
    
    private Boolean isActive;
    
    private String search;
    
    // TODO: Implementation of toSpecification() method will be added
    // after application layer is available for proper transformation
    public Object toSpecification() {
        throw new UnsupportedOperationException("toSpecification() method will be implemented when application layer is available");
    }
}