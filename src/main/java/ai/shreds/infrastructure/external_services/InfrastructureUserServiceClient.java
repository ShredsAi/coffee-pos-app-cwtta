package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortUserService;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import ai.shreds.infrastructure.utilities.InfrastructureRetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Infrastructure implementation of user service operations.
 * Communicates with external user management service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureUserServiceClient implements DomainOutputPortUserService {

    private final RestTemplate restTemplate;
    private final InfrastructureRetryUtil retryUtil;

    @Value("${services.user.base-url:https://users/api}")
    private String baseUrl;

    @Override
    public boolean validateUser(String userId) {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/users/" + userId;
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        return true; // User exists and is valid
                    }
                    
                    return false;
                } catch (RestClientException e) {
                    log.error("Failed to validate user {}: {}", userId, e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to validate user: " + e.getMessage(),
                        "UserService",
                        0,
                        e
                    );
                }
            });
        } catch (Exception e) {
            log.error("User validation failed after retries for user {}", userId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserDetails(String userId) {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/users/" + userId;
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        return response.getBody();
                    }
                    
                    throw new InfrastructureExternalServiceException(
                        "User not found: " + userId,
                        "UserService",
                        response.getStatusCodeValue(),
                        null
                    );
                } catch (RestClientException e) {
                    log.error("Failed to get user details for {}: {}", userId, e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to get user details: " + e.getMessage(),
                        "UserService",
                        0,
                        e
                    );
                }
            });
        } catch (Exception e) {
            log.error("Get user details failed after retries for user {}", userId, e);
            // Return empty map as fallback
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", userId);
            fallback.put("role", "UNKNOWN");
            fallback.put("error", "Service unavailable");
            return fallback;
        }
    }
}