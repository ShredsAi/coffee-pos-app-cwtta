package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainValidationException;
import java.util.Objects;

/**
 * Value object representing a physical address.
 * Provides validation and ensures addresses follow business rules.
 * Immutable and thread-safe.
 */
public class DomainAddressValue {
    private static final int MAX_STREET_LENGTH = 150;
    private static final int MAX_CITY_LENGTH = 100;
    private static final int MAX_STATE_LENGTH = 100;
    private static final int MAX_POSTAL_CODE_LENGTH = 20;
    private static final int MAX_COUNTRY_LENGTH = 100;
    
    private final String street;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;

    /**
     * Creates a new DomainAddressValue with the specified address components.
     *
     * @param street the street address (required)
     * @param city the city name (required)
     * @param state the state or province (optional)
     * @param postalCode the postal or ZIP code (required)
     * @param country the country name (required)
     * @throws DomainValidationException if any required field is invalid
     */
    public DomainAddressValue(String street, String city, String state, String postalCode, String country) {
        validateConstructorParameters(street, city, state, postalCode, country);
        
        this.street = street.trim();
        this.city = city.trim();
        this.state = state != null ? state.trim() : null;
        this.postalCode = postalCode.trim();
        this.country = country.trim();
    }
    
    private void validateConstructorParameters(String street, String city, String state, String postalCode, String country) {
        if (street == null || street.trim().isEmpty()) {
            throw new DomainValidationException("Street address is required", "street", street);
        }
        
        if (street.trim().length() > MAX_STREET_LENGTH) {
            throw new DomainValidationException(
                String.format("Street address cannot exceed %d characters", MAX_STREET_LENGTH),
                "street",
                street
            );
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new DomainValidationException("City is required", "city", city);
        }
        
        if (city.trim().length() > MAX_CITY_LENGTH) {
            throw new DomainValidationException(
                String.format("City cannot exceed %d characters", MAX_CITY_LENGTH),
                "city",
                city
            );
        }
        
        if (state != null && state.trim().length() > MAX_STATE_LENGTH) {
            throw new DomainValidationException(
                String.format("State cannot exceed %d characters", MAX_STATE_LENGTH),
                "state",
                state
            );
        }
        
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new DomainValidationException("Postal code is required", "postalCode", postalCode);
        }
        
        if (postalCode.trim().length() > MAX_POSTAL_CODE_LENGTH) {
            throw new DomainValidationException(
                String.format("Postal code cannot exceed %d characters", MAX_POSTAL_CODE_LENGTH),
                "postalCode",
                postalCode
            );
        }
        
        if (country == null || country.trim().isEmpty()) {
            throw new DomainValidationException("Country is required", "country", country);
        }
        
        if (country.trim().length() > MAX_COUNTRY_LENGTH) {
            throw new DomainValidationException(
                String.format("Country cannot exceed %d characters", MAX_COUNTRY_LENGTH),
                "country",
                country
            );
        }
    }

    /**
     * Performs additional business validation on the address.
     * Can be extended to include checks against address validation services.
     */
    public void validate() {
        // Additional validation rules could be implemented here
        // For example, validate country codes against ISO 3166-1 alpha-2
        // or postal code format based on country
        
        // Basic validation for postal codes based on country
        // This is a simplified example - real implementation would use proper validation
        if ("US".equalsIgnoreCase(country) || "USA".equalsIgnoreCase(country)) {
            // US ZIP code validation (simplified)
            if (!postalCode.matches("\\d{5}(-\\d{4})?")) {
                throw new DomainValidationException(
                    "Invalid US postal code format",
                    "postalCode",
                    postalCode
                );
            }
        }
    }
    
    /**
     * Creates a full address string representation.
     *
     * @return formatted address string
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        sb.append(", ").append(city);
        if (state != null && !state.isEmpty()) {
            sb.append(", ").append(state);
        }
        sb.append(" ").append(postalCode);
        sb.append(", ").append(country);
        return sb.toString();
    }
    
    // Getters
    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainAddressValue that = (DomainAddressValue) o;
        return Objects.equals(street, that.street) &&
               Objects.equals(city, that.city) &&
               Objects.equals(state, that.state) &&
               Objects.equals(postalCode, that.postalCode) &&
               Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, country);
    }
    
    @Override
    public String toString() {
        return getFullAddress();
    }
}