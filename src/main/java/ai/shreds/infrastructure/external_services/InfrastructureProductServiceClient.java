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

import java.util.HashMap;
import java.util.Map;

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
            // Return empty map as fallback
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", productId.getValue());
            fallback.put("status", "UNKNOWN");
            fallback.put("error", "Service unavailable");
            return fallback;
        }
    }
}