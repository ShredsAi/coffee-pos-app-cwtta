package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedSkuStatusChangedEvent {
    
    private String eventType;
    
    private UUID productId;
    
    private String skuStatus;
    
    private Instant timestamp;
    
    private String userId;
}