package ai.shreds.application.services;

import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.commands.DomainCreateBatchCommand;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.shared.dtos.SharedBatchRequestDTO;
import ai.shreds.shared.dtos.SharedBatchResponseDTO;
import ai.shreds.shared.dtos.SharedBatchCreatedEventDTO;
import ai.shreds.shared.value_objects.SharedQuantityValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Mapper(componentModel = "spring")
public interface ApplicationBatchMapper {

    @Mapping(target = "productId", source = "productId", qualifiedByName = "uuidToDomainProductId")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "sharedToDomainQuantity")
    DomainCreateBatchCommand toDomainCommand(SharedBatchRequestDTO request);

    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "domainToSharedQuantity")
    SharedBatchResponseDTO toResponseDTO(DomainBatchEntity batch);

    @Mapping(target = "batchId", source = "id")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "domainToSharedQuantity")
    @Mapping(target = "timestamp", source = "receivedAt")
    @Mapping(target = "fifoOrder", ignore = true) // Will be set in service based on received_at ordering
    SharedBatchCreatedEventDTO toEventDTO(DomainBatchEntity batch);

    // Custom mapping methods
    @Named("uuidToDomainProductId")
    default DomainProductIdValue uuidToDomainProductId(UUID productId) {
        return productId != null ? new DomainProductIdValue(productId.toString()) : null;
    }

    @Named("sharedToDomainQuantity")
    default DomainQuantityValue sharedToDomainQuantity(SharedQuantityValue shared) {
        return shared != null ? new DomainQuantityValue(shared.getValue(), shared.getUnit()) : null;
    }

    @Named("domainToSharedQuantity")
    default SharedQuantityValue domainToSharedQuantity(DomainQuantityValue domain) {
        return domain != null ? new SharedQuantityValue(domain.getValue(), domain.getUnit()) : null;
    }

    // Helper method to create a complete event DTO with FIFO order
    default SharedBatchCreatedEventDTO toEventDTOWithFifoOrder(DomainBatchEntity batch, Integer fifoOrder) {
        if (batch == null) return null;
        
        SharedBatchCreatedEventDTO event = new SharedBatchCreatedEventDTO();
        event.setBatchId(batch.getId());
        event.setWarehouseId(batch.getWarehouseId());
        // Fix: Convert String to UUID
        event.setProductId(UUID.fromString(batch.getProductId().getValue()));
        event.setBatchNumber(batch.getBatchNumber());
        event.setQuantity(domainToSharedQuantity(batch.getQuantity()));
        event.setExpirationDate(batch.getExpirationDate());
        event.setFifoOrder(fifoOrder);
        event.setTimestamp(batch.getReceivedAt());
        return event;
    }
}