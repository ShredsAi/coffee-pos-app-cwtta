package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.shared.value_objects.SharedQuantityValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a quantity with its unit of measure.
 * Provides arithmetic operations and validation for inventory quantities.
 * Immutable and thread-safe.
 */
public class DomainQuantityValue {
    private static final int SCALE = 6; // 6 decimal places for precision
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    
    private final BigDecimal value;
    private final String unit;

    /**
     * Creates a new DomainQuantityValue with the specified value and unit.
     *
     * @param value the numeric value (must not be null)
     * @param unit the unit of measure (must not be null or empty)
     * @throws DomainValidationException if value or unit is invalid
     */
    public DomainQuantityValue(BigDecimal value, String unit) {
        validateConstructorParameters(value, unit);
        
        this.value = value.setScale(SCALE, DEFAULT_ROUNDING);
        this.unit = unit.trim().toLowerCase();
    }

    /**
     * Creates a new DomainQuantityValue from a double value.
     *
     * @param value the numeric value
     * @param unit the unit of measure
     */
    public DomainQuantityValue(double value, String unit) {
        this(BigDecimal.valueOf(value), unit);
    }

    /**
     * Creates a new DomainQuantityValue from an integer value.
     *
     * @param value the numeric value
     * @param unit the unit of measure
     */
    public DomainQuantityValue(int value, String unit) {
        this(BigDecimal.valueOf(value), unit);
    }

    private void validateConstructorParameters(BigDecimal value, String unit) {
        if (value == null) {
            throw new DomainValidationException("Quantity value cannot be null", "value", null);
        }
        
        if (unit == null || unit.trim().isEmpty()) {
            throw new DomainValidationException("Unit cannot be null or empty", "unit", unit);
        }
        
        // Additional validation for reasonable limits
        if (value.compareTo(BigDecimal.valueOf(-999999999)) < 0 || 
            value.compareTo(BigDecimal.valueOf(999999999)) > 0) {
            throw new DomainValidationException(
                "Quantity value is outside acceptable range (-999,999,999 to 999,999,999)", 
                "value", 
                value
            );
        }
    }

    /**
     * Adds another quantity to this quantity.
     * Both quantities must have the same unit.
     *
     * @param other the quantity to add
     * @return a new DomainQuantityValue representing the sum
     * @throws DomainValidationException if units don't match
     */
    public DomainQuantityValue add(DomainQuantityValue other) {
        validateSameUnit(other, "add");
        return new DomainQuantityValue(this.value.add(other.value), this.unit);
    }

    /**
     * Subtracts another quantity from this quantity.
     * Both quantities must have the same unit.
     *
     * @param other the quantity to subtract
     * @return a new DomainQuantityValue representing the difference
     * @throws DomainValidationException if units don't match
     */
    public DomainQuantityValue subtract(DomainQuantityValue other) {
        validateSameUnit(other, "subtract");
        return new DomainQuantityValue(this.value.subtract(other.value), this.unit);
    }

    /**
     * Multiplies this quantity by a factor.
     *
     * @param factor the multiplication factor
     * @return a new DomainQuantityValue representing the product
     */
    public DomainQuantityValue multiply(BigDecimal factor) {
        if (factor == null) {
            throw new DomainValidationException("Multiplication factor cannot be null", "factor", null);
        }
        return new DomainQuantityValue(this.value.multiply(factor), this.unit);
    }

    /**
     * Divides this quantity by a divisor.
     *
     * @param divisor the division divisor (must not be zero)
     * @return a new DomainQuantityValue representing the quotient
     * @throws DomainValidationException if divisor is zero
     */
    public DomainQuantityValue divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new DomainValidationException("Division divisor cannot be null", "divisor", null);
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainValidationException("Cannot divide by zero", "divisor", divisor);
        }
        return new DomainQuantityValue(this.value.divide(divisor, SCALE, DEFAULT_ROUNDING), this.unit);
    }

    /**
     * Checks if this quantity is greater than another quantity.
     *
     * @param other the quantity to compare against
     * @return true if this quantity is greater
     * @throws DomainValidationException if units don't match
     */
    public boolean isGreaterThan(DomainQuantityValue other) {
        validateSameUnit(other, "compare");
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * Checks if this quantity is greater than or equal to another quantity.
     *
     * @param other the quantity to compare against
     * @return true if this quantity is greater than or equal
     * @throws DomainValidationException if units don't match
     */
    public boolean isGreaterThanOrEqual(DomainQuantityValue other) {
        validateSameUnit(other, "compare");
        return this.value.compareTo(other.value) >= 0;
    }

    /**
     * Checks if this quantity is less than another quantity.
     *
     * @param other the quantity to compare against
     * @return true if this quantity is less
     * @throws DomainValidationException if units don't match
     */
    public boolean isLessThan(DomainQuantityValue other) {
        validateSameUnit(other, "compare");
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * Checks if this quantity is zero.
     *
     * @return true if the value is zero
     */
    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this quantity is negative.
     *
     * @return true if the value is less than zero
     */
    public boolean isNegative() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Checks if this quantity is positive.
     *
     * @return true if the value is greater than zero
     */
    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns the absolute value of this quantity.
     *
     * @return a new DomainQuantityValue with the absolute value
     */
    public DomainQuantityValue abs() {
        return new DomainQuantityValue(this.value.abs(), this.unit);
    }

    /**
     * Creates a zero quantity with the same unit as this quantity.
     *
     * @return a new DomainQuantityValue with zero value
     */
    public DomainQuantityValue zero() {
        return new DomainQuantityValue(BigDecimal.ZERO, this.unit);
    }

    private void validateSameUnit(DomainQuantityValue other, String operation) {
        if (other == null) {
            throw new DomainValidationException(
                String.format("Cannot %s with null quantity", operation), 
                "other", 
                null
            );
        }
        
        if (!this.unit.equals(other.unit)) {
            throw new DomainValidationException(
                String.format("Cannot %s quantities with different units: %s and %s", 
                    operation, this.unit, other.unit),
                "unit",
                other.unit
            );
        }
    }

    /**
     * Converts this domain quantity to a shared quantity value.
     *
     * @return a SharedQuantityValue representation
     */
    public SharedQuantityValue toSharedQuantity() {
        return new SharedQuantityValue(this.value, this.unit);
    }

    // Getters
    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainQuantityValue that = (DomainQuantityValue) o;
        return Objects.equals(value, that.value) && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        return value.toString() + " " + unit;
    }
}