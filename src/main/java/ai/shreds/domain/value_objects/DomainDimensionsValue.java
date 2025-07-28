package ai.shreds.domain.value_objects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import ai.shreds.domain.enums.DomainDimensionUnit;
import ai.shreds.shared.value_objects.SharedDimensions;

/**
 * Immutable value object representing physical dimensions.
 * Handles dimension values with units and conversions between different units.
 */
public final class DomainDimensionsValue {
    private final BigDecimal length;
    private final BigDecimal width;
    private final BigDecimal height;
    private final DomainDimensionUnit unit;
    
    private static final int DECIMAL_PLACES = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Creates a new dimensions value object
     * 
     * @param length The length dimension
     * @param width The width dimension
     * @param height The height dimension
     * @param unit The unit of measurement
     * @throws IllegalArgumentException if any dimension is null, zero or negative, or if unit is null
     */
    public DomainDimensionsValue(BigDecimal length,
                                 BigDecimal width,
                                 BigDecimal height,
                                 DomainDimensionUnit unit) {
        if (length == null || width == null || height == null || unit == null) {
            throw new IllegalArgumentException("Dimensions and unit must not be null");
        }
        if (length.signum() <= 0 || width.signum() <= 0 || height.signum() <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        this.length = length.setScale(DECIMAL_PLACES, ROUNDING_MODE);
        this.width = width.setScale(DECIMAL_PLACES, ROUNDING_MODE);
        this.height = height.setScale(DECIMAL_PLACES, ROUNDING_MODE);
        this.unit = unit;
    }

    /**
     * Calculates the volume of the dimensions
     * 
     * @return The volume (length × width × height)
     */
    public BigDecimal calculateVolume() {
        return length.multiply(width).multiply(height).setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }

    /**
     * Converts dimensions to a different unit
     * 
     * @param targetUnit The target unit to convert to
     * @return A new DomainDimensionsValue with the same physical dimensions but in the target unit
     * @throws IllegalArgumentException if targetUnit is null
     */
    public DomainDimensionsValue convertTo(DomainDimensionUnit targetUnit) {
        if (targetUnit == null) {
            throw new IllegalArgumentException("Target unit must not be null");
        }
        
        if (this.unit == targetUnit) {
            return this; // No conversion needed
        }

        BigDecimal lengthInMeters = unit.toMeters(length);
        BigDecimal widthInMeters = unit.toMeters(width);
        BigDecimal heightInMeters = unit.toMeters(height);

        BigDecimal newLength = targetUnit.fromMeters(lengthInMeters);
        BigDecimal newWidth = targetUnit.fromMeters(widthInMeters);
        BigDecimal newHeight = targetUnit.fromMeters(heightInMeters);

        return new DomainDimensionsValue(newLength, newWidth, newHeight, targetUnit);
    }
    
    /**
     * Validates that dimensions are within reasonable limits
     * 
     * @throws IllegalArgumentException if dimensions exceed reasonable limits
     */
    public void validate() {
        // Convert to standard unit (meters) for validation
        DomainDimensionsValue inMeters = this;
        if (unit != DomainDimensionUnit.METER) {
            inMeters = this.convertTo(DomainDimensionUnit.METER);
        }
        
        // Set reasonable maximum limits (e.g., 100 meters for each dimension for products)
        BigDecimal maxDimension = new BigDecimal("100");
        
        if (inMeters.length.compareTo(maxDimension) > 0) {
            throw new IllegalArgumentException("Length exceeds maximum allowed dimensions");
        }
        if (inMeters.width.compareTo(maxDimension) > 0) {
            throw new IllegalArgumentException("Width exceeds maximum allowed dimensions");
        }
        if (inMeters.height.compareTo(maxDimension) > 0) {
            throw new IllegalArgumentException("Height exceeds maximum allowed dimensions");
        }
    }
    
    /**
     * Calculates the longest dimension
     * 
     * @return The longest dimension value
     */
    public BigDecimal getLongestDimension() {
        BigDecimal longest = length;
        if (width.compareTo(longest) > 0) {
            longest = width;
        }
        if (height.compareTo(longest) > 0) {
            longest = height;
        }
        return longest;
    }
    
    /**
     * Calculates the shortest dimension
     * 
     * @return The shortest dimension value
     */
    public BigDecimal getShortestDimension() {
        BigDecimal shortest = length;
        if (width.compareTo(shortest) < 0) {
            shortest = width;
        }
        if (height.compareTo(shortest) < 0) {
            shortest = height;
        }
        return shortest;
    }

    /**
     * Converts to shared dimensions value object
     * 
     * @return The equivalent SharedDimensions value object
     */
    public SharedDimensions toSharedValue() {
        return SharedDimensions.fromDomainValue(this);
    }

    /**
     * Creates a domain dimensions value from shared dimensions
     * 
     * @param shared The shared dimensions value
     * @return A new domain dimensions value
     * @throws IllegalArgumentException if shared is null
     */
    public static DomainDimensionsValue fromSharedValue(SharedDimensions shared) {
        if (shared == null) {
            throw new IllegalArgumentException("SharedDimensions must not be null");
        }
        return new DomainDimensionsValue(
            shared.getLength(),
            shared.getWidth(),
            shared.getHeight(),
            DomainDimensionUnit.valueOf(shared.getUnit().name())
        );
    }
    
    /**
     * Factory method to create dimensions in millimeters
     * 
     * @param length Length in millimeters
     * @param width Width in millimeters
     * @param height Height in millimeters
     * @return A new domain dimensions value in millimeters
     */
    public static DomainDimensionsValue ofMillimeters(BigDecimal length, BigDecimal width, BigDecimal height) {
        return new DomainDimensionsValue(length, width, height, DomainDimensionUnit.MILLIMETER);
    }
    
    /**
     * Factory method to create dimensions in centimeters
     * 
     * @param length Length in centimeters
     * @param width Width in centimeters
     * @param height Height in centimeters
     * @return A new domain dimensions value in centimeters
     */
    public static DomainDimensionsValue ofCentimeters(BigDecimal length, BigDecimal width, BigDecimal height) {
        return new DomainDimensionsValue(length, width, height, DomainDimensionUnit.CENTIMETER);
    }
    
    /**
     * Factory method to create dimensions in meters
     * 
     * @param length Length in meters
     * @param width Width in meters
     * @param height Height in meters
     * @return A new domain dimensions value in meters
     */
    public static DomainDimensionsValue ofMeters(BigDecimal length, BigDecimal width, BigDecimal height) {
        return new DomainDimensionsValue(length, width, height, DomainDimensionUnit.METER);
    }
    
    /**
     * Factory method to create dimensions in inches
     * 
     * @param length Length in inches
     * @param width Width in inches
     * @param height Height in inches
     * @return A new domain dimensions value in inches
     */
    public static DomainDimensionsValue ofInches(BigDecimal length, BigDecimal width, BigDecimal height) {
        return new DomainDimensionsValue(length, width, height, DomainDimensionUnit.INCH);
    }

    /**
     * Gets the length dimension
     * 
     * @return The length
     */
    public BigDecimal getLength() {
        return length;
    }

    /**
     * Gets the width dimension
     * 
     * @return The width
     */
    public BigDecimal getWidth() {
        return width;
    }

    /**
     * Gets the height dimension
     * 
     * @return The height
     */
    public BigDecimal getHeight() {
        return height;
    }

    /**
     * Gets the unit of measurement
     * 
     * @return The unit
     */
    public DomainDimensionUnit getUnit() {
        return unit;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        // Convert both to a standard unit (meters) before comparison
        DomainDimensionsValue that = (DomainDimensionsValue) o;
        DomainDimensionsValue thisInMeters = this.unit == DomainDimensionUnit.METER ? 
            this : this.convertTo(DomainDimensionUnit.METER);
        DomainDimensionsValue thatInMeters = that.unit == DomainDimensionUnit.METER ? 
            that : that.convertTo(DomainDimensionUnit.METER);
            
        return thisInMeters.length.equals(thatInMeters.length) &&
               thisInMeters.width.equals(thatInMeters.width) &&
               thisInMeters.height.equals(thatInMeters.height);
    }
    
    @Override
    public int hashCode() {
        // Convert to meters for consistent hash code regardless of unit
        DomainDimensionsValue inMeters = this.unit == DomainDimensionUnit.METER ? 
            this : this.convertTo(DomainDimensionUnit.METER);
            
        return Objects.hash(inMeters.length, inMeters.width, inMeters.height);
    }
    
    @Override
    public String toString() {
        return String.format("Dimensions(%.2f × %.2f × %.2f %s)", 
                length, width, height, unit.toString());
    }
}