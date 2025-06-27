package ai.shreds.shared.dtos;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.value_objects.SharedAddressValue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedWarehouseResponseDTO {

    private UUID id;

    private String code;

    private String name;

    private SharedAddressValue address;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}