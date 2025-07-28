package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainProductCategoryEntity;
import ai.shreds.domain.enums.DomainPublicationStatus;
import ai.shreds.domain.ports.DomainInputPortProductService;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.ports.DomainOutputPortCategoryRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditWriter;
import ai.shreds.domain.exceptions.DomainProductNotFoundException;
import ai.shreds.domain.exceptions.DomainInvalidStateTransitionException;
import ai.shreds.domain.value_objects.DomainAuditEntry;
import ai.shreds.domain.dtos.DomainCreateProductCommand;
import ai.shreds.domain.dtos.DomainUpdateProductCommand;
import ai.shreds.domain.specifications.DomainProductSpecification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain Product Service
 * Implements the domain business logic for product management
 */
@Service
public class DomainProductService implements DomainInputPortProductService {

    private final DomainOutputPortProductRepository productRepository;
    private final DomainOutputPortCategoryRepository categoryRepository;
    private final DomainValidationService validationService;
    private final DomainOutputPortAuditWriter auditWriter;

    /**
     * Constructor with dependencies
     */
    public DomainProductService(
            DomainOutputPortProductRepository productRepository,
            DomainOutputPortCategoryRepository categoryRepository,
            DomainValidationService validationService,
            DomainOutputPortAuditWriter auditWriter) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.validationService = validationService;
        this.auditWriter = auditWriter;
    }

    @Override
    public DomainProductEntity createProduct(DomainCreateProductCommand command) {
        validationService.validateProductUniqueness(command.getSku(), command.getSlug(), null);
        DomainProductEntity product = DomainProductEntity.createDraft(command);
        if (command.getCategoryIds() != null && !command.getCategoryIds().isEmpty()) {
            linkCategories(product, command.getCategoryIds(), command.getPrimaryCategoryId());
        }
        DomainProductEntity savedProduct = productRepository.save(product);
        auditWriter.writeProductAudit(DomainAuditEntry.forCreate(savedProduct, "PRODUCT", command.getCreatedBy()));
        return savedProduct;
    }

    @Override
    public DomainProductEntity updateProduct(DomainUpdateProductCommand command) {
        DomainProductEntity existingProduct = productRepository.findById(command.getId())
                .orElseThrow(() -> new DomainProductNotFoundException(command.getId()));
        if (!existingProduct.getVersion().equals(command.getVersion())) {
            throw new IllegalStateException("Product has been modified by another user");
        }
        if (command.getSku() != null || command.getSlug() != null) {
            String sku = command.getSku() != null ? command.getSku() : existingProduct.getSku();
            String slug = command.getSlug() != null ? command.getSlug() : existingProduct.getSlug();
            validationService.validateProductUniqueness(sku, slug, existingProduct.getId());
        }
        DomainProductEntity beforeState = cloneProduct(existingProduct);
        updateProductProperties(existingProduct, command);
        if (command.getCategoryIds() != null) {
            existingProduct.getCategories().clear();
            linkCategories(existingProduct, command.getCategoryIds(), command.getPrimaryCategoryId());
        }
        DomainProductEntity updatedProduct = productRepository.save(existingProduct);
        auditWriter.writeProductAudit(DomainAuditEntry.forUpdate(beforeState, updatedProduct, "PRODUCT", command.getUpdatedBy()));
        return updatedProduct;
    }

    @Override
    public DomainProductEntity getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DomainProductNotFoundException(id));
    }

    @Override
    public List<DomainProductEntity> findProducts(DomainProductSpecification specification) {
        return productRepository.findAll(
                specification,
                specification.getEffectivePage(),
                specification.getEffectiveSize()
        ).getContent();
    }

    @Override
    public void deleteProduct(UUID id) {
        DomainProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new DomainProductNotFoundException(id));
        DomainAuditEntry auditEntry = DomainAuditEntry.forDelete(product, "PRODUCT", product.getUpdatedBy());
        productRepository.delete(id);
        auditWriter.writeProductAudit(auditEntry);
    }

    @Override
    public DomainProductEntity changePublicationStatus(UUID productId, DomainPublicationStatus newStatus) {
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        DomainProductEntity beforeState = cloneProduct(product);
        validateStateTransition(product.getPublicationStatus(), newStatus);
        if (newStatus == DomainPublicationStatus.PUBLISHED) {
            validationService.validatePublicationRequirements(product);
        }
        product.changePublicationStatus(newStatus);
        DomainProductEntity updatedProduct = productRepository.save(product);
        auditWriter.writeProductAudit(DomainAuditEntry.forUpdate(beforeState, updatedProduct, "PRODUCT", product.getUpdatedBy()));
        return updatedProduct;
    }

    /**
     * Validates that a product meets all requirements for publication
     */
    @Override
    public void validatePublicationRequirements(UUID productId) {
        DomainProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainProductNotFoundException(productId));
        validationService.validatePublicationRequirements(product);
    }

    private void updateProductProperties(DomainProductEntity product, DomainUpdateProductCommand command) {
        if (command.getName() != null) product.setName(command.getName());
        if (command.getDescription() != null) product.setDescription(command.getDescription());
        if (command.getShortDescription() != null) product.setShortDescription(command.getShortDescription());
        if (command.getBrand() != null) product.setBrand(command.getBrand());
        if (command.getModel() != null) product.setModel(command.getModel());
        if (command.getSku() != null) product.setSku(command.getSku());
        if (command.getSlug() != null) product.setSlug(command.getSlug());
        if (command.getPublicationStatus() != null) product.setPublicationStatus(command.getPublicationStatus());
        if (command.getIsActive() != null) product.setIsActive(command.getIsActive());
        if (command.getUpdatedBy() != null) product.setUpdatedBy(command.getUpdatedBy());
    }

    private void linkCategories(DomainProductEntity product, List<UUID> categoryIds, UUID primaryCategoryId) {
        for (UUID categoryId : categoryIds) {
            categoryRepository.findById(categoryId).ifPresent(category -> {
                DomainProductCategoryEntity assoc = DomainProductCategoryEntity.create(
                        product.getId(), categoryId, categoryId.equals(primaryCategoryId));
                assoc.setCategory(category);
                product.addCategory(assoc);
            });
        }
        if (primaryCategoryId != null && !product.getCategories().isEmpty()) {
            boolean found = product.getCategories().stream().anyMatch(c -> c.getCategoryId().equals(primaryCategoryId));
            product.setPrimaryCategory(found ? primaryCategoryId : product.getCategories().get(0).getCategoryId());
        } else if (!product.getCategories().isEmpty()) {
            product.setPrimaryCategory(product.getCategories().get(0).getCategoryId());
        }
    }

    private void validateStateTransition(DomainPublicationStatus current, DomainPublicationStatus target) {
        if (current != null && !current.canTransitionTo(target)) {
            throw new DomainInvalidStateTransitionException(current, target);
        }
    }

    private DomainProductEntity cloneProduct(DomainProductEntity product) {
        return DomainProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .brand(product.getBrand())
                .model(product.getModel())
                .sku(product.getSku())
                .slug(product.getSlug())
                .publicationStatus(product.getPublicationStatus())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .version(product.getVersion())
                .build();
    }
}
