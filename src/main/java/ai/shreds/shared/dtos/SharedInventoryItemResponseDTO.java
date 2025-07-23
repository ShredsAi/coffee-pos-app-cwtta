package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.dtos.SharedInventoryQuantitiesDTO;
import ai.shreds.shared.dtos.SharedInventoryThresholdsDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedInventoryItemResponseDTO {

    private UUID id;
    private UUID warehouseId;
    private UUID productId;
    private SharedInventoryQuantitiesDTO quantities;
    private SharedInventoryThresholdsDTO thresholds;
    private LocalDateTime lastMovementAt;
    private Long version;
}