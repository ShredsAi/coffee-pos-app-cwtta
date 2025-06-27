package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.shared.value_objects.SharedMoneyValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import java.util.Set;

/**
 * Value object representing a monetary amount with currency.
 * Provides arithmetic operations and validation for financial calculations.
 * Immutable and thread-safe.
 */
public class DomainMoneyValue {
    private static final int SCALE = 4; // 4 decimal places for monetary precision
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NOK"
    );
    
    private final BigDecimal amount;
    private final String currency;

    /**
     * Creates a new DomainMoneyValue with the specified amount and currency.
     *
     * @param amount the monetary amount (must not be null)
     * @param currency the currency code (must be a valid ISO 4217 code)
     * @throws DomainValidationException if amount or currency is invalid
     */
    public DomainMoneyValue(BigDecimal amount, String currency) {
        validateConstructorParameters(amount, currency);
        
        this.amount = amount.setScale(SCALE, DEFAULT_ROUNDING);
        this.currency = currency.toUpperCase().trim();
    }

    /**
     * Creates a new DomainMoneyValue from a double amount.
     *
     * @param amount the monetary amount
     * @param currency the currency code
     */
    public DomainMoneyValue(double amount, String currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates a new DomainMoneyValue from an integer amount.
     *
     * @param amount the monetary amount
     * @param currency the currency code
     */
    public DomainMoneyValue(int amount, String currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    private void validateConstructorParameters(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new DomainValidationException("Amount cannot be null", "amount", null);
        }
        
        if (currency == null || currency.trim().isEmpty()) {
            throw new DomainValidationException("Currency cannot be null or empty", "currency", currency);
        }
        
        String trimmedCurrency = currency.trim().toUpperCase();
        if (trimmedCurrency.length() != 3) {
            throw new DomainValidationException(
                "Currency must be a 3-character ISO 4217 code", 
                "currency", 
                currency
            );
        }
        
        // Validate that it's a supported currency
        if (!SUPPORTED_CURRENCIES.contains(trimmedCurrency)) {
            // Try to validate against Java Currency class
            try {
                Currency.getInstance(trimmedCurrency);
            } catch (IllegalArgumentException e) {
                throw new DomainValidationException(
                    "Invalid currency code: " + currency, 
                    "currency", 
                    currency, 
                    e
                );
            }
        }
        
        // Validate amount is within reasonable bounds
        if (amount.compareTo(BigDecimal.valueOf(-999999999.9999)) < 0 || 
            amount.compareTo(BigDecimal.valueOf(999999999.9999)) > 0) {
            throw new DomainValidationException(
                "Amount is outside acceptable range", 
                "amount", 
                amount
            );
        }
    }

    /**
     * Adds another money value to this money value.
     * Both amounts must have the same currency.
     *
     * @param other the money value to add
     * @return a new DomainMoneyValue representing the sum
     * @throws DomainValidationException if currencies don't match
     */
    public DomainMoneyValue add(DomainMoneyValue other) {
        validateSameCurrency(other, "add");
        return new DomainMoneyValue(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another money value from this money value.
     * Both amounts must have the same currency.
     *
     * @param other the money value to subtract
     * @return a new DomainMoneyValue representing the difference
     * @throws DomainValidationException if currencies don't match
     */
    public DomainMoneyValue subtract(DomainMoneyValue other) {
        validateSameCurrency(other, "subtract");
        return new DomainMoneyValue(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies this money value by a factor.
     *
     * @param factor the multiplication factor
     * @return a new DomainMoneyValue representing the product
     */
    public DomainMoneyValue multiply(BigDecimal factor) {
        if (factor == null) {
            throw new DomainValidationException("Multiplication factor cannot be null", "factor", null);
        }
        return new DomainMoneyValue(this.amount.multiply(factor), this.currency);
    }

    /**
     * Divides this money value by a divisor.
     *
     * @param divisor the division divisor (must not be zero)
     * @return a new DomainMoneyValue representing the quotient
     * @throws DomainValidationException if divisor is zero
     */
    public DomainMoneyValue divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new DomainValidationException("Division divisor cannot be null", "divisor", null);
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainValidationException("Cannot divide by zero", "divisor", divisor);
        }
        return new DomainMoneyValue(this.amount.divide(divisor, SCALE, DEFAULT_ROUNDING), this.currency);
    }

    /**
     * Checks if this money value is greater than another money value.
     *
     * @param other the money value to compare against
     * @return true if this amount is greater
     * @throws DomainValidationException if currencies don't match
     */
    public boolean isGreaterThan(DomainMoneyValue other) {
        validateSameCurrency(other, "compare");
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this money value is less than another money value.
     *
     * @param other the money value to compare against
     * @return true if this amount is less
     * @throws DomainValidationException if currencies don't match
     */
    public boolean isLessThan(DomainMoneyValue other) {
        validateSameCurrency(other, "compare");
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if this money value is zero.
     *
     * @return true if the amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this money value is negative.
     *
     * @return true if the amount is less than zero
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Checks if this money value is positive.
     *
     * @return true if the amount is greater than zero
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns the absolute value of this money value.
     *
     * @return a new DomainMoneyValue with the absolute amount
     */
    public DomainMoneyValue abs() {
        return new DomainMoneyValue(this.amount.abs(), this.currency);
    }

    /**
     * Creates a zero money value with the same currency as this money value.
     *
     * @return a new DomainMoneyValue with zero amount
     */
    public DomainMoneyValue zero() {
        return new DomainMoneyValue(BigDecimal.ZERO, this.currency);
    }

    private void validateSameCurrency(DomainMoneyValue other, String operation) {
        if (other == null) {
            throw new DomainValidationException(
                String.format("Cannot %s with null money value", operation), 
                "other", 
                null
            );
        }
        
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException(
                String.format("Cannot %s money values with different currencies: %s and %s", 
                    operation, this.currency, other.currency),
                "currency",
                other.currency
            );
        }
    }

    /**
     * Converts this domain money value to a shared money value.
     *
     * @return a SharedMoneyValue representation
     */
    public SharedMoneyValue toSharedMoney() {
        return new SharedMoneyValue(this.amount, this.currency);
    }

    // Getters
    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainMoneyValue that = (DomainMoneyValue) o;
        return Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency;
    }
}