package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.application.ports.ApplicationInputPortProductService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortProductService;
import ai.shreds.domain.ports.DomainInputPortCategoryService;
import ai.shreds.domain.ports.DomainInputPortAttributeValueService;
import ai.shreds.domain.ports.DomainOutputPortProductRepository;
import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.dtos.DomainCreateProductCommand;
import ai.shreds.domain.dtos.DomainUpdateProductCommand;
import ai.shreds.shared.dtos.SharedProductDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.enums.SharedPublicationStatus;
import ai.shreds.shared.dtos.SharedProductCreatedEvent;
import ai.shreds.shared.dtos.SharedProductUpdatedEvent;
import ai.shreds.shared.dtos.SharedProductDeletedEvent;
import ai.shreds.shared.exceptions.SharedConflictException;
import ai.shreds.shared.exceptions.SharedNotFoundException;
import ai.shreds.shared.dtos.ApplicationCreateProductCommand;
import ai.shreds.shared.dtos.ApplicationUpdateProductCommand;

import java.util.UUID;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationProductService implements ApplicationInputPortProductService {

    private final DomainInputPortProductService domainProductService;
    private final DomainInputPortCategoryService domainCategoryService;
    private final DomainInputPortAttributeValueService domainAttributeValueService;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    private final DomainOutputPortProductRepository productRepository;

    @Override
    public SharedProductDTO createProduct(ApplicationCreateProductCommand command) {
        DomainCreateProductCommand domainCmd = DomainCreateProductCommand.fromApplicationCommand(command, getCurrentUser());
        DomainProductEntity product = domainProductService.createProduct(domainCmd);
        SharedProductDTO dto = product.toDTO();
        eventPublisher.publishProductCreatedEvent(
            SharedProductCreatedEvent.fromProduct(product, getCurrentUser())
        );
        return dto;
    }

    @Override
    public SharedProductDTO updateProduct(ApplicationUpdateProductCommand command) {
        checkOptimisticLocking(command.getId(), command.getVersion());
        DomainUpdateProductCommand domainCmd = DomainUpdateProductCommand.fromApplicationCommand(command, getCurrentUser());
        DomainProductEntity updated = domainProductService.updateProduct(domainCmd);
        SharedProductDTO dto = updated.toDTO();
        eventPublisher.publishProductUpdatedEvent(
            SharedProductUpdatedEvent.fromProduct(updated, Collections.emptyMap(), getCurrentUser())
        );
        return dto;
    }

    @Override
    public SharedProductDTO getProduct(UUID id) {
        DomainProductEntity product = domainProductService.getProduct(id);
        return product.toDTO();
    }

    @Override
    public SharedPagedResponse<SharedProductDTO> listProducts(ApplicationProductFilterSpecification specification) {
        var page = productRepository.findAll(
            specification.toDomainSpecification(),
            specification.getPage(),
            specification.getSize()
        );
        var content = page.getContent().stream()
            .map(DomainProductEntity::toDTO)
            .collect(Collectors.toList());
        return SharedPagedResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public void deleteProduct(UUID id) {
        DomainProductEntity product = domainProductService.getProduct(id);
        domainProductService.deleteProduct(id);
        eventPublisher.publishProductDeletedEvent(
            SharedProductDeletedEvent.fromProduct(id, product.getName(), getCurrentUser())
        );
    }

    private void checkOptimisticLocking(UUID id, Long version) {
        DomainProductEntity existing = productRepository.findById(id)
            .orElseThrow(() -> new SharedNotFoundException("Product", id));
        if (!existing.getVersion().equals(version)) {
            throw new SharedConflictException("Product version mismatch for id: " + id);
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}