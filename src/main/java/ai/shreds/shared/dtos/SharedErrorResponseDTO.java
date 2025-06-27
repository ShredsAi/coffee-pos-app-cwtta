package ai.shreds.shared.dtos;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedErrorResponseDTO {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private LocalDateTime timestamp;
    private List<SharedFieldErrorDTO> violations;
}
