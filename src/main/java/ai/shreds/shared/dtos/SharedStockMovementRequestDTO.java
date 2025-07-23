package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
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
public class SharedStockMovementRequestDTO {

    @NotNull
    private UUID warehouseId;

    @NotNull
    private UUID productId;

    @NotNull
    private SharedStockMovementTypeEnum movementType;

    @NotNull
    private SharedQuantityValue quantity;

    private String referenceId;

    private SharedReferenceTypeEnum referenceType;

    @NotBlank
    @Size(max = 500)
    private String reason;

    @NotBlank
    private String performedBy;

    private SharedMoneyValue costPerUnit;

    private String batchNumber;
}