package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedProductAttributeValueDTO {
    
    private UUID id;
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Attribute ID is required")
    private UUID attributeId;
    
    private SharedProductAttributeDTO attribute;
    
    private String textValue;
    
    private BigDecimal numericValue;
    
    private Boolean booleanValue;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateValue;
    
    @Builder.Default
    private List<SharedAttributeOptionDTO> selectedOptions = new ArrayList<>();
    
    @Builder.Default
    private Boolean isActive = true;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    private Long version;

    // TODO: Implementation of toEntity() and fromEntity() methods will be added
    // after domain layer is available for proper transformation
    public Object toEntity() {
        throw new UnsupportedOperationException("toEntity() method will be implemented when domain layer is available");
    }

    public static SharedProductAttributeValueDTO fromEntity(Object entity) {
        throw new UnsupportedOperationException("fromEntity() method will be implemented when domain layer is available");
    }
}