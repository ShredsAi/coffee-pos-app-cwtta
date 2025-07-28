package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.application.ports.ApplicationInputPortAttributeService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortAttributeDefinitionService;
import ai.shreds.domain.ports.DomainOutputPortAttributeRepository;
import ai.shreds.domain.entities.DomainProductAttributeEntity;
import ai.shreds.domain.entities.DomainAttributeOptionEntity;
import ai.shreds.shared.dtos.SharedProductAttributeDTO;
import ai.shreds.shared.dtos.SharedAttributeOptionDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.SharedProductAttributeUpdatedEvent;
import ai.shreds.shared.dtos.ApplicationCreateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationUpdateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationCreateAttributeOptionCommand;
import ai.shreds.shared.exceptions.SharedConflictException;
import ai.shreds.shared.exceptions.SharedNotFoundException;

import java.util.UUID;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationAttributeService implements ApplicationInputPortAttributeService {

    private final DomainInputPortAttributeDefinitionService domainAttributeService;
    private final DomainOutputPortAttributeRepository attributeRepository;
    private final ApplicationOutputPortEventPublisher eventPublisher;

    @Override
    public SharedProductAttributeDTO createAttribute(ApplicationCreateAttributeCommand command) {
        var domainCmd = command.toDomainCommand();
        DomainProductAttributeEntity attribute = domainAttributeService.createAttribute(domainCmd);
        SharedProductAttributeDTO dto = attribute.toDTO();
        
        SharedProductAttributeUpdatedEvent event = new SharedProductAttributeUpdatedEvent();
        event.setAttributeId(attribute.getId());
        event.setAttributeCode(attribute.getCode());
        event.setChangeType("CREATED");
        event.setAffectedProductIds(Collections.emptyList());
        event.setUserId(getCurrentUser());
        eventPublisher.publishAttributeUpdatedEvent(event);
        
        return dto;
    }

    @Override
    public SharedProductAttributeDTO updateAttribute(ApplicationUpdateAttributeCommand command) {
        checkOptimisticLocking(command.getId(), command.getVersion());
        var domainCmd = command.toDomainCommand();
        DomainProductAttributeEntity updated = domainAttributeService.updateAttribute(domainCmd);
        SharedProductAttributeDTO dto = updated.toDTO();
        
        SharedProductAttributeUpdatedEvent event = new SharedProductAttributeUpdatedEvent();
        event.setAttributeId(updated.getId());
        event.setAttributeCode(updated.getCode());
        event.setChangeType("UPDATED");
        event.setAffectedProductIds(getAffectedProductIds(updated.getId()));
        event.setUserId(getCurrentUser());
        eventPublisher.publishAttributeUpdatedEvent(event);
        
        return dto;
    }

    @Override
    public SharedProductAttributeDTO getAttribute(UUID id) {
        DomainProductAttributeEntity attribute = domainAttributeService.getAttribute(id);
        return attribute.toDTO();
    }

    @Override
    public SharedPagedResponse<SharedProductAttributeDTO> listAttributes(ApplicationAttributeFilterSpecification specification) {
        var page = attributeRepository.findAll(
            specification.toDomainSpecification(),
            specification.getPage(),
            specification.getSize()
        );
        var content = page.getContent().stream()
            .map(DomainProductAttributeEntity::toDTO)
            .collect(Collectors.toList());
        return SharedPagedResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public void deleteAttribute(UUID id) {
        DomainProductAttributeEntity attribute = domainAttributeService.getAttribute(id);
        List<UUID> affectedProducts = getAffectedProductIds(id);
        domainAttributeService.deleteAttribute(id);
        
        SharedProductAttributeUpdatedEvent event = new SharedProductAttributeUpdatedEvent();
        event.setAttributeId(attribute.getId());
        event.setAttributeCode(attribute.getCode());
        event.setChangeType("DELETED");
        event.setAffectedProductIds(affectedProducts);
        event.setUserId(getCurrentUser());
        eventPublisher.publishAttributeUpdatedEvent(event);
    }

    @Override
    public SharedAttributeOptionDTO addAttributeOption(ApplicationCreateAttributeOptionCommand command) {
        var domainCmd = command.toDomainCommand();
        DomainAttributeOptionEntity option = domainAttributeService.addOption(domainCmd);
        SharedAttributeOptionDTO dto = option.toDTO();
        DomainProductAttributeEntity attribute = domainAttributeService.getAttribute(command.getAttributeId());
        
        SharedProductAttributeUpdatedEvent event = new SharedProductAttributeUpdatedEvent();
        event.setAttributeId(attribute.getId());
        event.setAttributeCode(attribute.getCode());
        event.setChangeType("OPTION_ADDED");
        event.setAffectedProductIds(getAffectedProductIds(command.getAttributeId()));
        event.setUserId(getCurrentUser());
        eventPublisher.publishAttributeUpdatedEvent(event);
        
        return dto;
    }

    private void checkOptimisticLocking(UUID id, Long version) {
        DomainProductAttributeEntity existing = attributeRepository.findById(id)
            .orElseThrow(() -> new SharedNotFoundException("Attribute", id));
        if (!existing.getVersion().equals(version)) {
            throw new SharedConflictException("Attribute version mismatch for id: " + id);
        }
    }

    private List<UUID> getAffectedProductIds(UUID attributeId) {
        return Collections.emptyList();
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}