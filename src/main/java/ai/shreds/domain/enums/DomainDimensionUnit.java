package ai.shreds.domain.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Dimension Unit Enum
 * Represents units of measurement for dimensions with conversion capabilities
 */
public enum DomainDimensionUnit {
    MILLIMETER(new BigDecimal("0.001")),
    CENTIMETER(new BigDecimal("0.01")), 
    METER(new BigDecimal("1.0")),
    INCH(new BigDecimal("0.0254")),
    FOOT(new BigDecimal("0.3048"));
    
    private final BigDecimal metersPerUnit;
    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Constructor with conversion factor to meters
     * 
     * @param metersPerUnit How many meters equal one unit of this dimension unit
     */
    DomainDimensionUnit(BigDecimal metersPerUnit) {
        this.metersPerUnit = metersPerUnit;
    }
    
    /**
     * Converts a value in this unit to meters
     * 
     * @param value The value in this unit
     * @return The equivalent value in meters
     */
    public BigDecimal toMeters(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return value.multiply(metersPerUnit).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Converts a value from meters to this unit
     * 
     * @param meters The value in meters
     * @return The equivalent value in this unit
     */
    public BigDecimal fromMeters(BigDecimal meters) {
        if (meters == null) {
            throw new IllegalArgumentException("Meters value cannot be null");
        }
        return meters.divide(metersPerUnit, SCALE, ROUNDING_MODE);
    }
    
    /**
     * Converts a value from this unit to another unit
     * 
     * @param value The value in this unit
     * @param targetUnit The target unit to convert to
     * @return The equivalent value in the target unit
     */
    public BigDecimal convertTo(BigDecimal value, DomainDimensionUnit targetUnit) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (targetUnit == null) {
            throw new IllegalArgumentException("Target unit cannot be null");
        }
        
        if (this == targetUnit) {
            return value; // No conversion needed
        }
        
        // Convert to meters first, then to target unit
        BigDecimal inMeters = toMeters(value);
        return targetUnit.fromMeters(inMeters);
    }
    
    /**
     * Gets the conversion factor to meters
     * 
     * @return The number of meters per unit of this dimension unit
     */
    public BigDecimal getMetersPerUnit() {
        return metersPerUnit;
    }
    
    /**
     * Gets the display symbol for this unit
     * 
     * @return The symbol string
     */
    public String getSymbol() {
        switch (this) {
            case MILLIMETER:
                return "mm";
            case CENTIMETER:
                return "cm";
            case METER:
                return "m";
            case INCH:
                return "in";
            case FOOT:
                return "ft";
            default:
                return name().toLowerCase();
        }
    }
    
    /**
     * Gets the display name for this unit
     * 
     * @return The human-readable name
     */
    public String getDisplayName() {
        switch (this) {
            case MILLIMETER:
                return "Millimeter";
            case CENTIMETER:
                return "Centimeter";
            case METER:
                return "Meter";
            case INCH:
                return "Inch";
            case FOOT:
                return "Foot";
            default:
                return name();
        }
    }
    
    /**
     * Gets the plural display name for this unit
     * 
     * @return The plural form of the human-readable name
     */
    public String getPluralDisplayName() {
        switch (this) {
            case MILLIMETER:
                return "Millimeters";
            case CENTIMETER:
                return "Centimeters";
            case METER:
                return "Meters";
            case INCH:
                return "Inches";
            case FOOT:
                return "Feet";
            default:
                return name() + "S";
        }
    }
    
    /**
     * Checks if this unit is metric
     * 
     * @return true if this is a metric unit, false for imperial
     */
    public boolean isMetric() {
        return this == MILLIMETER || this == CENTIMETER || this == METER;
    }
    
    /**
     * Checks if this unit is imperial
     * 
     * @return true if this is an imperial unit, false for metric
     */
    public boolean isImperial() {
        return this == INCH || this == FOOT;
    }
    
    /**
     * Gets the smallest unit in the same system (metric/imperial)
     * 
     * @return The smallest unit in the same measurement system
     */
    public DomainDimensionUnit getSmallestInSystem() {
        if (isMetric()) {
            return MILLIMETER;
        } else {
            return INCH;
        }
    }
    
    /**
     * Gets the largest unit in the same system (metric/imperial)
     * 
     * @return The largest unit in the same measurement system
     */
    public DomainDimensionUnit getLargestInSystem() {
        if (isMetric()) {
            return METER;
        } else {
            return FOOT;
        }
    }
    
    /**
     * Parses a unit from a symbol string
     * 
     * @param symbol The symbol to parse (e.g., "mm", "cm", "in")
     * @return The corresponding unit
     * @throws IllegalArgumentException if the symbol is not recognized
     */
    public static DomainDimensionUnit fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        
        String lowerSymbol = symbol.toLowerCase().trim();
        
        for (DomainDimensionUnit unit : values()) {
            if (unit.getSymbol().toLowerCase().equals(lowerSymbol)) {
                return unit;
            }
        }
        
        throw new IllegalArgumentException("Unknown dimension unit symbol: " + symbol);
    }
    
    @Override
    public String toString() {
        return getDisplayName() + " (" + getSymbol() + ")";
    }
}