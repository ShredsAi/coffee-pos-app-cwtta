package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainWarehouseEntity;
import ai.shreds.domain.entities.DomainInventoryItemEntity;
import ai.shreds.domain.entities.DomainStockMovementEntity;
import ai.shreds.domain.entities.DomainBatchEntity;
import ai.shreds.domain.entities.DomainReservationEntity;
import ai.shreds.domain.entities.DomainAllocationEntity;
import ai.shreds.domain.entities.DomainLowStockAlertEntity;
import ai.shreds.domain.entities.DomainEventOutboxEntity;
import ai.shreds.domain.value_objects.DomainAddressValue;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.enums.DomainStockMovementTypeEnum;
import ai.shreds.domain.enums.DomainReservationStatusEnum;
import ai.shreds.domain.enums.DomainAllocationStatusEnum;
import ai.shreds.domain.enums.DomainAlertSeverityEnum;
import ai.shreds.domain.enums.DomainAlertStatusEnum;
import ai.shreds.domain.enums.DomainReferenceTypeEnum;
import ai.shreds.infrastructure.repositories.entities.InfrastructureWarehouseJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureInventoryItemJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureStockMovementJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureBatchJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureReservationJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureAllocationJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureLowStockAlertJpaEntity;
import ai.shreds.infrastructure.repositories.entities.InfrastructureEventOutboxJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps between domain entities and JPA entities for persistence.
 * This class is deliberately implemented manually (instead of MapStruct) to stay
 * framework-agnostic inside the domain layer while keeping mapping logic here
 * in the infrastructure boundary.
 */
@Component
public class InfrastructureEntityMapper {

    // ──────────────────────────────────  Warehouse  ────────────────────────────

