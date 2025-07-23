package ai.shreds.application.ports;

import java.util.UUID;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;

public interface ApplicationInventoryQueryInputPort {

    SharedInventoryItemResponseDTO getItem(UUID warehouseId, UUID productId);
}