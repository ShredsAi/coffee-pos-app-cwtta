package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationReorderMediaCommand {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @Valid
    @NotEmpty(message = "Media order list cannot be empty")
    @Builder.Default
    private List<SharedMediaOrderItem> mediaOrder = new ArrayList<>();
    
    // TODO: Implementation of toDomainCommand() method will be added
    // after domain layer is available for proper transformation
    public Object toDomainCommand() {
        throw new UnsupportedOperationException("toDomainCommand() method will be implemented when domain layer is available");
    }
    
    public static ApplicationReorderMediaCommand fromRequest(UUID productId, SharedMediaReorderRequest request) {
        if (request == null) {
            return null;
        }
        
        return ApplicationReorderMediaCommand.builder()
                .productId(productId)
                .mediaOrder(request.getMediaOrder())
                .build();
    }
}