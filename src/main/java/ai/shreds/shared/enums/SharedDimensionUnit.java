package ai.shreds.shared.enums;

/**
 * Enumeration of dimension units for product measurements.
 * Used to specify the unit of measurement for product dimensions.
 */
public enum SharedDimensionUnit {
    MILLIMETER("mm", "Millimeter", 1.0),
    CENTIMETER("cm", "Centimeter", 10.0),
    METER("m", "Meter", 1000.0),
    INCH("in", "Inch", 25.4),
    FOOT("ft", "Foot", 304.8);

    private final String symbol;
    private final String displayName;
    private final double millimeterConversion;

    SharedDimensionUnit(String symbol, String displayName, double millimeterConversion) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.millimeterConversion = millimeterConversion;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMillimeterConversion() {
        return millimeterConversion;
    }

    public double convertToMillimeters(double value) {
        return value * millimeterConversion;
    }

    public double convertFromMillimeters(double millimeters) {
        return millimeters / millimeterConversion;
    }

    public static SharedDimensionUnit fromSymbol(String symbol) {
        if (symbol == null) return null;
        for (SharedDimensionUnit unit : values()) {
            if (unit.symbol.equalsIgnoreCase(symbol)) {
                return unit;
            }
        }
        return null;
    }
}