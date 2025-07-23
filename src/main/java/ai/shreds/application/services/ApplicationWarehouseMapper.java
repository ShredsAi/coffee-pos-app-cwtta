package ai.shreds.application.services;

import org.springframework.stereotype.Component;
import ai.shreds.shared.dtos.SharedWarehouseResponseDTO;
import ai.shreds.shared.dtos.SharedWarehouseRequestDTO;
import ai.shreds.shared.dtos.SharedWarehouseStatusChangedEventDTO;
import ai.shreds.shared.value_objects.SharedAddressValue;
import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.commands.DomainCreateWarehouseCommand;
import ai.shreds.domain.value_objects.DomainAddressValue;

@Component
public class ApplicationWarehouseMapper {

    public SharedWarehouseResponseDTO toResponseDTO(DomainWarehouseEntity warehouse) {
        if (warehouse == null) {
            return null;
        }
        SharedWarehouseResponseDTO dto = new SharedWarehouseResponseDTO();
        dto.setId(warehouse.getId());
        dto.setCode(warehouse.getCode());
        dto.setName(warehouse.getName());
        dto.setAddress(toSharedAddress(warehouse.getAddress()));
        dto.setIsActive(warehouse.isActive());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        return dto;
    }

    public DomainCreateWarehouseCommand toDomainCommand(SharedWarehouseRequestDTO request) {
        if (request == null) {
            return null;
        }
        DomainAddressValue address = new DomainAddressValue(
            request.getAddress().getStreet(),
            request.getAddress().getCity(),
            request.getAddress().getState(),
            request.getAddress().getPostalCode(),
            request.getAddress().getCountry()
        );
        return new DomainCreateWarehouseCommand(
            request.getCode(),
            request.getName(),
            address,
            request.getIsActive()
        );
    }

    public SharedWarehouseStatusChangedEventDTO toEventDTO(DomainWarehouseEntity warehouse, boolean oldStatus, boolean newStatus) {
        if (warehouse == null) {
            return null;
        }
        SharedWarehouseStatusChangedEventDTO event = new SharedWarehouseStatusChangedEventDTO();
        event.setWarehouseId(warehouse.getId());
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setTimestamp(warehouse.getUpdatedAt());
        return event;
    }
    
    private SharedAddressValue toSharedAddress(DomainAddressValue domainAddress) {
        if (domainAddress == null) {
            return null;
        }
        return new SharedAddressValue(
            domainAddress.getStreet(),
            domainAddress.getCity(),
            domainAddress.getState(),
            domainAddress.getPostalCode(),
            domainAddress.getCountry()
        );
    }
}