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
 * JPA entity representing the inventory_item table.
 */
@Entity
@Table(
    name = "inventory_item",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}, name = "idx_inventory_item_composite")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureInventoryItemJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @Column(name = "product_id", nullable = false)
    private String productId;
    
    @Column(name = "available_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal availableQty;
    
    @Column(name = "reserved_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal reservedQty;
    
    @Column(name = "allocated_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal allocatedQty;
    
    @Column(name = "total_qty", nullable = false, precision = 19, scale = 6)
    private BigDecimal totalQty;
    
    @Column(name = "qty_unit", length = 20, nullable = false)
    private String qtyUnit;
    
    @Column(name = "safety_stock_level", nullable = false, precision = 19, scale = 6)
    private BigDecimal safetyStockLevel;
    
    @Column(name = "reorder_point", nullable = false, precision = 19, scale = 6)
    private BigDecimal reorderPoint;
    
    @Column(name = "last_movement_at")
    private LocalDateTime lastMovementAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}