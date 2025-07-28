package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.entities.DomainProductAttributeValueEntity;
import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.exceptions.DomainDuplicateSkuException;
import ai.shreds.domain.exceptions.DomainCategoryHierarchyException;
import ai.shreds.domain.exceptions.DomainAttributeValidationException;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.ports.DomainOutputPortCategoryRepository;
import ai.shreds.domain.ports.DomainOutputPortAttributeRepository;
import ai.shreds.domain.enums.DomainPublicationStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain Validation Service
 * Responsible for validating business rules and constraints
 */
@Service
public class DomainValidationService {

    private final DomainOutputPortProductRepository productRepository;
    private final DomainOutputPortCategoryRepository categoryRepository;
    private final DomainOutputPortAttributeRepository attributeRepository;

    /**
     * Constructor with dependencies
     */
    public DomainValidationService(
            DomainOutputPortProductRepository productRepository,
            DomainOutputPortCategoryRepository categoryRepository,
            DomainOutputPortAttributeRepository attributeRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.attributeRepository = attributeRepository;
    }

    /**
     * Validates product uniqueness (SKU and slug)
     * 
     * @param sku The SKU to validate
     * @param slug The slug to validate
     * @param excludeId The product ID to exclude from validation (for updates)
     * @throws DomainDuplicateSkuException if SKU or slug already exists
     */
    public void validateProductUniqueness(String sku, String slug, UUID excludeId) {
        // Validate SKU uniqueness
        if (sku != null && !sku.trim().isEmpty() && productRepository.existsBySku(sku)) {
            productRepository.findBySku(sku)
                    .ifPresent(existing -> {
                        if (excludeId == null || !existing.getId().equals(excludeId)) {
                            throw new DomainDuplicateSkuException(sku);
                        }
                    });
        }

        // Validate slug uniqueness
        if (slug != null && !slug.trim().isEmpty() && productRepository.existsBySlug(slug)) {
            productRepository.findBySlug(slug)
                    .ifPresent(existing -> {
                        if (excludeId == null || !existing.getId().equals(excludeId)) {
                            throw new IllegalArgumentException("Product with slug '" + slug + "' already exists");
                        }
                    });
        }
    }

    /**
     * Validates category uniqueness (slug within same parent)
     * 
     * @param slug The slug to validate
     * @param parentId The parent category ID
     * @param excludeId The category ID to exclude from validation (for updates)
     */
    public void validateCategoryUniqueness(String slug, UUID parentId, UUID excludeId) {
        if (slug != null && !slug.trim().isEmpty() && categoryRepository.existsBySlug(slug)) {
            categoryRepository.findBySlug(slug)
                    .ifPresent(existing -> {
                        boolean sameParent = (existing.getParentCategoryId() == null && parentId == null) ||
                                (existing.getParentCategoryId() != null && 
                                existing.getParentCategoryId().equals(parentId));
                                
                        if (sameParent && (excludeId == null || !existing.getId().equals(excludeId))) {
                            throw new IllegalArgumentException("Category with slug '" + slug + 
                                    "' already exists under the same parent");
                        }
                    });
        }
    }

    /**
     * Validates attribute code uniqueness
     * 
     * @param code The code to validate
     * @param excludeId The attribute ID to exclude from validation (for updates)
     */
    public void validateAttributeUniqueness(String code, UUID excludeId) {
        if (code != null && !code.trim().isEmpty() && attributeRepository.existsByCode(code)) {
            attributeRepository.findByCode(code)
                    .ifPresent(existing -> {
                        if (excludeId == null || !existing.getId().equals(excludeId)) {
                            throw new IllegalArgumentException("Attribute with code '" + code + "' already exists");
                        }
                    });
        }
    }

