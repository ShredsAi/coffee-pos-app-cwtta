package ai.shreds.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.shreds.application.ports.ApplicationInputPortMediaService;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.application.ports.ApplicationOutputPortStorageService;
import ai.shreds.application.value_objects.ApplicationFileMetadata;
import ai.shreds.domain.ports.DomainInputPortMediaManagementService;
import ai.shreds.domain.ports.DomainOutputPortMediaRepository;
import ai.shreds.domain.entities.DomainProductMediaEntity;
import ai.shreds.domain.value_objects.DomainFileMetadataValue;
import ai.shreds.shared.dtos.SharedProductMediaDTO;
import ai.shreds.shared.dtos.SharedProductMediaUpdatedEvent;
import ai.shreds.shared.dtos.ApplicationUploadMediaCommand;
import ai.shreds.shared.dtos.ApplicationUpdateMediaCommand;
import ai.shreds.shared.dtos.ApplicationReorderMediaCommand;
import ai.shreds.domain.dtos.DomainUploadMediaCommand;
import ai.shreds.domain.dtos.DomainUpdateMediaCommand;
import ai.shreds.domain.dtos.DomainReorderMediaCommand;
import ai.shreds.shared.exceptions.SharedBadRequestException;
import ai.shreds.shared.exceptions.SharedNotFoundException;
import ai.shreds.shared.enums.SharedMediaType;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationMediaService implements ApplicationInputPortMediaService {

    private final DomainInputPortMediaManagementService domainMediaService;
    private final ApplicationOutputPortStorageService storageService;
    private final DomainOutputPortMediaRepository mediaRepository;
    private final ApplicationOutputPortEventPublisher eventPublisher;

    @Override
    public SharedProductMediaDTO uploadMedia(ApplicationUploadMediaCommand command) {
        ApplicationFileMetadata fileMetadata = extractAndValidateMetadata(
            command.getFile(), command.getContentType());

        String url = storageService.uploadFile(
            command.getFile(), command.getFileName(), command.getContentType());

        DomainUploadMediaCommand domainCommand = DomainUploadMediaCommand.fromApplicationCommand(
            command, url, fileMetadata.toDomainValue());

        DomainProductMediaEntity media = domainMediaService.uploadMedia(domainCommand);
        SharedProductMediaDTO dto = media.toDTO();

        SharedProductMediaUpdatedEvent event = new SharedProductMediaUpdatedEvent();
        event.setMediaId(media.getId());
        event.setProductId(media.getProductId());
        event.setChangeType("UPLOADED");
        event.setMediaType(SharedMediaType.valueOf(media.getMediaType().name()));
        event.setUrl(media.getUrl());
        event.setUserId(getCurrentUser());
        eventPublisher.publishMediaUpdatedEvent(event);

        return dto;
    }

    @Override
    public SharedProductMediaDTO updateMedia(ApplicationUpdateMediaCommand command) {
        DomainUpdateMediaCommand domainCommand = DomainUpdateMediaCommand.fromApplicationCommand(command);
        DomainProductMediaEntity updated = domainMediaService.updateMedia(domainCommand);
        SharedProductMediaDTO dto = updated.toDTO();

        SharedProductMediaUpdatedEvent event = new SharedProductMediaUpdatedEvent();
        event.setMediaId(updated.getId());
        event.setProductId(updated.getProductId());
        event.setChangeType("UPDATED");
        event.setMediaType(SharedMediaType.valueOf(updated.getMediaType().name()));
        event.setUrl(updated.getUrl());
        event.setUserId(getCurrentUser());
        eventPublisher.publishMediaUpdatedEvent(event);

        return dto;
    }

    @Override
    public SharedProductMediaDTO getMedia(UUID productId, UUID mediaId) {
        DomainProductMediaEntity media = domainMediaService.getMedia(productId, mediaId);
        return media.toDTO();
    }

    @Override
    public List<SharedProductMediaDTO> listProductMedia(UUID productId) {
        List<DomainProductMediaEntity> mediaList = domainMediaService.listProductMedia(productId);
        return mediaList.stream().map(DomainProductMediaEntity::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteMedia(UUID productId, UUID mediaId) {
        DomainProductMediaEntity media = domainMediaService.getMedia(productId, mediaId);
        domainMediaService.deleteMedia(productId, mediaId);
        storageService.deleteFile(media.getUrl());

        SharedProductMediaUpdatedEvent event = new SharedProductMediaUpdatedEvent();
        event.setMediaId(media.getId());
        event.setProductId(media.getProductId());
        event.setChangeType("DELETED");
        event.setMediaType(SharedMediaType.valueOf(media.getMediaType().name()));
        event.setUrl(media.getUrl());
        event.setUserId(getCurrentUser());
        eventPublisher.publishMediaUpdatedEvent(event);
    }

    @Override
    public List<SharedProductMediaDTO> reorderMedia(ApplicationReorderMediaCommand command) {
        DomainReorderMediaCommand domainCommand = DomainReorderMediaCommand.fromApplicationCommand(command);
        List<DomainProductMediaEntity> reorderedMedia = domainMediaService.reorderMedia(domainCommand);
        List<SharedProductMediaDTO> dtoList = reorderedMedia.stream()
            .map(DomainProductMediaEntity::toDTO).collect(Collectors.toList());

        DomainProductMediaEntity primaryMedia = reorderedMedia.stream()
            .filter(DomainProductMediaEntity::getIsPrimary).findFirst().orElse(null);

        if (primaryMedia != null) {
            SharedProductMediaUpdatedEvent event = new SharedProductMediaUpdatedEvent();
            event.setMediaId(primaryMedia.getId());
            event.setProductId(primaryMedia.getProductId());
            event.setChangeType("REORDERED");
            event.setMediaType(SharedMediaType.valueOf(primaryMedia.getMediaType().name()));
            event.setUrl(primaryMedia.getUrl());
            event.setUserId(getCurrentUser());
            eventPublisher.publishMediaUpdatedEvent(event);
        }

        return dtoList;
    }

    private ApplicationFileMetadata extractAndValidateMetadata(byte[] file, String contentType) {
        ApplicationFileMetadata metadata = storageService.extractMetadata(file, contentType);
        if (metadata == null) {
            throw new SharedBadRequestException("Could not extract metadata from file");
        }
        if (!metadata.isValidForUpload()) {
            throw new SharedBadRequestException("File validation failed. Check file type, size or dimensions.");
        }
        return metadata;
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }
}