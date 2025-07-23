package ai.shreds.domain.enums;

/**
 * Enumeration representing different severity levels for inventory alerts.
 * Used to prioritize alerts based on their business impact.
 */
public enum DomainAlertSeverityEnum {
    /**
     * Low severity, informational alert
     */
    LOW,
    
    /**
     * Medium severity, requires attention
     */
    MEDIUM,
    
    /**
     * High severity, requires prompt action
     */
    HIGH,
    
    /**
     * Critical severity, requires immediate action
     */
    CRITICAL;
    
    /**
     * Determines if this severity level requires immediate notification.
     * 
     * @return true if the severity is HIGH or CRITICAL
     */
    public boolean requiresImmediateNotification() {
        return this == HIGH || this == CRITICAL; 
    }
    
    /**
     * Determines if this severity level should block operations until resolved.
     * 
     * @return true if the severity is CRITICAL
     */
    public boolean shouldBlockOperations() {
        return this == CRITICAL;
    }
    
    /**
     * Returns the recommended maximum time in hours before the alert should be addressed.
     * 
     * @return recommended maximum time in hours
     */
    public int getRecommendedMaxHoursToResolve() {
        switch (this) {
            case LOW: return 72; // 3 days
            case MEDIUM: return 24; // 1 day
            case HIGH: return 4;  // 4 hours
            case CRITICAL: return 1; // 1 hour
            default: return 24; // Default to 1 day
        }
    }
    
    /**
     * Calculates the appropriate severity level based on stock level percentage.
     * 
     * @param stockLevelPercentage the current stock level as a percentage of required level
     * @return the appropriate severity level
     */
    public static DomainAlertSeverityEnum fromStockLevelPercentage(int stockLevelPercentage) {
        if (stockLevelPercentage < 25) {
            return CRITICAL;
        } else if (stockLevelPercentage < 50) {
            return HIGH;
        } else if (stockLevelPercentage < 75) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}