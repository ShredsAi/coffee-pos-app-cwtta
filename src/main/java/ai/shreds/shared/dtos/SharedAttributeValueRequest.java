package ai.shreds.shared.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedAttributeValueRequest {
    
    @NotNull(message = "Attribute ID is required")
    private UUID attributeId;
    
    private String textValue;
    
    private BigDecimal numericValue;
    
    private Boolean booleanValue;
    
    private LocalDate dateValue;
    
    @Builder.Default
    private List<UUID> selectedOptionIds = new ArrayList<>();
}