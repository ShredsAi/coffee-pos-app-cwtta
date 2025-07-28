package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedDimensionUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedDimensions {
    
    private BigDecimal length;
    
    private BigDecimal width;
    
    private BigDecimal height;
    
    private SharedDimensionUnit unit;
    
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        return length.multiply(width).multiply(height);
    }
    
    // TODO: Implementation of toDomainValue() and fromDomainValue() methods will be added
    // after domain layer is available for proper transformation
    public Object toDomainValue() {
        throw new UnsupportedOperationException("toDomainValue() method will be implemented when domain layer is available");
    }
    
    public static SharedDimensions fromDomainValue(Object value) {
        throw new UnsupportedOperationException("fromDomainValue() method will be implemented when domain layer is available");
    }
}