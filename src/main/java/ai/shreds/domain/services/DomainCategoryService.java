package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.ports.DomainInputPortCategoryService;
import ai.shreds.domain.ports.DomainOutputPortCategoryRepository;
import ai.shreds.domain.ports.DomainOutputPortAuditWriter;
import ai.shreds.domain.exceptions.DomainCategoryNotFoundException;
import ai.shreds.domain.exceptions.DomainCategoryHierarchyException;
import ai.shreds.domain.value_objects.DomainAuditEntry;
import ai.shreds.domain.dtos.DomainCreateCategoryCommand;
import ai.shreds.domain.dtos.DomainUpdateCategoryCommand;
import ai.shreds.domain.specifications.DomainCategorySpecification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain Category Service
 * Implements the domain business logic for category management
 */
@Service
public class DomainCategoryService implements DomainInputPortCategoryService {

    private final DomainOutputPortCategoryRepository categoryRepository;
    private final DomainValidationService validationService;
    private final DomainOutputPortAuditWriter auditWriter;

    /**
     * Constructor with dependencies
     */
    public DomainCategoryService(
            DomainOutputPortCategoryRepository categoryRepository,
            DomainValidationService validationService,
            DomainOutputPortAuditWriter auditWriter) {
        this.categoryRepository = categoryRepository;
        this.validationService = validationService;
        this.auditWriter = auditWriter;
    }

    @Override
    public DomainCategoryEntity createCategory(DomainCreateCategoryCommand command) {
        // Validate slug uniqueness in the same parent context
        validationService.validateCategoryUniqueness(command.getSlug(), command.getParentCategoryId(), null);
        
        // Validate hierarchy constraints
        validationService.validateCategoryHierarchy(null, command.getParentCategoryId());
        
        // Create category entity
        DomainCategoryEntity category = DomainCategoryEntity.create(
                command.getName(), 
                command.getSlug(), 
                command.getDescription(), 
                command.getParentCategoryId(), 
                command.getSortOrder(),
                command.getCreatedBy());
        
        // Calculate hierarchy information
        calculateHierarchyInfo(category);
        
        // Save the category
        DomainCategoryEntity savedCategory = categoryRepository.save(category);
        
        // Write audit
        auditWriter.writeCategoryAudit(DomainAuditEntry.forCreate(
                savedCategory, "CATEGORY", command.getCreatedBy()));
        
        return savedCategory;
    }

    @Override
    public DomainCategoryEntity updateCategory(DomainUpdateCategoryCommand command) {
        // Find existing category
        DomainCategoryEntity existingCategory = categoryRepository.findById(command.getId())
                .orElseThrow(() -> new DomainCategoryNotFoundException(command.getId()));
        
        // Check version for optimistic locking
        if (!existingCategory.getVersion().equals(command.getVersion())) {
            throw new IllegalStateException("Category has been modified by another user");
        }
        
        // Validate slug uniqueness if changed
        if (command.getSlug() != null && !command.getSlug().equals(existingCategory.getSlug())) {
            validationService.validateCategoryUniqueness(
                    command.getSlug(), 
                    command.getParentCategoryId() != null 
                        ? command.getParentCategoryId() 
                        : existingCategory.getParentCategoryId(), 
                    existingCategory.getId());
        }
        
        // Store the before state for audit
        DomainCategoryEntity beforeState = cloneCategory(existingCategory);
        
        // Check if parent has changed
        boolean parentChanged = command.getParentCategoryId() != null && 
                !command.getParentCategoryId().equals(existingCategory.getParentCategoryId());
        
        // If parent changed, validate hierarchy constraints
        if (parentChanged) {
            validationService.validateCategoryHierarchy(
                    existingCategory.getId(), command.getParentCategoryId());
        }
        
        // Update properties
        updateCategoryProperties(existingCategory, command);
        
        // Recalculate hierarchy if parent changed
        if (parentChanged) {
            calculateHierarchyInfo(existingCategory);
            
            // Update children recursively
            List<DomainCategoryEntity> children = categoryRepository.findByParentId(existingCategory.getId());
            for (DomainCategoryEntity child : children) {
                child.updateHierarchy(existingCategory.getPath(), existingCategory.getLevel());
                categoryRepository.save(child);
            }
        }
        
        // Save the updated category
        DomainCategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        
        // Write audit
        auditWriter.writeCategoryAudit(DomainAuditEntry.forUpdate(
                beforeState, updatedCategory, "CATEGORY", command.getUpdatedBy()));
        
        return updatedCategory;
    }

