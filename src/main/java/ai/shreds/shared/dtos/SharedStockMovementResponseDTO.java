package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;
import ai.shreds.shared.value_objects.SharedQuantityValue;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import ai.shreds.shared.enums.SharedReferenceTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedStockMovementResponseDTO {

    private UUID movementId;

    private UUID warehouseId;

    private UUID productId;

    private SharedStockMovementTypeEnum movementType;

    private SharedQuantityValue quantity;

    private String referenceId;

    private SharedReferenceTypeEnum referenceType;

    private UUID batchId;

    private String reason;

    private String performedBy;

    private LocalDateTime performedAt;

    private SharedMoneyValue costPerUnit;

    private LocalDateTime createdAt;
}