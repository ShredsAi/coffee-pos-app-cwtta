package ai.shreds.shared.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing inventory threshold information.
 * Contains safety stock level and reorder point thresholds used for inventory management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedInventoryThresholdsDTO {
    
    @NotNull(message = "Safety stock level must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Safety stock level must be non-negative")
    private BigDecimal safetyStockLevel;
    
    @NotNull(message = "Reorder point must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Reorder point must be non-negative")
    private BigDecimal reorderPoint;
    
    /**
     * Checks if the provided quantity is below the safety stock level.
     * This indicates a critical inventory level that requires immediate attention.
     *
     * @param currentQuantity the current inventory quantity to check
     * @return true if the quantity is below safety stock level, false otherwise
     */
    public boolean isBelowSafetyStockLevel(BigDecimal currentQuantity) {
        if (currentQuantity == null || safetyStockLevel == null) {
            return false;
        }
        return currentQuantity.compareTo(safetyStockLevel) < 0;
    }
    
    /**
     * Checks if the provided quantity is below the reorder point.
     * This indicates that a replenishment order should be initiated.
     *
     * @param currentQuantity the current inventory quantity to check
     * @return true if the quantity is below reorder point, false otherwise
     */
    public boolean isBelowReorderPoint(BigDecimal currentQuantity) {
        if (currentQuantity == null || reorderPoint == null) {
            return false;
        }
        return currentQuantity.compareTo(reorderPoint) < 0;
    }
    
    /**
     * Validates that the safety stock level is less than or equal to the reorder point.
     * This ensures the threshold values follow the expected business rule.
     *
     * @return true if thresholds are valid, false otherwise
     */
    public boolean isValid() {
        if (safetyStockLevel == null || reorderPoint == null) {
            return false;
        }
        // Safety stock level should be less than or equal to reorder point
        return safetyStockLevel.compareTo(reorderPoint) <= 0;
    }
}