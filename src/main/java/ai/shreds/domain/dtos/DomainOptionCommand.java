package ai.shreds.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Option Command
 * Command object representing an attribute option for create operations
 * Used within domain commands that include options
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainOptionCommand {
    
    private String value;
    private String code;
    private Integer sortOrder;
    
    /**
     * Validates the option data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Option value cannot be null or empty");
        }
        
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }
}