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
 * JPA entity representing the reservation table.
 */
@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureReservationJpaEntity {
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

    @Column(name = "reserved_for", nullable = false)
    private String reservedFor;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}