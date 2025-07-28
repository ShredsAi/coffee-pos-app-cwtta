package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainProductAttributeValueEntity;
import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.ports.DomainInputPortAttributeValueService;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.ports.DomainOutputPortAttributeRepository;
import ai.shreds.domain.exceptions.DomainProductNotFoundException;
import ai.shreds.domain.exceptions.DomainAttributeValidationException;
import ai.shreds.domain.exceptions.DomainAttributeNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service implementing attribute value operations on products.
 */
@Service
public class DomainAttributeValueService implements DomainInputPortAttributeValueService {

    private final DomainOutputPortProductRepository productRepository;
    private final DomainOutputPortAttributeRepository attributeRepository;
    private final DomainValidationService validationService;

    /**
     * Constructor with dependencies
     */
    public DomainAttributeValueService(
            DomainOutputPortProductRepository productRepository,
            DomainOutputPortAttributeRepository attributeRepository,
            DomainValidationService validationService) {
        this.productRepository = productRepository;
        this.attributeRepository = attributeRepository;
        this.validationService = validationService;
    }

    @Override
    public void assignValues(UUID productId, List<DomainProductAttributeValueEntity> values) {
        // Find the product
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Validate and assign each value
        for (DomainProductAttributeValueEntity value : values) {
            value.setProductId(productId);
            value.validateAgainstAttribute();
            product.addAttributeValue(value);
        }
        
        // Save the updated product
        productRepository.save(product);
    }

    @Override
    public DomainProductAttributeValueEntity updateValue(UUID valueId, Object newValue) {
        // Find the attribute value by searching through products
        // This is a simplified implementation - in practice you might have a separate attribute value repository
        for (DomainProductEntity product : findAllProducts()) {
            Optional<DomainProductAttributeValueEntity> valueOpt = product.getAttributes().stream()
                    .filter(attr -> attr.getId().equals(valueId))
                    .findFirst();
            
            if (valueOpt.isPresent()) {
                DomainProductAttributeValueEntity value = valueOpt.get();
                value.updateValue(newValue);
                productRepository.save(product);
                return value;
            }
        }
        
        throw new IllegalArgumentException("Attribute value with ID " + valueId + " not found");
    }

    @Override
    public void removeValue(UUID productId, UUID attributeId) {
        // Find the product
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Remove the attribute value
        product.getAttributes().removeIf(value -> value.getAttributeId().equals(attributeId));
        
        // Save the updated product
        productRepository.save(product);
    }

    @Override
    public void validateRequiredAttributes(UUID productId) {
        // Find the product
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Get all required attributes
        List<DomainProductAttributeEntity> requiredAttributes = getAllRequiredAttributes();
        
        // Check that each required attribute has a value
        for (DomainProductAttributeEntity requiredAttr : requiredAttributes) {
            boolean hasValue = product.getAttributes().stream()
                    .anyMatch(value -> value.getAttributeId().equals(requiredAttr.getId()) 
                            && value.getValue() != null);
            
            if (!hasValue) {
                throw new DomainAttributeValidationException(
                        requiredAttr.getCode(), 
                        "Required attribute '" + requiredAttr.getName() + "' must have a value");
            }
        }
    }
    
    @Override
    public List<DomainProductAttributeValueEntity> getProductAttributeValues(UUID productId) {
        // Find the product
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        return product.getAttributes();
    }
    
    @Override
    public DomainProductAttributeValueEntity getProductAttributeValue(UUID productId, UUID attributeId) {
        // Find the product
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        
        // Find the specific attribute value
        return product.getAttributes().stream()
                .filter(value -> value.getAttributeId().equals(attributeId))
                .findFirst()
                .orElse(null); // Return null if not found
    }
    
    /**
     * Helper method to get all required attributes
     */
    private List<DomainProductAttributeEntity> getAllRequiredAttributes() {
        // This is a simplified implementation
        // In practice, you would use the attribute repository with a specification
        return List.of(); // Placeholder - would query repository for required attributes
    }
    
    /**
     * Helper method to find all products (simplified implementation)
     */
    private List<DomainProductEntity> findAllProducts() {
        // This is a very inefficient implementation for demonstration
        // In practice, you would have a dedicated attribute value repository
        return List.of(); // Placeholder
    }
}