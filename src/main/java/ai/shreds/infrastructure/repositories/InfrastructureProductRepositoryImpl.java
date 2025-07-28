package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.specifications.DomainProductSpecification;
import ai.shreds.domain.value_objects.DomainPage;
import ai.shreds.infrastructure.entities.InfrastructureProductJpaEntity;
import ai.shreds.infrastructure.mappers.InfrastructureProductMapper;
import ai.shreds.infrastructure.specifications.InfrastructureProductSpecification;
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
 * Infrastructure implementation of the product repository.
 * Implements the domain output port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InfrastructureProductRepositoryImpl implements DomainOutputPortProductRepository {

    private final InfrastructureProductJpaRepository productJpaRepository;
    private final InfrastructureCategoryJpaRepository categoryJpaRepository;
    private final InfrastructureProductAttributeValueJpaRepository attributeValueJpaRepository;
    private final InfrastructureMediaJpaRepository mediaJpaRepository;
    private final InfrastructureProductMapper productMapper;

    @Override
    @Transactional
    public DomainProductEntity save(DomainProductEntity product) {
        log.debug("Saving product with ID: {}", product.getId());
        
        try {
            InfrastructureProductJpaEntity jpaEntity;
            
            if (product.getId() != null && productJpaRepository.existsById(product.getId())) {
                // Update existing product
                jpaEntity = productJpaRepository.findById(product.getId())
                        .orElseThrow(() -> new IllegalStateException("Product not found for update: " + product.getId()));
                productMapper.mergeDomainToJpa(product, jpaEntity);
            } else {
                // Create new product
                jpaEntity = productMapper.toJpaEntity(product);
                if (jpaEntity.getId() == null) {
                    jpaEntity.setId(UUID.randomUUID());
                }
            }
            
            InfrastructureProductJpaEntity savedEntity = productJpaRepository.save(jpaEntity);
            DomainProductEntity result = productMapper.toDomainEntity(savedEntity);
            
            log.debug("Successfully saved product with ID: {}", result.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Error saving product with ID: {}", product.getId(), e);
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductEntity> findById(UUID id) {
        log.debug("Finding product by ID: {}", id);
        
        return productJpaRepository.findById(id)
                .map(productMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductEntity> findBySku(String sku) {
        log.debug("Finding product by SKU: {}", sku);
        
        return productJpaRepository.findBySku(sku)
                .map(productMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DomainProductEntity> findBySlug(String slug) {
        log.debug("Finding product by slug: {}", slug);
        
        return productJpaRepository.findBySlug(slug)
                .map(productMapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public DomainPage<DomainProductEntity> findAll(DomainProductSpecification specification, Integer page, Integer size) {
        log.debug("Finding products with specification, page: {}, size: {}", page, size);
        
        try {
            // Create specification converter using static method
            Specification<InfrastructureProductJpaEntity> jpaSpec = 
                    (root, query, criteriaBuilder) -> InfrastructureProductSpecification.toCriteria(specification, criteriaBuilder, root);
            
            // Create page request with default sort
            Sort defaultSort = Sort.by(Sort.Direction.DESC, "updatedAt")
                    .and(Sort.by(Sort.Direction.ASC, "name"));
            PageRequest pageRequest = PageRequest.of(
                specification != null ? specification.getEffectivePage() : (page != null ? page : 0), 
                specification != null ? specification.getEffectiveSize() : (size != null ? size : 20), 
                defaultSort
            );
            
            // Execute query and get Spring Page
            Page<InfrastructureProductJpaEntity> jpaPage = productJpaRepository.findAll(jpaSpec, pageRequest);
            
            // Convert JPA entities to domain entities
            List<DomainProductEntity> domainEntities = jpaPage.getContent()
                    .stream()
                    .map(productMapper::toDomainEntity)
                    .collect(Collectors.toList());
            
            // Convert Spring Page to DomainPage
            return DomainPage.of(
                domainEntities,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements()
            );
            
        } catch (Exception e) {
            log.error("Error finding products with specification", e);
            throw new RuntimeException("Failed to find products: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting product with ID: {}", id);
        
        try {
            if (!productJpaRepository.existsById(id)) {
                log.warn("Attempted to delete non-existent product: {}", id);
                return;
            }
            
            // Delete associated data first (due to foreign key constraints)
            attributeValueJpaRepository.deleteByProductId(id);
            mediaJpaRepository.deleteByProductId(id);
            
            // Delete the product
            productJpaRepository.deleteById(id);
            
            log.debug("Successfully deleted product with ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        log.debug("Checking if product exists with SKU: {}", sku);
        return productJpaRepository.existsBySku(sku);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        log.debug("Checking if product exists with slug: {}", slug);
        return productJpaRepository.existsBySlug(slug);
    }

    /**
     * Additional helper methods for the infrastructure layer
     */
    
    /**
     * Checks if SKU is unique for a specific product (excluding itself)
     * @param sku the SKU to check
     * @param excludeId the product ID to exclude from the check
     * @return true if SKU is unique
     */
    public boolean isSkuUniqueForProduct(String sku, UUID excludeId) {
        return !productJpaRepository.existsBySkuAndIdNot(sku, excludeId);
    }
    
    /**
     * Checks if slug is unique for a specific product (excluding itself)
     * @param slug the slug to check
     * @param excludeId the product ID to exclude from the check
     * @return true if slug is unique
     */
    public boolean isSlugUniqueForProduct(String slug, UUID excludeId) {
        return !productJpaRepository.existsBySlugAndIdNot(slug, excludeId);
    }
    
    /**
     * Finds products by brand
     * @param brand the brand name
     * @return list of domain product entities
     */
    public List<DomainProductEntity> findByBrand(String brand) {
        return productJpaRepository.findByBrandAndActive(brand)
                .stream()
                .map(productMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds recently updated products for reindexing
     * @param since the timestamp to check updates since
     * @return list of recently updated products
     */
    public List<DomainProductEntity> findUpdatedSince(java.time.Instant since) {
        return productJpaRepository.findUpdatedSince(since)
                .stream()
                .map(productMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}