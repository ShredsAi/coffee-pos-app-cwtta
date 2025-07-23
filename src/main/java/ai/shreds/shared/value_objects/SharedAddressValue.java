package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Value object representing a postal address.
 * Provides validation for address fields and business equality methods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedAddressValue {

    @NotBlank(message = "Street must not be blank")
    @Size(max = 200, message = "Street must be at most 200 characters")
    private String street;

    @NotBlank(message = "City must not be blank")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "PostalCode must not be blank")
    @Size(max = 20, message = "PostalCode must be at most 20 characters")
    private String postalCode;

    @NotBlank(message = "Country must not be blank")
    @Pattern(regexp = "^([A-Z]{2}|[A-Z]{3})$", message = "Country must be a valid ISO 3166-1 alpha-2 or alpha-3 code")
    private String country;

    /**
     * Validates the address values for business rules.
     * This method can be used to validate rules that go beyond simple field validation.
     * 
     * @throws IllegalArgumentException if the address is invalid
     */
    public void validate() {
        // Add additional validation if needed beyond field-level constraints
        // For example, validating postal code format for specific countries
        
        // If street contains only numbers, it might be invalid
        if (street != null && street.matches("^\\d+$")) {
            throw new IllegalArgumentException("Street should not contain only numbers");
        }
    }

    /**
     * Custom equals implementation to compare address values based on business rules.
     * Two addresses are equal if all fields match (case-insensitive for text fields).
     *
     * @param o The object to compare with
     * @return true if addresses are equal based on business rules
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SharedAddressValue that = (SharedAddressValue) o;
        
        return equalsIgnoreCase(street, that.street) &&
               equalsIgnoreCase(city, that.city) &&
               equalsIgnoreCase(state, that.state) &&
               equalsIgnoreCase(postalCode, that.postalCode) &&
               equalsIgnoreCase(country, that.country);
    }

    /**
     * Custom hashCode implementation to match the custom equals method.
     *
     * @return hash code based on the address fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            street != null ? street.toLowerCase() : null,
            city != null ? city.toLowerCase() : null,
            state != null ? state.toLowerCase() : null,
            postalCode != null ? postalCode.toLowerCase() : null,
            country != null ? country.toUpperCase() : null
        );
    }
    
    /**
     * Helper method for case-insensitive string comparison.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return true if strings are equal ignoring case
     */
    private boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equalsIgnoreCase(s2);
    }
}