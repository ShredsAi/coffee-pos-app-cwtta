package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortProductService;
import ai.shreds.domain.value_objects.DomainProductIdValue;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of product service operations.
 * Communicates with external product catalog service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureProductServiceClient implements DomainOutputPortProductService {

    private final RestTemplate restTemplate;
    private final InfrastructureRetryUtil retryUtil;

    @Value("${services.product.base-url:https://product-catalog/api}")
    private String baseUrl;

    @Override
    public boolean validateProduct(DomainProductIdValue productId) {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/products/" + productId.getValue();
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        Map<String, Object> product = response.getBody();
                        String status = (String) product.get("status");
                        return "ACTIVE".equalsIgnoreCase(status);
                    }

                    return false;
                } catch (RestClientException e) {
                    log.error("Failed to validate product {}: {}", productId.getValue(), e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to validate product: " + e.getMessage(),
                        "ProductService",
                        0,
                        e
                    );
                }
            });
        } catch (Exception e) {
            log.error("Product validation failed after retries for product {}", productId.getValue(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getProductDetails(DomainProductIdValue productId) {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/products/" + productId.getValue();
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        return response.getBody();
                    }

                    throw new InfrastructureExternalServiceException(
                        "Product not found or inactive: " + productId.getValue(),
                        "ProductService",
                        response.getStatusCodeValue(),
                        null
                    );
                } catch (RestClientException e) {
                    log.error("Failed to get product details for {}: {}", productId.getValue(), e.getMessage());
                    throw new InfrastructureExternalServiceException(
                        "Failed to get product details: " + e.getMessage(),
                        "ProductService",
                        0,
                        e
                    );
                }
            });
        } catch (Exception e) {
            log.error("Get product details failed after retries for product {}", productId.getValue(), e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", productId.getValue());
            fallback.put("status", "UNKNOWN");
            fallback.put("error", "Service unavailable");
            return fallback;
        }
    }

    @Override
    public String getDefaultUnitOfMeasure(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            String unit = (String) productDetails.get("defaultUnit");
            return unit != null ? unit : "pieces"; // Default fallback
        } catch (Exception e) {
            log.warn("Failed to get default unit for product {}: {}", productId.getValue(), e.getMessage());
            return "pieces"; // Safe fallback
        }
    }

    @Override
    public String getProductName(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            String name = (String) productDetails.get("name");
            return name != null ? name : "Unknown Product"; // Fallback
        } catch (Exception e) {
            log.warn("Failed to get product name for {}: {}", productId.getValue(), e.getMessage());
            return "Unknown Product";
        }
    }

    @Override
    public boolean requiresBatchTracking(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            Boolean batchTracking = (Boolean) productDetails.get("requiresBatchTracking");
            return batchTracking != null ? batchTracking : false;
        } catch (Exception e) {
            log.warn("Failed to check batch tracking requirement for product {}: {}", productId.getValue(), e.getMessage());
            return false; // Safe default
        }
    }

    @Override
    public boolean isPerishable(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            Boolean perishable = (Boolean) productDetails.get("isPerishable");
            return perishable != null ? perishable : false;
        } catch (Exception e) {
            log.warn("Failed to check if product {} is perishable: {}", productId.getValue(), e.getMessage());
            return false; // Safe default
        }
    }

    @Override
    public String getProductCategory(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            String category = (String) productDetails.get("category");
            return category != null ? category : "General";
        } catch (Exception e) {
            log.warn("Failed to get category for product {}: {}", productId.getValue(), e.getMessage());
            return "General";
        }
    }

    @Override
    public Map<DomainProductIdValue, Boolean> validateMultipleProducts(List<DomainProductIdValue> productIds) {
        Map<DomainProductIdValue, Boolean> results = new HashMap<>();
        
        if (productIds == null || productIds.isEmpty()) {
            return results;
        }

        // For now, validate each product individually
        // In a real implementation, this could be optimized with a bulk API call
        for (DomainProductIdValue productId : productIds) {
            try {
                results.put(productId, validateProduct(productId));
            } catch (Exception e) {
                log.warn("Failed to validate product {} in bulk operation: {}", productId.getValue(), e.getMessage());
                results.put(productId, false);
            }
        }

        return results;
    }

    @Override
    public Map<DomainProductIdValue, Map<String, Object>> getMultipleProductDetails(List<DomainProductIdValue> productIds) {
        Map<DomainProductIdValue, Map<String, Object>> results = new HashMap<>();
        
        if (productIds == null || productIds.isEmpty()) {
            return results;
        }

        // For now, get details for each product individually
        // In a real implementation, this could be optimized with a bulk API call
        for (DomainProductIdValue productId : productIds) {
            try {
                results.put(productId, getProductDetails(productId));
            } catch (Exception e) {
                log.warn("Failed to get details for product {} in bulk operation: {}", productId.getValue(), e.getMessage());
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error", "Failed to retrieve details");
                errorDetails.put("id", productId.getValue());
                results.put(productId, errorDetails);
            }
        }

        return results;
    }

    @Override
    public boolean isDiscontinued(DomainProductIdValue productId) {
        try {
            Map<String, Object> productDetails = getProductDetails(productId);
            String status = (String) productDetails.get("status");
            return "DISCONTINUED".equalsIgnoreCase(status) || "OBSOLETE".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.warn("Failed to check if product {} is discontinued: {}", productId.getValue(), e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getAllowedUnitsOfMeasure(DomainProductIdValue productId) {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/products/" + productId.getValue() + "/units";
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        Map<String, Object> unitsResponse = response.getBody();
                        @SuppressWarnings("unchecked")
                        List<String> units = (List<String>) unitsResponse.get("allowedUnits");
                        if (units != null && !units.isEmpty()) {
                            return units;
                        }
                    }

                    // Fallback: try to get from product details
                    Map<String, Object> productDetails = getProductDetails(productId);
                    @SuppressWarnings("unchecked")
                    List<String> unitsFromDetails = (List<String>) productDetails.get("allowedUnits");
                    if (unitsFromDetails != null && !unitsFromDetails.isEmpty()) {
                        return unitsFromDetails;
                    }

                    // Final fallback: return default unit
                    String defaultUnit = getDefaultUnitOfMeasure(productId);
                    return Collections.singletonList(defaultUnit);

                } catch (RestClientException e) {
                    log.warn("Failed to get allowed units for product {}: {}", productId.getValue(), e.getMessage());
                    // Fallback to common units
                    return Arrays.asList("pieces", "kg", "grams", "liters", "ml");
                }
            });
        } catch (Exception e) {
            log.error("Get allowed units failed after retries for product {}", productId.getValue(), e);
            // Safe fallback
            return Arrays.asList("pieces", "kg", "grams", "liters", "ml");
        }
    }

    @Override
    public boolean isValidUnitForProduct(DomainProductIdValue productId, String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return false;
        }
        
        try {
            List<String> allowedUnits = getAllowedUnitsOfMeasure(productId);
            return allowedUnits.contains(unit.trim().toLowerCase()) || 
                   allowedUnits.contains(unit.trim());
        } catch (Exception e) {
            log.warn("Failed to validate unit {} for product {}: {}", unit, productId.getValue(), e.getMessage());
            // Basic validation: accept any non-null, non-empty unit
            return true;
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(baseUrl + "/health", Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Product service health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getServiceHealthStatus() {
        try {
            return retryUtil.executeWithExponentialBackoff(() -> {
                try {
                    String url = baseUrl + "/health";
                    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        return response.getBody();
                    }
                    throw new InfrastructureExternalServiceException("Product service health not ok: " + response.getStatusCode(), "ProductService", response.getStatusCodeValue(), null);
                } catch (RestClientException e) {
                    log.error("Failed to get product service health status: {}", e.getMessage());
                    throw new InfrastructureExternalServiceException("Failed to get service health: " + e.getMessage(), "ProductService", 0, e);
                }
            });
        } catch (Exception e) {
            log.error("Product service health check failed after retries", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("status", "UNKNOWN");
            fallback.put("error", e.getMessage());
            return fallback;
        }
    }
}