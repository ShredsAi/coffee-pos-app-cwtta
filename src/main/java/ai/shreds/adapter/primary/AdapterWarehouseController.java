package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationWarehouseInputPort;
import ai.shreds.shared.dtos.SharedWarehouseRequestDTO;
import ai.shreds.shared.dtos.SharedWarehouseResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/warehouses")
@Validated
public class AdapterWarehouseController {

    private final ApplicationWarehouseInputPort applicationWarehouseService;

    public AdapterWarehouseController(ApplicationWarehouseInputPort applicationWarehouseService) {
        this.applicationWarehouseService = applicationWarehouseService;
    }

    @PostMapping
    public ResponseEntity<SharedWarehouseResponseDTO> createWarehouse(
            @Valid @RequestBody SharedWarehouseRequestDTO request) {
        SharedWarehouseResponseDTO response = applicationWarehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}