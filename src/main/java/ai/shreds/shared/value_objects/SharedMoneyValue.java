package ai.shreds.shared.value_objects;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing monetary value with an amount and currency.
 * Provides operations for money arithmetic while maintaining currency integrity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedMoneyValue {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal amount;

    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;
    
    /**
     * Adds another money value to this one.
     * Both values must have the same currency.
     *
     * @param other The money value to add
     * @return A new money value with the sum amount and same currency
     * @throws IllegalArgumentException if currencies don't match
     */
    public SharedMoneyValue add(SharedMoneyValue other) {
        if (other == null) {
            return this;
        }
        
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money values with different currencies: " + 
                                             this.currency + " and " + other.currency);
        }
        return new SharedMoneyValue(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Multiplies this money value by a factor.
     *
     * @param factor The multiplication factor
     * @return A new money value with the multiplied amount and same currency
     */
    public SharedMoneyValue multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Multiplication factor cannot be null");
        }
        return new SharedMoneyValue(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Checks if this money value is greater than another.
     * Both values must have the same currency.
     *
     * @param other The money value to compare with
     * @return true if this amount is greater than the other amount
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThan(SharedMoneyValue other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null value");
        }
        
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money values with different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this money value is zero.
     *
     * @return true if the amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}