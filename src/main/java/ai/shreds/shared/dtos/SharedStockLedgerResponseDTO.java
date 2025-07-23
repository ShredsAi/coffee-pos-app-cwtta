package ai.shreds.shared.dtos;

import java.util.UUID;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedStockLedgerResponseDTO {

    private UUID warehouseId;
    private UUID productId;
    private SharedInventoryQuantitiesDTO currentQuantities;
    private List<SharedStockMovementResponseDTO> movements;
    private SharedPaginationDTO pagination;
}