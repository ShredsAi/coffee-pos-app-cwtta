package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing the allocation table.
 */
@Entity
@Table(name = "allocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureAllocationJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;

    @Column(name = "qty_unit", length = 20, nullable = false)
    private String qtyUnit;

    @Column(name = "allocated_to", nullable = false)
    private String allocatedTo;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "expected_shipment_at")
    private LocalDateTime expectedShipmentAt;
}