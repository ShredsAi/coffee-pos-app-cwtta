package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.ports.DomainOutputPortCategoryRepository;
import ai.shreds.domain.specifications.DomainCategorySpecification;
import ai.shreds.domain.value_objects.DomainPage;
import ai.shreds.infrastructure.entities.InfrastructureCategoryJpaEntity;
import ai.shreds.infrastructure.mappers.InfrastructureCategoryMapper;
import ai.shreds.infrastructure.specifications.InfrastructureCategorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of the category repository.
 * Implements the domain output port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InfrastructureCategoryRepositoryImpl implements DomainOutputPortCategoryRepository {

    private final InfrastructureCategoryJpaRepository categoryJpaRepository;
    private final InfrastructureProductCategoryJpaRepository productCategoryJpaRepository;
    private final InfrastructureCategoryMapper categoryMapper;

    @Override
    @Transactional
    public DomainCategoryEntity save(DomainCategoryEntity category) {
        log.debug("Saving category with ID: {}", category.getId());
        
        try {
            InfrastructureCategoryJpaEntity jpaEntity;
            
            if (category.getId() != null && categoryJpaRepository.existsById(category.getId())) {
                // Update existing category
                jpaEntity = categoryJpaRepository.findById(category.getId())
                        .orElseThrow(() -> new IllegalStateException("Category not found for update: " + category.getId()));
                categoryMapper.mergeDomainToJpa(category, jpaEntity);
            } else {
                // Create new category
                jpaEntity = categoryMapper.toJpaEntity(category);
                if (jpaEntity.getId() == null) {
                    jpaEntity.setId(UUID.randomUUID());
                }
            }
            
            InfrastructureCategoryJpaEntity savedEntity = categoryJpaRepository.save(jpaEntity);
            DomainCategoryEntity result = categoryMapper.toDomainEntity(savedEntity);
            
            log.debug("Successfully saved category with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error saving category with ID: {}", category.getId(), e);
            throw new RuntimeException("Failed to save category: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainCategoryEntity> findById(UUID id) {
        log.debug("Finding category by ID: {}", id);
        
        return categoryJpaRepository.findById(id)
                .map(categoryMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainCategoryEntity> findBySlug(String slug) {
        log.debug("Finding category by slug: {}", slug);
        
        return categoryJpaRepository.findBySlug(slug)
                .map(categoryMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainCategoryEntity> findByParentId(UUID parentId) {
        log.debug("Finding categories by parent ID: {}", parentId);
        
        return categoryJpaRepository.findByParentCategoryId(parentId)
                .stream()
                .map(categoryMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DomainPage<DomainCategoryEntity> findAll(DomainCategorySpecification specification, Integer page, Integer size) {
        log.debug("Finding categories with specification, page: {}, size: {}", page, size);
        
        try {
            // Create specification converter using static method
            Specification<InfrastructureCategoryJpaEntity> jpaSpec = 
                    (root, query, criteriaBuilder) -> InfrastructureCategorySpecification.toCriteria(specification, criteriaBuilder, root);
            
            // Create page request with hierarchical sort (level first, then sort order, then name)
            Sort defaultSort = Sort.by(Sort.Direction.ASC, "level")
                    .and(Sort.by(Sort.Direction.ASC, "sortOrder"))
                    .and(Sort.by(Sort.Direction.ASC, "name"));
            PageRequest pageRequest = PageRequest.of(
                specification != null ? (specification.getPage() != null ? specification.getPage() : 0) : (page != null ? page : 0),
                specification != null ? (specification.getSize() != null ? specification.getSize() : 20) : (size != null ? size : 20),
                defaultSort
            );
            
            // Execute query and get Spring Page
            Page<InfrastructureCategoryJpaEntity> jpaPage = categoryJpaRepository.findAll(jpaSpec, pageRequest);
            
            // Convert JPA entities to domain entities
            List<DomainCategoryEntity> domainEntities = jpaPage.getContent()
                    .stream()
                    .map(categoryMapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            // Convert Spring Page to DomainPage
            return DomainPage.of(
                domainEntities,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements()
            );
            
        } catch (Exception e) {
            log.error("Error finding categories with specification", e);
            throw new RuntimeException("Failed to find categories: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting category with ID: {}", id);
        
        try {
            if (!categoryJpaRepository.existsById(id)) {
                log.warn("Attempted to delete non-existent category: {}", id);
                return;
            }
            
            // Check if category has children
            List<InfrastructureCategoryJpaEntity> children = categoryJpaRepository.findByParentCategoryId(id);
            if (!children.isEmpty()) {
                log.warn("Cannot delete category {} as it has {} children", id, children.size());
                throw new IllegalStateException("Cannot delete category with children. Move or delete children first.");
            }
            
            // Check if category has active products
            long activeProductCount = productCategoryJpaRepository.countByCategoryId(id);
            if (activeProductCount > 0) {
                log.warn("Cannot delete category {} as it has {} products", id, activeProductCount);
                throw new IllegalStateException("Cannot delete category with products. Remove products first.");
            }
            
            // Delete the category
            categoryJpaRepository.deleteById(id);
            
            log.debug("Successfully deleted category with ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting category with ID: {}", id, e);
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        log.debug("Checking if category exists with slug: {}", slug);
        return categoryJpaRepository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveProductsInCategory(UUID categoryId) {
        // Delegate to JPA repository, assuming countByCategoryId filters active products
        return productCategoryJpaRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainCategoryEntity> findRootCategories() {
        log.debug("Finding root categories");
        
        return categoryJpaRepository.findRootCategories()
                .stream()
                .map(categoryMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainCategoryEntity> findAllDescendants(UUID categoryId) {
        log.debug("Finding all descendants of category ID: {}", categoryId);
        
        if (categoryId == null) {
            return List.of();
        }
        
        try {
            // Find the category to get its path
            Optional<InfrastructureCategoryJpaEntity> category = categoryJpaRepository.findById(categoryId);
            if (category.isEmpty()) {
                return List.of();
            }
            
            String categoryPath = category.get().getPath();
            if (categoryPath == null || categoryPath.trim().isEmpty()) {
                return List.of();
            }
            
            // Find all categories whose path starts with the parent path (descendants)
            return categoryJpaRepository.findDescendants(categoryPath)
                    .stream()
                    .map(categoryMapper::toDomainEntity)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error finding descendants of category {}", categoryId, e);
            return List.of();
        }
    }
}