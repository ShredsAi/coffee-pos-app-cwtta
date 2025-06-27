package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationStockLedgerInputPort;
import ai.shreds.shared.dtos.SharedStockLedgerResponseDTO;
import ai.shreds.shared.value_objects.SharedStockLedgerQueryParams;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@RestController
@RequestMapping("/api/stock-ledger")
@Validated
public class AdapterStockLedgerController {

    private final ApplicationStockLedgerInputPort applicationStockLedgerService;

    public AdapterStockLedgerController(ApplicationStockLedgerInputPort applicationStockLedgerService) {
        this.applicationStockLedgerService = applicationStockLedgerService;
    }

    @GetMapping("/{warehouseId}/{productId}")
    public ResponseEntity<SharedStockLedgerResponseDTO> getStockLedger(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId,
            @Valid @ModelAttribute SharedStockLedgerQueryParams queryParams) {
        SharedStockLedgerResponseDTO response = applicationStockLedgerService.getStockLedger(warehouseId, productId, queryParams);
        return ResponseEntity.ok(response);
    }
}
