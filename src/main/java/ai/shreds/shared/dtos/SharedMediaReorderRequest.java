package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class SharedMediaReorderRequest {
    
    @Valid
    @NotEmpty(message = "Media order list cannot be empty")
    @Builder.Default
    private List<SharedMediaOrderItem> mediaOrder = new ArrayList<>();
    
    // TODO: Implementation of toCommand() method will be added
    // after application layer is available for proper transformation
    public Object toCommand(UUID productId) {
        throw new UnsupportedOperationException("toCommand() method will be implemented when application layer is available");
    }
}