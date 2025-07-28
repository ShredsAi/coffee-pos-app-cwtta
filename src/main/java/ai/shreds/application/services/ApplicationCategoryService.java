package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.application.ports.ApplicationInputPortCategoryService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortCategoryService;
import ai.shreds.domain.ports.DomainOutputPortCategoryRepository;
import ai.shreds.domain.entities.DomainCategoryEntity;
import ai.shreds.domain.dtos.DomainCreateCategoryCommand;
import ai.shreds.domain.dtos.DomainUpdateCategoryCommand;
import ai.shreds.shared.dtos.SharedCategoryDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.SharedCategoryChangedEvent;
import ai.shreds.shared.dtos.ApplicationCreateCategoryCommand;
import ai.shreds.shared.dtos.ApplicationUpdateCategoryCommand;
import ai.shreds.shared.exceptions.SharedConflictException;
import ai.shreds.shared.exceptions.SharedNotFoundException;

import java.util.UUID;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationCategoryService implements ApplicationInputPortCategoryService {

    private final DomainInputPortCategoryService domainCategoryService;
    private final DomainOutputPortCategoryRepository categoryRepository;
    private final ApplicationOutputPortEventPublisher eventPublisher;

    @Override
    public SharedCategoryDTO createCategory(ApplicationCreateCategoryCommand command) {
        // Convert to domain command explicitly
        DomainCreateCategoryCommand domainCmd = (DomainCreateCategoryCommand) command.toDomainCommand();
        DomainCategoryEntity category = domainCategoryService.createCategory(domainCmd);
        SharedCategoryDTO dto = category.toDTO();
        eventPublisher.publishCategoryChangedEvent(
            SharedCategoryChangedEvent.fromCategory(category, "CREATED", Collections.emptyList(), getCurrentUser())
        );
        return dto;
    }

    @Override
    public SharedCategoryDTO updateCategory(ApplicationUpdateCategoryCommand command) {
        checkOptimisticLocking(command.getId(), command.getVersion());
        // Convert to domain command explicitly
        DomainUpdateCategoryCommand domainCmd = (DomainUpdateCategoryCommand) command.toDomainCommand();
        DomainCategoryEntity updated = domainCategoryService.updateCategory(domainCmd);

        // Recalculate hierarchy if parent changed
        if (command.getParentCategoryId() != null) {
            recalculateHierarchy(updated.getId());
        }

        SharedCategoryDTO dto = updated.toDTO();
        eventPublisher.publishCategoryChangedEvent(
            SharedCategoryChangedEvent.fromCategory(updated, "UPDATED", getAffectedProductIds(updated.getId()), getCurrentUser())
        );
        return dto;
    }

    @Override
    public SharedCategoryDTO getCategory(UUID id) {
        DomainCategoryEntity category = domainCategoryService.getCategory(id);
        return category.toDTO();
    }

    @Override
    public SharedPagedResponse<SharedCategoryDTO> listCategories(ApplicationCategoryFilterSpecification specification) {
        var page = categoryRepository.findAll(
            specification.toDomainSpecification(),
            specification.getPage(),
            specification.getSize()
        );
        var content = page.getContent().stream()
            .map(DomainCategoryEntity::toDTO)
            .collect(Collectors.toList());
        return SharedPagedResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public void deleteCategory(UUID id) {
        DomainCategoryEntity category = domainCategoryService.getCategory(id);
        List<UUID> affectedProducts = getAffectedProductIds(id);
        domainCategoryService.deleteCategory(id);
        eventPublisher.publishCategoryChangedEvent(
            SharedCategoryChangedEvent.fromCategory(category, "DELETED", affectedProducts, getCurrentUser())
        );
    }

    private void recalculateHierarchy(UUID categoryId) {
        domainCategoryService.recalculateHierarchy(categoryId);
    }

    private void checkOptimisticLocking(UUID id, Long version) {
        DomainCategoryEntity existing = categoryRepository.findById(id)
            .orElseThrow(() -> new SharedNotFoundException("Category", id));
        if (!existing.getVersion().equals(version)) {
            throw new SharedConflictException("Category version mismatch for id: " + id);
        }
    }

    private List<UUID> getAffectedProductIds(UUID categoryId) {
        // This would typically query products associated with this category
        // For now returning empty list, but in real implementation would query product-category associations
        return Collections.emptyList();
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}