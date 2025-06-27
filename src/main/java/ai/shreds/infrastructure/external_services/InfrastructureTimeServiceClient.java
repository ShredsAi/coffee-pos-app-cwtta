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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

    @Value("${services.time.base-url:https://time/api}")
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
}