    @Override
    public DomainCategoryEntity getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainCategoryNotFoundException(id));
    }

    @Override
    public List<DomainCategoryEntity> findCategories(DomainCategorySpecification specification) {
        return categoryRepository.findAll(
                specification, 
                specification.getEffectivePage(), 
                specification.getEffectiveSize())
                .getContent();
    }

    @Override
    public void deleteCategory(UUID id) {
        DomainCategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new DomainCategoryNotFoundException(id));
        
        // Check if category has children
        List<DomainCategoryEntity> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new DomainCategoryHierarchyException(
                    id, "Cannot delete category with children. Delete children first or reassign them.");
        }
        
        // Check if category has products
        long productCount = categoryRepository.countActiveProductsInCategory(id);
        if (productCount > 0) {
            throw new DomainCategoryHierarchyException(
                    id, String.format("Cannot delete category with %d active products associated.", productCount));
        }
        
        // Create audit before deletion
        DomainAuditEntry auditEntry = DomainAuditEntry.forDelete(
                category, "CATEGORY", category.getUpdatedBy());
        
        // Delete the category
        categoryRepository.delete(id);
        
        // Write audit after deletion
        auditWriter.writeCategoryAudit(auditEntry);
    }

    @Override
    public void recalculateHierarchy(UUID categoryId) {
        // Find the category
        DomainCategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainCategoryNotFoundException(categoryId));
        
        // Recalculate its hierarchy information
        calculateHierarchyInfo(category);
        
        // Save the updated category
        categoryRepository.save(category);
        
        // Recursively update all descendants
        updateDescendantHierarchy(category);
    }
    
    /**
     * Calculates hierarchy information (level and path) for a category
     */
    private void calculateHierarchyInfo(DomainCategoryEntity category) {
        if (category.getParentCategoryId() == null) {
            // Root category
            category.setLevel(0);
            category.setPath("/" + category.getSlug());
        } else {
            // Child category - need parent info
            categoryRepository.findById(category.getParentCategoryId())
                    .ifPresent(parent -> {
                        category.setLevel(parent.getLevel() + 1);
                        category.setPath(parent.getPath() + "/" + category.getSlug());
                    });
        }
    }
    
    /**
     * Recursively updates hierarchy for all descendants
     */
    private void updateDescendantHierarchy(DomainCategoryEntity parent) {
        List<DomainCategoryEntity> children = categoryRepository.findByParentId(parent.getId());
        
        for (DomainCategoryEntity child : children) {
            child.updateHierarchy(parent.getPath(), parent.getLevel());
            categoryRepository.save(child);
            
            // Continue recursively
            updateDescendantHierarchy(child);
        }
    }
    
    /**
     * Updates properties of a category from an update command
     */
    private void updateCategoryProperties(DomainCategoryEntity category, DomainUpdateCategoryCommand command) {
        // Update only non-null fields
        if (command.getName() != null) {
            category.setName(command.getName());
        }
        if (command.getDescription() != null) {
            category.setDescription(command.getDescription());
        }
        if (command.getSlug() != null) {
            category.setSlug(command.getSlug());
        }
        if (command.getParentCategoryId() != null) {
            category.setParentCategoryId(command.getParentCategoryId());
        }
        if (command.getSortOrder() != null) {
            category.setSortOrder(command.getSortOrder());
        }
        if (command.getIsActive() != null) {
            category.setIsActive(command.getIsActive());
        }
        if (command.getUpdatedBy() != null) {
            category.setUpdatedBy(command.getUpdatedBy());
        }
    }
    
    /**
     * Creates a shallow clone of a category for audit purposes
     */
    private DomainCategoryEntity cloneCategory(DomainCategoryEntity category) {
        return DomainCategoryEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .parentCategoryId(category.getParentCategoryId())
                .level(category.getLevel())
                .path(category.getPath())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .version(category.getVersion())
                .build();
    }
}