    public DomainWarehouseEntity toDomainWarehouse(InfrastructureWarehouseJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        DomainAddressValue address = new DomainAddressValue(
                jpa.getStreet(),
                jpa.getCity(),
                jpa.getState(),
                jpa.getPostalCode(),
                jpa.getCountry()
        );
        
        return new DomainWarehouseEntity(
                jpa.getId(),
                jpa.getCode(),
                jpa.getName(),
                address,
                Boolean.TRUE.equals(jpa.getIsActive()),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    public InfrastructureWarehouseJpaEntity toJpaWarehouse(DomainWarehouseEntity domain) {
        if (domain == null) {
            return null;
        }
        DomainAddressValue address = domain.getAddress();
        return InfrastructureWarehouseJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isActive(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    // ──────────────────────────────────  Inventory Item  ────────────────────────────

    public DomainInventoryItemEntity toDomainInventoryItem(InfrastructureInventoryItemJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue availableQuantity = new DomainQuantityValue(jpa.getAvailableQty(), jpa.getQtyUnit());
        DomainQuantityValue reservedQuantity = new DomainQuantityValue(jpa.getReservedQty(), jpa.getQtyUnit());
        DomainQuantityValue allocatedQuantity = new DomainQuantityValue(jpa.getAllocatedQty(), jpa.getQtyUnit());
        DomainQuantityValue totalQuantity = new DomainQuantityValue(jpa.getTotalQty(), jpa.getQtyUnit());
        DomainQuantityValue safetyStockLevel = new DomainQuantityValue(jpa.getSafetyStockLevel(), jpa.getQtyUnit());
        DomainQuantityValue reorderPoint = new DomainQuantityValue(jpa.getReorderPoint(), jpa.getQtyUnit());
        
        return new DomainInventoryItemEntity(
                jpa.getId(),
                productId,
                jpa.getWarehouseId(),
                availableQuantity,
                reservedQuantity,
                allocatedQuantity,
                totalQuantity,
                safetyStockLevel,
                reorderPoint,
                jpa.getLastMovementAt(),
                jpa.getVersion()
        );
    }

    public InfrastructureInventoryItemJpaEntity toJpaInventoryItem(DomainInventoryItemEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureInventoryItemJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .availableQty(domain.getAvailableQuantity().getValue())
                .reservedQty(domain.getReservedQuantity().getValue())
                .allocatedQty(domain.getAllocatedQuantity().getValue())
                .totalQty(domain.getTotalQuantity().getValue())
                .qtyUnit(domain.getAvailableQuantity().getUnit())
                .safetyStockLevel(domain.getSafetyStockLevel().getValue())
                .reorderPoint(domain.getReorderPoint().getValue())
                .lastMovementAt(domain.getLastMovementAt())
                .version(domain.getVersion())
                .build();
    }

    // ──────────────────────────────────  Stock Movement  ────────────────────────────

    public DomainStockMovementEntity toDomainStockMovement(InfrastructureStockMovementJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue quantity = new DomainQuantityValue(jpa.getQuantity(), jpa.getQtyUnit());
        DomainMoneyValue costPerUnit = jpa.getCostPerUnit() != null ? 
                new DomainMoneyValue(jpa.getCostPerUnit(), jpa.getCostCurrency()) : null;
        
        return new DomainStockMovementEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                DomainStockMovementTypeEnum.valueOf(jpa.getMovementType()),
                quantity,
                jpa.getReferenceId(),
                jpa.getReferenceType() != null ? DomainReferenceTypeEnum.valueOf(jpa.getReferenceType()) : null,
                jpa.getBatchId(),
                jpa.getReason(),
                jpa.getPerformedBy(),
                jpa.getPerformedAt(),
                costPerUnit,
                jpa.getCreatedAt()
        );
    }

    public InfrastructureStockMovementJpaEntity toJpaStockMovement(DomainStockMovementEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureStockMovementJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .movementType(domain.getMovementType().name())
                .quantity(domain.getQuantity().getValue())
                .qtyUnit(domain.getQuantity().getUnit())
                .referenceId(domain.getReferenceId())
                .referenceType(domain.getReferenceType() != null ? domain.getReferenceType().name() : null)
                .batchId(domain.getBatchId())
                .reason(domain.getReason())
                .performedBy(domain.getPerformedBy())
                .performedAt(domain.getPerformedAt())
                .costPerUnit(domain.getCostPerUnit() != null ? domain.getCostPerUnit().getAmount() : null)
                .costCurrency(domain.getCostPerUnit() != null ? domain.getCostPerUnit().getCurrency() : null)
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // ──────────────────────────────────  Batch  ────────────────────────────

    public DomainBatchEntity toDomainBatch(InfrastructureBatchJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue quantity = new DomainQuantityValue(jpa.getQuantity(), jpa.getQtyUnit());
        
        return new DomainBatchEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                jpa.getBatchNumber(),
                quantity,
                jpa.getManufacturingDate(),
                jpa.getExpirationDate(),
                jpa.getReceivedAt(),
                jpa.getSupplierId(),
                jpa.getVersion()
        );
    }

    public InfrastructureBatchJpaEntity toJpaBatch(DomainBatchEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureBatchJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .batchNumber(domain.getBatchNumber())
                .quantity(domain.getQuantity().getValue())
                .qtyUnit(domain.getQuantity().getUnit())
                .manufacturingDate(domain.getManufacturingDate())
                .expirationDate(domain.getExpirationDate())
                .receivedAt(domain.getReceivedAt())
                .supplierId(domain.getSupplierId())
                .version(domain.getVersion())
                .build();
    }

    // ──────────────────────────────────  Reservation  ────────────────────────────

    public DomainReservationEntity toDomainReservation(InfrastructureReservationJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue quantity = new DomainQuantityValue(jpa.getQuantity(), jpa.getQtyUnit());
        
        return new DomainReservationEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                quantity,
                jpa.getReservedFor(),
                DomainReservationStatusEnum.valueOf(jpa.getStatus()),
                jpa.getExpiresAt(),
                jpa.getCreatedAt(),
                jpa.getVersion()
        );
    }

    public InfrastructureReservationJpaEntity toJpaReservation(DomainReservationEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureReservationJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .quantity(domain.getQuantity().getValue())
                .qtyUnit(domain.getQuantity().getUnit())
                .reservedFor(domain.getReservedFor())
                .status(domain.getStatus().name())
                .expiresAt(domain.getExpiresAt())
                .createdAt(domain.getCreatedAt())
                .version(domain.getVersion())
                .build();
    }

    // ──────────────────────────────────  Allocation  ────────────────────────────

    public DomainAllocationEntity toDomainAllocation(InfrastructureAllocationJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue quantity = new DomainQuantityValue(jpa.getQuantity(), jpa.getQtyUnit());
        
        return new DomainAllocationEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                quantity,
                jpa.getAllocatedTo(),
                DomainAllocationStatusEnum.valueOf(jpa.getStatus()),
                jpa.getBatchId(),
                jpa.getAllocatedAt(),
                jpa.getExpectedShipmentAt()
        );
    }

    public InfrastructureAllocationJpaEntity toJpaAllocation(DomainAllocationEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureAllocationJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .quantity(domain.getQuantity().getValue())
                .qtyUnit(domain.getQuantity().getUnit())
                .allocatedTo(domain.getAllocatedTo())
                .status(domain.getStatus().name())
                .batchId(domain.getBatchId())
                .allocatedAt(domain.getAllocatedAt())
                .expectedShipmentAt(domain.getExpectedShipmentAt())
                .build();
    }

    // ──────────────────────────────────  Low Stock Alert  ────────────────────────────

    public DomainLowStockAlertEntity toDomainLowStockAlert(InfrastructureLowStockAlertJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        DomainProductIdValue productId = new DomainProductIdValue(jpa.getProductId());
        DomainQuantityValue currentQuantity = new DomainQuantityValue(jpa.getCurrentQuantity(), jpa.getQtyUnit());
        DomainQuantityValue safetyStockLevel = new DomainQuantityValue(jpa.getSafetyStockLevel(), jpa.getQtyUnit());
        
        return new DomainLowStockAlertEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                currentQuantity,
                safetyStockLevel,
                DomainAlertSeverityEnum.valueOf(jpa.getSeverity()),
                DomainAlertStatusEnum.valueOf(jpa.getStatus()),
                jpa.getCreatedAt(),
                jpa.getAcknowledgedAt(),
                jpa.getAcknowledgedBy(),
                jpa.getResolvedAt()
        );
    }

