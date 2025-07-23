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
import java.util.List;
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
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", userId);
            fallback.put("role", "UNKNOWN");
            fallback.put("error", "Service unavailable");
            return fallback;
        }
    }

    @Override
    public boolean canPerformStockOperation(String userId, String warehouseId, String operationType) {
        try {
            // 1. Validate user exists and is valid
            if (!validateUser(userId)) {
                return false;
            }
            // 2. Check warehouse access
            String accessUrl = baseUrl + "/users/" + userId + "/warehouses/" + warehouseId + "/access";
            ResponseEntity<Map> accessResponse = restTemplate.getForEntity(accessUrl, Map.class);
            if (accessResponse.getStatusCode() != HttpStatus.OK
                    || accessResponse.getBody() == null
                    || !Boolean.TRUE.equals(accessResponse.getBody().get("access"))) {
                return false;
            }
            // 3. Check specific operation permission
            String permUrl = baseUrl + "/users/" + userId + "/permissions?operation=" + operationType;
            ResponseEntity<Map> permResponse = restTemplate.getForEntity(permUrl, Map.class);
            if (permResponse.getStatusCode() == HttpStatus.OK && permResponse.getBody() != null) {
                return Boolean.TRUE.equals(permResponse.getBody().get("allowed"));
            }
            return false;
        } catch (RestClientException e) {
            log.error("Failed to check if user {} can perform {} on warehouse {}: {}", userId, operationType, warehouseId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Failed to check user operation permission: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
    }

    @Override
    public Map<String, Object> getServiceHealthStatus() {
        try {
            String healthUrl = baseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            Map<String, Object> status = new HashMap<>();
            status.put("status", response.getStatusCode().toString());
            return status;
        } catch (Exception e) {
            log.error("Failed to get service health status: {}", e.getMessage());
            throw new InfrastructureExternalServiceException(
                    "Failed to get service health status: " + e.getMessage(),
                    "UserService",
                    0,
                    e
            );
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(baseUrl + "/health", Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("User service health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void logUserAction(String userId, String action, Map<String, Object> details) {
        try {
            retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/users/" + userId + "/actions";
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("action", action);
                    payload.put("details", details);
                    restTemplate.postForEntity(url, payload, Void.class);
                    return null;
                } catch (RestClientException e) {
                    log.error("Failed to log user action {} for user {}: {}", action, userId, e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to log user action: " + e.getMessage(),
                        "UserService",
                        0,
                        e
                    );
                }
            });
        } catch (Exception e) {
            log.error("User action logging failed after retries for user {} action {}: {}", userId, action, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "User action logging failed after retries: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
    }

    @Override
    public String getUserDepartment(String userId) {
        try {
            // Fetch user department with retry
            ResponseEntity<Map> response = retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/users/" + userId + "/department";
                    return restTemplate.getForEntity(url, Map.class);
                } catch (RestClientException e) {
                    log.error("Failed to get user department for {}: {}", userId, e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to get user department: " + e.getMessage(),
                        "UserService",
                        0,
                        e
                    );
                }
            });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object dept = response.getBody().get("department");
                return dept != null ? dept.toString() : null;
            }
            throw new InfrastructureExternalServiceException(
                "Failed to get user department for user: " + userId,
                "UserService",
                response.getStatusCodeValue(),
                null
            );
        } catch (Exception e) {
            log.error("Get user department failed after retries for user {}: {}", userId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Get user department failed after retries: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
    }

    // Added missing interface method implementations below

    @Override
    public String getUserDisplayName(String userId) {
        Map<String, Object> details = getUserDetails(userId);
        Object name = details.get("displayName");
        return name != null ? name.toString() : null;
    }

    @Override
    public String getUserRole(String userId) {
        Map<String, Object> details = getUserDetails(userId);
        Object role = details.get("role");
        return role != null ? role.toString() : null;
    }

    @Override
    public boolean hasPermission(String userId, String permission) {
        try {
            String url = baseUrl + "/users/" + userId + "/permissions/" + permission + "/check";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("allowed"));
            }
        } catch (RestClientException e) {
            log.error("Failed to check permission {} for user {}: {}", permission, userId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Failed to check permission: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
        return false;
    }

    @Override
    public boolean canAccessWarehouse(String userId, String warehouseId) {
        try {
            String url = baseUrl + "/users/" + userId + "/warehouses/" + warehouseId + "/access";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("access"));
            }
        } catch (RestClientException e) {
            log.error("Failed to check access for user {} to warehouse {}: {}", userId, warehouseId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Failed to check warehouse access: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
        return false;
    }

    @Override
    public List<String> getUserPermissions(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/permissions";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.error("Failed to get permissions for user {}: {}", userId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Failed to get user permissions: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
        return List.of();
    }

    @Override
    public List<String> getAccessibleWarehouses(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/warehouses";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.error("Failed to get accessible warehouses for user {}: {}", userId, e.getMessage());
            throw new InfrastructureExternalServiceException(
                "Failed to get accessible warehouses: " + e.getMessage(),
                "UserService",
                0,
                e
            );
        }
        return List.of();
    }

    @Override
    public Map<String, Boolean> validateMultipleUsers(List<String> userIds) {
        Map<String, Boolean> results = new HashMap<>();
        for (String id : userIds) {
            results.put(id, validateUser(id));
        }
        return results;
    }

    @Override
    public Map<String, Map<String, Object>> getMultipleUserDetails(List<String> userIds) {
        Map<String, Map<String, Object>> results = new HashMap<>();
        for (String id : userIds) {
            results.put(id, getUserDetails(id));
        }
        return results;
    }

    @Override
    public boolean isUserActive(String userId) {
        return validateUser(userId);
    }
}
