package ai.shreds.application.services;

import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.commands.DomainStockMovementCommand;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.enums.DomainReferenceTypeEnum;
import ai.shreds.shared.dtos.SharedStockMovementRequestDTO;
import ai.shreds.shared.dtos.SharedStockMovementResponseDTO;
import ai.shreds.shared.dtos.SharedStockMovementCreatedEventDTO;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;
import ai.shreds.shared.enums.SharedReferenceTypeEnum;
import ai.shreds.shared.value_objects.SharedQuantityValue;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Mapper(componentModel = "spring")
public interface ApplicationStockMovementMapper {

    @Mapping(target = "productId", source = "productId", qualifiedByName = "stringToDomainProductId")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "sharedToDomainQuantity")
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "sharedToDomainMoney")
    @Mapping(target = "movementType", source = "movementType", qualifiedByName = "sharedToDomainMovementType")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "sharedToDomainReferenceType")
    DomainStockMovementCommand toDomainCommand(SharedStockMovementRequestDTO request);

    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "domainToSharedQuantity")
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "domainToSharedMoney")
    @Mapping(target = "movementType", source = "movementType", qualifiedByName = "domainToSharedMovementType")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "domainToSharedReferenceType")
    @Mapping(target = "movementId", source = "id")
    SharedStockMovementResponseDTO toResponseDTO(DomainStockMovementEntity movement);

    @Mapping(target = "movementId", source = "id")
    @Mapping(target = "productId", source = "productId.value")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "domainToSharedQuantity")
    @Mapping(target = "movementType", source = "movementType", qualifiedByName = "domainToSharedMovementType")
    @Mapping(target = "timestamp", source = "performedAt")
    @Mapping(target = "updatedQuantities", ignore = true) // Set separately in service
    SharedStockMovementCreatedEventDTO toEventDTO(DomainStockMovementEntity movement);

    // Custom mapping methods
    @Named("stringToDomainProductId")
    default DomainProductIdValue stringToDomainProductId(UUID productId) {
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

    @Named("sharedToDomainMoney")
    default DomainMoneyValue sharedToDomainMoney(SharedMoneyValue shared) {
        return shared != null ? new DomainMoneyValue(shared.getAmount(), shared.getCurrency()) : null;
    }

    @Named("domainToSharedMoney")
    default SharedMoneyValue domainToSharedMoney(DomainMoneyValue domain) {
        return domain != null ? new SharedMoneyValue(domain.getAmount(), domain.getCurrency()) : null;
    }

    @Named("sharedToDomainMovementType")
    default DomainStockMovementTypeEnum sharedToDomainMovementType(SharedStockMovementTypeEnum shared) {
        if (shared == null) return null;
        switch (shared) {
            case INBOUND: return DomainStockMovementTypeEnum.INBOUND;
            case OUTBOUND: return DomainStockMovementTypeEnum.OUTBOUND;
            case TRANSFER: return DomainStockMovementTypeEnum.TRANSFER;
            case ADJUSTMENT: return DomainStockMovementTypeEnum.ADJUSTMENT;
            default: throw new IllegalArgumentException("Unknown movement type: " + shared);
        }
    }

    @Named("domainToSharedMovementType")
    default SharedStockMovementTypeEnum domainToSharedMovementType(DomainStockMovementTypeEnum domain) {
        if (domain == null) return null;
        switch (domain) {
            case INBOUND: return SharedStockMovementTypeEnum.INBOUND;
            case OUTBOUND: return SharedStockMovementTypeEnum.OUTBOUND;
            case TRANSFER: return SharedStockMovementTypeEnum.TRANSFER;
            case ADJUSTMENT: return SharedStockMovementTypeEnum.ADJUSTMENT;
            default: throw new IllegalArgumentException("Unknown movement type: " + domain);
        }
    }

    @Named("sharedToDomainReferenceType")
    default DomainReferenceTypeEnum sharedToDomainReferenceType(SharedReferenceTypeEnum shared) {
        if (shared == null) return null;
        switch (shared) {
            case PURCHASE_ORDER: return DomainReferenceTypeEnum.PURCHASE_ORDER;
            case SALES_ORDER: return DomainReferenceTypeEnum.SALES_ORDER;
            case TRANSFER_ORDER: return DomainReferenceTypeEnum.TRANSFER_ORDER;
            case RETURN_ORDER: return DomainReferenceTypeEnum.RETURN_ORDER;
            case ADJUSTMENT_ORDER: return DomainReferenceTypeEnum.ADJUSTMENT_ORDER;
            default: throw new IllegalArgumentException("Unknown reference type: " + shared);
        }
    }

    @Named("domainToSharedReferenceType")
    default SharedReferenceTypeEnum domainToSharedReferenceType(DomainReferenceTypeEnum domain) {
        if (domain == null) return null;
        switch (domain) {
            case PURCHASE_ORDER: return SharedReferenceTypeEnum.PURCHASE_ORDER;
            case SALES_ORDER: return SharedReferenceTypeEnum.SALES_ORDER;
            case TRANSFER_ORDER: return SharedReferenceTypeEnum.TRANSFER_ORDER;
            case RETURN_ORDER: return SharedReferenceTypeEnum.RETURN_ORDER;
            case ADJUSTMENT_ORDER: return SharedReferenceTypeEnum.ADJUSTMENT_ORDER;
            default: throw new IllegalArgumentException("Unknown reference type: " + domain);
        }
    }
}