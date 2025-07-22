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
public class InfrastructureEntityMapperFixed {

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
        
        InfrastructureWarehouseJpaEntity entity = new InfrastructureWarehouseJpaEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setPostalCode(address.getPostalCode());
        entity.setCountry(address.getCountry());
        entity.setIsActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
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
                jpa.getWarehouseId(),
                productId,
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
        
        InfrastructureInventoryItemJpaEntity entity = new InfrastructureInventoryItemJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setAvailableQty(domain.getAvailableQuantity().getValue());
        entity.setReservedQty(domain.getReservedQuantity().getValue());
        entity.setAllocatedQty(domain.getAllocatedQuantity().getValue());
        entity.setTotalQty(domain.getTotalQuantity().getValue());
        entity.setQtyUnit(domain.getAvailableQuantity().getUnit());
        entity.setSafetyStockLevel(domain.getSafetyStockLevel().getValue());
        entity.setReorderPoint(domain.getReorderPoint().getValue());
        entity.setLastMovementAt(domain.getLastMovementAt());
        entity.setVersion(domain.getVersion());
        return entity;
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
        
        // Use raw String for referenceType
        String referenceType = jpa.getReferenceType();
        
        return new DomainStockMovementEntity(
                jpa.getId(),
                jpa.getWarehouseId(),
                productId,
                DomainStockMovementTypeEnum.valueOf(jpa.getMovementType()),
                quantity,
                jpa.getReferenceId(),
                referenceType, // Pass string directly
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
        
        InfrastructureStockMovementJpaEntity entity = new InfrastructureStockMovementJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setMovementType(domain.getMovementType().name());
        entity.setQuantity(domain.getQuantity().getValue());
        entity.setQtyUnit(domain.getQuantity().getUnit());
        entity.setReferenceId(domain.getReferenceId());
        entity.setReferenceType(domain.getReferenceType()); // Get string directly
        entity.setBatchId(domain.getBatchId());
        entity.setReason(domain.getReason());
        entity.setPerformedBy(domain.getPerformedBy());
        entity.setPerformedAt(domain.getPerformedAt());
        entity.setCostPerUnit(domain.getCostPerUnit() != null ? domain.getCostPerUnit().getAmount() : null);
        entity.setCostCurrency(domain.getCostPerUnit() != null ? domain.getCostPerUnit().getCurrency() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
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
        
        InfrastructureBatchJpaEntity entity = new InfrastructureBatchJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setBatchNumber(domain.getBatchNumber());
        entity.setQuantity(domain.getQuantity().getValue());
        entity.setQtyUnit(domain.getQuantity().getUnit());
        entity.setManufacturingDate(domain.getManufacturingDate());
        entity.setExpirationDate(domain.getExpirationDate());
        entity.setReceivedAt(domain.getReceivedAt());
        entity.setSupplierId(domain.getSupplierId());
        entity.setVersion(domain.getVersion());
        return entity;
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
        
        InfrastructureReservationJpaEntity entity = new InfrastructureReservationJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setQuantity(domain.getQuantity().getValue());
        entity.setQtyUnit(domain.getQuantity().getUnit());
        entity.setReservedFor(domain.getReservedFor());
        entity.setStatus(domain.getStatus().name());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setVersion(domain.getVersion());
        return entity;
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
        
        InfrastructureAllocationJpaEntity entity = new InfrastructureAllocationJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setQuantity(domain.getQuantity().getValue());
        entity.setQtyUnit(domain.getQuantity().getUnit());
        entity.setAllocatedTo(domain.getAllocatedTo());
        entity.setStatus(domain.getStatus().name());
        entity.setBatchId(domain.getBatchId());
        entity.setAllocatedAt(domain.getAllocatedAt());
        entity.setExpectedShipmentAt(domain.getExpectedShipmentAt());
        return entity;
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
        
        InfrastructureLowStockAlertJpaEntity entity = new InfrastructureLowStockAlertJpaEntity();
        entity.setId(domain.getId());
        entity.setWarehouseId(domain.getWarehouseId());
        entity.setProductId(domain.getProductId().getValue());
        entity.setCurrentQuantity(domain.getCurrentQuantity().getValue());
        entity.setQtyUnit(domain.getCurrentQuantity().getUnit());
        entity.setSafetyStockLevel(domain.getSafetyStockLevel().getValue());
        entity.setSeverity(domain.getSeverity().name());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setAcknowledgedAt(domain.getAcknowledgedAt());
        entity.setAcknowledgedBy(domain.getAcknowledgedBy());
        entity.setResolvedAt(domain.getResolvedAt());
        return entity;
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
        
        InfrastructureEventOutboxJpaEntity entity = new InfrastructureEventOutboxJpaEntity();
        entity.setId(domain.getId());
        entity.setAggregateId(domain.getAggregateId());
        entity.setAggregateType(domain.getAggregateType());
        entity.setEventType(domain.getEventType());
        entity.setPayload(domain.getPayload());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setProcessed(domain.isProcessed());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }
}