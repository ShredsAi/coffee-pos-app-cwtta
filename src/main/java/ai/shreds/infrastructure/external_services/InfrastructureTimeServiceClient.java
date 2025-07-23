package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortTimeService;
import ai.shreds.infrastructure.utilities.InfrastructureRetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Infrastructure implementation of time service operations.
 * Provides consistent timestamps across the application.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureTimeServiceClient implements DomainOutputPortTimeService {

    private final RestTemplate restTemplate;
    private final InfrastructureRetryUtil retryUtil;
    private final AtomicLong monotonicCounter = new AtomicLong(0);

    @Value("${services.time.base-url:https://time-api.com}")
    private String baseUrl;
    
    @Value("${services.time.use-local-fallback:true}")
    private boolean useLocalFallback;

    @Override
    public LocalDateTime getCurrentTime() {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/now";
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        Map<String, Object> timeResponse = response.getBody();
                        String utcTime = (String) timeResponse.get("utc");
                        return LocalDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
                    }

                    throw new RestClientException("Invalid response from time service");
                } catch (RestClientException e) {
                    log.warn("Failed to get time from external service: {}, will use local time", e.getMessage());
                    if (useLocalFallback) {
                        return LocalDateTime.now(); // Fallback to system time
                    }
                    throw e; // Re-throw if fallback disabled
                }
            });
        } catch (Exception e) {
            log.error("Time service failed after retries", e);
            return LocalDateTime.now(); // Final fallback
        }
    }

    @Override
    public LocalDate getCurrentDate() {
        return getCurrentTime().toLocalDate();
    }

    @Override
    public String formatTimestamp(LocalDateTime timestamp, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return timestamp.format(formatter);
    }

    @Override
    public boolean isValidPastDate(LocalDate date) {
        return date != null && !date.isAfter(getCurrentDate());
    }

    @Override
    public boolean isValidFutureDate(LocalDate date) {
        return date != null && date.isAfter(getCurrentDate());
    }

    @Override
    public long getDaysFrom(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(date, getCurrentDate());
    }

    @Override
    public long getDaysUntil(LocalDate futureDate) {
        if (futureDate == null || futureDate.isBefore(getCurrentDate())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(getCurrentDate(), futureDate);
    }

    @Override
    public LocalDateTime convertToTimeZone(LocalDateTime timestamp, ZoneId targetZone) {
        return timestamp.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(targetZone)
                .toLocalDateTime();
    }

    @Override
    public DateTimeFormatter getFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    public boolean isTimeSynchronized() {
        try {
            // Simple check: compare local time with external time service
            LocalDateTime externalTime = getCurrentTime();
            LocalDateTime localTime = LocalDateTime.now();
            long differenceSeconds = Math.abs(ChronoUnit.SECONDS.between(localTime, externalTime));
            // Consider synchronized if difference is less than 5 seconds
            return differenceSeconds < 5;
        } catch (Exception e) {
            log.warn("Cannot check time synchronization: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getClockSkewMillis() {
        try {
            LocalDateTime externalTime = getCurrentTime();
            LocalDateTime localTime = LocalDateTime.now();
            return ChronoUnit.MILLIS.between(localTime, externalTime);
        } catch (Exception e) {
            log.warn("Cannot calculate clock skew: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getTimeSource() {
        if (useLocalFallback) {
            try {
                // Try external service first
                String url = baseUrl + "/source";
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String source = (String) response.getBody().get("source");
                    return source != null ? source : "External Time Service";
                }
            } catch (Exception e) {
                log.debug("Could not get external time source: {}", e.getMessage());
            }
            return "System Clock (with external fallback)";
        }
        return "External Time Service";
    }

    @Override
    public LocalDateTime getMonotonicTimestamp() {
        // Create a monotonic timestamp by adding microseconds to current time
        LocalDateTime currentTime = getCurrentTime();
        long counter = monotonicCounter.incrementAndGet();
        return currentTime.plusNanos(counter * 1000); // Add microseconds
    }

    @Override
    public String getCurrentTimeIsoString() {
        LocalDateTime currentTime = getCurrentTime();
        return currentTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}