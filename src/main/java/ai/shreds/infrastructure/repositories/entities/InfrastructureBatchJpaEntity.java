package ai.shreds.infrastructure.repositories.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing the batch table.
 */
@Entity
@Table(
    name = "batch",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "product_id", "batch_number"}, name = "idx_batch_warehouse_product_number")
    },
    indexes = {
        @Index(columnList = "warehouse_id, product_id", name = "idx_batch_warehouse_product"),
        @Index(columnList = "warehouse_id, product_id, received_at", name = "idx_batch_fifo")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfrastructureBatchJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @Column(name = "product_id", nullable = false)
    private String productId;
    
    @Column(name = "batch_number", length = 64, nullable = false)
    private String batchNumber;
    
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "qty_unit", length = 20, nullable = false)
    private String qtyUnit;
    
    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;
    
    @Column(name = "expiration_date")
    private LocalDate expirationDate;
    
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
    
    @Column(name = "supplier_id")
    private UUID supplierId;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}