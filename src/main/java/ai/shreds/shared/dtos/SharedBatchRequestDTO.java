package ai.shreds.shared.dtos;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.value_objects.SharedQuantityValue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBatchRequestDTO {

    @NotNull
    private UUID warehouseId;

    @NotNull
    private UUID productId;

    @NotBlank
    private String batchNumber;

    @NotNull
    private SharedQuantityValue quantity;

    private LocalDate manufacturingDate;

    private LocalDate expirationDate;

    private UUID supplierId;
}