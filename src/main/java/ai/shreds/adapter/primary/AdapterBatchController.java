package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationBatchInputPort;
import ai.shreds.shared.dtos.SharedBatchRequestDTO;
import ai.shreds.shared.dtos.SharedBatchResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/batches")
@Validated
public class AdapterBatchController {

    private final ApplicationBatchInputPort applicationBatchService;

    public AdapterBatchController(ApplicationBatchInputPort applicationBatchService) {
        this.applicationBatchService = applicationBatchService;
    }

    @PostMapping
    public ResponseEntity<SharedBatchResponseDTO> createBatch(
            @Valid @RequestBody SharedBatchRequestDTO request) {
        SharedBatchResponseDTO response = applicationBatchService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
