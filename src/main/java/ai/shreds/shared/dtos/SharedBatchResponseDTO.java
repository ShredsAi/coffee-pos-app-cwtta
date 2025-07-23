package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.value_objects.SharedQuantityValue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBatchResponseDTO {

    private UUID id;
    private UUID warehouseId;
    private UUID productId;
    private String batchNumber;
    private SharedQuantityValue quantity;
    private LocalDate manufacturingDate;
    private LocalDate expirationDate;
    private LocalDateTime receivedAt;
    private UUID supplierId;
    private Long version;
}