    public InfrastructureLowStockAlertJpaEntity toJpaLowStockAlert(DomainLowStockAlertEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureLowStockAlertJpaEntity.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .productId(domain.getProductId().getValue())
                .currentQuantity(domain.getCurrentQuantity().getValue())
                .qtyUnit(domain.getCurrentQuantity().getUnit())
                .safetyStockLevel(domain.getSafetyStockLevel().getValue())
                .severity(domain.getSeverity().name())
                .status(domain.getStatus().name())
                .createdAt(domain.getCreatedAt())
                .acknowledgedAt(domain.getAcknowledgedAt())
                .acknowledgedBy(domain.getAcknowledgedBy())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }

    // ──────────────────────────────────  Event Outbox  ────────────────────────────

    public DomainEventOutboxEntity toDomainEventOutbox(InfrastructureEventOutboxJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        return new DomainEventOutboxEntity(
                jpa.getId(),
                jpa.getAggregateId(),
                jpa.getAggregateType(),
                jpa.getEventType(),
                jpa.getPayload(),
                jpa.getCreatedAt(),
                jpa.getProcessed(),
                jpa.getProcessedAt()
        );
    }

    public InfrastructureEventOutboxJpaEntity toJpaEventOutbox(DomainEventOutboxEntity domain) {
        if (domain == null) {
            return null;
        }
        
        return InfrastructureEventOutboxJpaEntity.builder()
                .id(domain.getId())
                .aggregateId(domain.getAggregateId())
                .aggregateType(domain.getAggregateType())
                .eventType(domain.getEventType())
                .payload(domain.getPayload())
                .createdAt(domain.getCreatedAt())
                .processed(domain.isProcessed())
                .processedAt(domain.getProcessedAt())
                .build();
    }
}
