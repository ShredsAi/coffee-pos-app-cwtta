package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainOptionCommand {

    @NotBlank(message = "Option value is required")
    @Size(max = 255, message = "Option value must not exceed 255 characters")
    private String value;

    @NotBlank(message = "Option code is required")
    @Size(max = 50, message = "Option code must not exceed 50 characters")
    private String code;

    @PositiveOrZero(message = "Sort order must be zero or positive")
    @Builder.Default
    private Integer sortOrder = 0;
}