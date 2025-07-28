package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedPublicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedProductFilterParams {
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    private String sort;
    
    private UUID categoryId;
    
    private String brand;
    
    private SharedPublicationStatus publicationStatus;
    
    private String search;
    
    private Boolean isActive;
    
    // TODO: Implementation of toSpecification() method will be added
    // after application layer is available for proper transformation
    public Object toSpecification() {
        throw new UnsupportedOperationException("toSpecification() method will be implemented when application layer is available");
    }
}