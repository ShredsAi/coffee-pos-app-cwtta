package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationInventoryQueryInputPort;
import ai.shreds.shared.dtos.SharedInventoryItemResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-items")
public class AdapterInventoryItemController {

    private final ApplicationInventoryQueryInputPort applicationInventoryQueryService;

    public AdapterInventoryItemController(ApplicationInventoryQueryInputPort applicationInventoryQueryService) {
        this.applicationInventoryQueryService = applicationInventoryQueryService;
    }

    @GetMapping("/{warehouseId}/{productId}")
    public ResponseEntity<SharedInventoryItemResponseDTO> getInventoryItem(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId) {
        SharedInventoryItemResponseDTO response = applicationInventoryQueryService.getItem(warehouseId, productId);
        return ResponseEntity.ok(response);
    }
}