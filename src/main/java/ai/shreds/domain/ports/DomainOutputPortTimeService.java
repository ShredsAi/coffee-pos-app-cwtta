package ai.shreds.domain.ports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Output port for time service operations.
 * Defines the contract for accessing consistent, reliable time information.
 * To be implemented by infrastructure layer.
 * 
 * This port is critical for ensuring consistent timestamps across distributed system components,
 * which is essential for transaction ordering, stock movement auditing, and batch expiration management.
 */
public interface DomainOutputPortTimeService {
    
    /**
     * Gets the current time as a UTC-based LocalDateTime.
     * Provides a consistent time source across different nodes to prevent clock skew issues.
     * 
     * @return the current system time, potentially synchronized with an external time source
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if time service is unavailable
     */
    LocalDateTime getCurrentTime();
    
    /**
     * Gets the current date in UTC.
     * Useful for date-only operations like expiration date comparisons.
     * 
     * @return the current date
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if time service is unavailable
     */
    LocalDate getCurrentDate();
    
    /**
     * Formats a timestamp using a standardized format for logs and audit records.
     * Ensures consistent timestamp formatting across the system.
     * 
     * @param timestamp the timestamp to format
     * @param pattern the format pattern (standard Java DateTimeFormatter patterns)
     * @return the formatted timestamp string
     */
    String formatTimestamp(LocalDateTime timestamp, String pattern);
    
    /**
     * Validates that a date is not in the future.
     * Useful for validating manufacturing dates and other past-only dates.
     * 
     * @param date the date to validate
     * @return true if the date is valid (not in the future)
     */
    boolean isValidPastDate(LocalDate date);
    
    /**
     * Validates that a date is in the future.
     * Useful for validating expiration dates.
     * 
     * @param date the date to validate
     * @return true if the date is valid (in the future)
     */
    boolean isValidFutureDate(LocalDate date);
    
    /**
     * Calculates the age in days between a date and the current date.
     * Useful for determining batch age.
     * 
     * @param date the reference date
     * @return the number of days between the reference date and the current date
     */
    long getDaysFrom(LocalDate date);
    
    /**
     * Calculates the days until a future date from the current date.
     * Useful for expiration tracking and alerts.
     * 
     * @param futureDate the future date
     * @return the number of days until the future date, or 0 if the date is in the past
     */
    long getDaysUntil(LocalDate futureDate);
    
    /**
     * Converts a timestamp to a different time zone.
     * Useful for reporting and display purposes.
     * 
     * @param timestamp the timestamp to convert
     * @param targetZone the target time zone
     * @return the timestamp in the target time zone
     */
    LocalDateTime convertToTimeZone(LocalDateTime timestamp, ZoneId targetZone);
    
    /**
     * Gets the timestamp formatter for a specific format.
     * Provides consistent date/time formatting.
     * 
     * @param pattern the format pattern
     * @return the formatter for the pattern
     */
    DateTimeFormatter getFormatter(String pattern);
    
    /**
     * Checks if the time service is synchronized and accurate.
     * Used for health checks.
     * 
     * @return true if the time service is synchronized and accurate
     */
    boolean isTimeSynchronized();
    
    /**
     * Gets the maximum clock skew in milliseconds between this node and the time reference.
     * Useful for monitoring and troubleshooting timestamp issues.
     * 
     * @return the clock skew in milliseconds, or null if not available
     */
    Long getClockSkewMillis();
    
    /**
     * Gets the time source currently being used.
     * 
     * @return a description of the time source (e.g., "NTP", "System Clock", "External Time Service")
     */
    String getTimeSource();
    
    /**
     * Gets a monotonically increasing timestamp that is guaranteed to be unique.
     * Useful for guaranteeing strict ordering of events.
     * 
     * @return a monotonic timestamp
     */
    LocalDateTime getMonotonicTimestamp();
    
    /**
     * Gets the standardized ISO-8601 representation of the current time.
     * 
     * @return the current time in ISO-8601 format
     */
    String getCurrentTimeIsoString();
}