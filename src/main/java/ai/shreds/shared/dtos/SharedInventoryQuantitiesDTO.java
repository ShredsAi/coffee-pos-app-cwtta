package ai.shreds.shared.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing inventory quantity information.
 * Contains all quantity types: available, reserved, allocated, and total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedInventoryQuantitiesDTO {
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Available quantity must be non-negative")
    private BigDecimal availableQty;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Reserved quantity must be non-negative")
    private BigDecimal reservedQty;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Allocated quantity must be non-negative")
    private BigDecimal allocatedQty;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Total quantity must be non-negative")
    private BigDecimal totalQty;
    
    @NotBlank(message = "Quantity unit must not be blank")
    private String qtyUnit;
    
    /**
     * Validates that the total quantity equals the sum of available, reserved, and allocated quantities.
     * This ensures data consistency across the inventory quantities.
     *
     * @return true if quantities are consistent, false otherwise
     */
    public boolean isQuantityConsistent() {
        if (availableQty == null || reservedQty == null || allocatedQty == null || totalQty == null) {
            return false;
        }
        
        BigDecimal calculatedTotal = availableQty.add(reservedQty).add(allocatedQty);
        return totalQty.compareTo(calculatedTotal) == 0;
    }
    
    /**
     * Gets the committed quantity (reserved + allocated).
     *
     * @return the sum of reserved and allocated quantities
     */
    public BigDecimal getCommittedQty() {
        if (reservedQty == null || allocatedQty == null) {
            return BigDecimal.ZERO;
        }
        return reservedQty.add(allocatedQty);
    }
}