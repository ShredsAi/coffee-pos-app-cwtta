package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Value object representing a Product ID.
 * Encapsulates product identifier validation and conversion logic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedProductIdValue {

    @NotBlank(message = "ProductId must not be blank")
    @Pattern(
        regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
        message = "ProductId must be a valid UUID"
    )
    private String value;
    
    /**
     * Constructor that accepts a UUID and converts it to string format.
     *
     * @param uuid The UUID to use as product ID
     */
    public SharedProductIdValue(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Product UUID must not be null");
        }
        this.value = uuid.toString();
    }
    
    /**
     * Validates the product ID format.
     * This method can be called to explicitly validate the ID when annotation-based
     * validation is not available.
     * 
     * @throws IllegalArgumentException if the product ID is invalid
     */
    public void validate() {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductId must not be blank");
        }
        
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ProductId must be a valid UUID", e);
        }
    }
    
    /**
     * Converts the product ID to UUID format.
     *
     * @return The product ID as a UUID
     * @throws IllegalArgumentException if the product ID is not a valid UUID
     */
    public UUID toUUID() {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot convert to UUID: " + value, e);
        }
    }
    
    /**
     * Creates a product ID value from a UUID.
     *
     * @param uuid The UUID to convert to a product ID
     * @return A new product ID value object
     */
    public static SharedProductIdValue fromUUID(UUID uuid) {
        return new SharedProductIdValue(uuid);
    }
    
    /**
     * Creates a product ID value from a string representation of a UUID.
     *
     * @param uuidString The UUID string to convert to a product ID
     * @return A new product ID value object
     * @throws IllegalArgumentException if the input is not a valid UUID string
     */
    public static SharedProductIdValue fromString(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            return new SharedProductIdValue(uuid.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuidString, e);
        }
    }
}