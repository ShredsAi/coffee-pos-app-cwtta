package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationStockMovementInputPort;
import ai.shreds.shared.dtos.SharedStockMovementRequestDTO;
import ai.shreds.shared.dtos.SharedStockMovementResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stock-movements")
@Validated
public class AdapterStockMovementController {

    private final ApplicationStockMovementInputPort applicationStockMovementService;

    public AdapterStockMovementController(ApplicationStockMovementInputPort applicationStockMovementService) {
        this.applicationStockMovementService = applicationStockMovementService;
    }

    @PostMapping
    public ResponseEntity<SharedStockMovementResponseDTO> createStockMovement(
            @Valid @RequestBody SharedStockMovementRequestDTO request) {
        SharedStockMovementResponseDTO response = applicationStockMovementService.processMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
