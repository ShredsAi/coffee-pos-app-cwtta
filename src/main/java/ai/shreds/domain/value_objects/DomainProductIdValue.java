package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainValidationException;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Value object representing a product identifier.
 * Provides validation and ensures product IDs follow business rules.
 * Immutable and thread-safe.
 */
public class DomainProductIdValue {
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    
    private final String value;

    /**
     * Creates a new DomainProductIdValue with the specified string value.
     *
     * @param value the product ID as a string (must be a valid UUID format)
     * @throws DomainValidationException if value is invalid
     */
    public DomainProductIdValue(String value) {
        validateValue(value);
        this.value = value.toLowerCase().trim();
    }

    /**
     * Creates a new DomainProductIdValue from a UUID.
     *
     * @param productId the product ID as a UUID
     * @throws DomainValidationException if productId is null
     */
    public DomainProductIdValue(UUID productId) {
        if (productId == null) {
            throw new DomainValidationException("Product ID UUID cannot be null", "productId", null);
        }
        this.value = productId.toString().toLowerCase();
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException(
                "Product ID cannot be null or empty", 
                "value", 
                value
            );
        }

        String trimmedValue = value.trim();
        
        // Check if it's a valid UUID format
        if (!PRODUCT_ID_PATTERN.matcher(trimmedValue).matches()) {
            throw new DomainValidationException(
                "Product ID must be a valid UUID format", 
                "value", 
                value
            );
        }

        // Additional validation - try to parse as UUID to ensure it's valid
        try {
            UUID.fromString(trimmedValue);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException(
                "Product ID is not a valid UUID: " + e.getMessage(), 
                "value", 
                value, 
                e
            );
        }
    }

    /**
     * Performs additional business validation on the product ID.
     * Can be extended to include checks against external product catalog.
     */
    public void validate() {
        // Base validation is already done in constructor
        // This method can be extended for additional business rules
        // such as checking against product catalog service
    }

    /**
     * Converts this product ID to a UUID object.
     *
     * @return UUID representation of the product ID
     */
    public UUID toUUID() {
        return UUID.fromString(value);
    }

    /**
     * Checks if this product ID represents a valid product.
     * This is a placeholder for future integration with product catalog.
     *
     * @return true if the product ID is considered valid
     */
    public boolean isValidProduct() {
        // For now, just check that it's a properly formatted UUID
        // In the future, this could check against a product catalog service
        return value != null && !value.isEmpty();
    }

    /**
     * Creates a random product ID for testing or demo purposes.
     *
     * @return a new DomainProductIdValue with a random UUID
     */
    public static DomainProductIdValue random() {
        return new DomainProductIdValue(UUID.randomUUID());
    }

    /**
     * Creates a DomainProductIdValue from a string, with null safety.
     *
     * @param value the string value
     * @return DomainProductIdValue or null if input is null/empty
     */
    public static DomainProductIdValue fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return new DomainProductIdValue(value);
    }

    // Getter
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainProductIdValue that = (DomainProductIdValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}