    /**
     * Validates publication requirements for a product
     * 
     * @param product The product to validate
     * @throws IllegalArgumentException if requirements are not met
     */
    public void validatePublicationRequirements(DomainProductEntity product) {
        Map<String, String> errors = new HashMap<>();

        // Validate required fields
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            errors.put("name", "Product name is required for publication");
        }

        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            errors.put("sku", "Product SKU is required for publication");
        }

        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            errors.put("slug", "Product slug is required for publication");
        }

        // Validate at least one category
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            errors.put("categories", "Product must have at least one category for publication");
        } else {
            // Validate primary category
            boolean hasPrimary = product.getCategories().stream()
                    .anyMatch(cat -> Boolean.TRUE.equals(cat.getIsPrimary()));

            if (!hasPrimary) {
                errors.put("primaryCategory", "Product must have a primary category for publication");
            }
        }

        // Validate required attributes
        for (DomainProductAttributeValueEntity value : product.getAttributes()) {
            if (value.getAttribute() != null && 
                    Boolean.TRUE.equals(value.getAttribute().getIsRequired()) && 
                    value.getValue() == null) {
                errors.put("attribute_" + value.getAttributeId(), 
                        "Required attribute '" + value.getAttribute().getName() + "' must have a value");
            }
        }

        // Throw exception if validation fails
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Publication requirements not met: " + errors);
        }
    }

    /**
     * Validates an attribute value against the attribute definition
     * 
     * @param attribute The attribute definition
     * @param value The value to validate
     * @throws DomainAttributeValidationException if validation fails
     */
    public void validateAttributeValue(DomainProductAttributeEntity attribute, Object value) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }

        // Check if required attribute has a value
        if (Boolean.TRUE.equals(attribute.getIsRequired()) && value == null) {
            throw new DomainAttributeValidationException(
                    attribute.getCode(), "Required attribute must have a value");
        }

        // Skip further validation if value is null
        if (value == null) {
            return;
        }

        // Validate value type
        if (!attribute.validateType(value)) {
            throw new DomainAttributeValidationException(
                    attribute.getCode(), 
                    "Value type mismatch. Expected type compatible with " + attribute.getAttributeType());
        }
    }

    /**
     * Validates the category hierarchy to prevent cycles and depth overflow
     * 
     * @param categoryId The category ID (null for new categories)
     * @param parentId The parent category ID
     * @throws DomainCategoryHierarchyException if validation fails
     */
    public void validateCategoryHierarchy(UUID categoryId, UUID parentId) {
        // Prevent self-referencing (only applicable when categoryId is not null)
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new DomainCategoryHierarchyException(
                    categoryId, "Category cannot be its own parent");
        }

        // Skip validation if no parent
        if (parentId == null) {
            return;
        }

        // Find parent category
        categoryRepository.findById(parentId)
                .ifPresent(parent -> {
                    // Check max depth (configurable, default 5)
                    int maxDepth = 5;
                    if (parent.getLevel() >= maxDepth) {
                        throw new DomainCategoryHierarchyException(
                                categoryId,
                                String.format("Category hierarchy cannot exceed %d levels", maxDepth));
                    }

                    // Check for cycles in ancestry chain only if categoryId is not null
                    if (categoryId != null) {
                        validateNoCycles(categoryId, parentId, new HashMap<>());
                    }
                });
    }

    /**
     * Recursive method to validate no cycles exist in category hierarchy
     */
    private void validateNoCycles(UUID categoryId, UUID ancestorId, Map<UUID, Boolean> visited) {
        // Ensure categoryId is not null (should not happen if called correctly)
        if (categoryId == null) {
            return;
        }

        // Mark this ancestor as visited
        visited.put(ancestorId, true);

        // Get the parent of this ancestor
        categoryRepository.findById(ancestorId)
                .ifPresent(ancestor -> {
                    UUID parentId = ancestor.getParentCategoryId();

                    // If parent is null, we've reached the root - no cycles
                    if (parentId == null) {
                        return;
                    }

                    // If we find the category ID in the ancestry chain, we have a cycle
                    if (categoryId.equals(parentId)) {
                        throw new DomainCategoryHierarchyException(
                                categoryId, "Cyclic reference detected in category hierarchy");
                    }

                    // If we've already visited this parent, no need to check again
                    if (visited.containsKey(parentId)) {
                        return;
                    }

                    // Continue up the ancestry chain
                    validateNoCycles(categoryId, parentId, visited);
                });
    }
}