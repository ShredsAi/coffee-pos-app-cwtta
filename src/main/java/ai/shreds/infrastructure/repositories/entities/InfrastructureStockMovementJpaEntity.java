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
 * JPA entity representing the stock_movement table for movement history.
 */
@Entity
@Table(
    name = "stock_movement",
    indexes = {
        @Index(columnList = "warehouse_id, product_id, performed_at DESC", name = "idx_movement_warehouse_product_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureStockMovementJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @Column(name = "product_id", nullable = false)
    private String productId;
    
    @Column(name = "movement_type", nullable = false)
    private String movementType;
    
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "qty_unit", nullable = false)
    private String qtyUnit;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "reference_type")
    private String referenceType;
    
    @Column(name = "batch_id")
    private UUID batchId;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
    
    @Column(name = "cost_per_unit", precision = 19, scale = 4)
    private BigDecimal costPerUnit;
    
    @Column(name = "cost_currency", length = 3)
    private String costCurrency;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}