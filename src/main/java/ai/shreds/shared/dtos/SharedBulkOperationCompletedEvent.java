package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedBulkOperationCompletedEvent {
    
    private String eventType;
    
    private UUID operationId;
    
    private String operationType;
    
    private List<UUID> affectedProductIds;
    
    private Instant timestamp;
    
    private String userId;
    
    private Boolean success;
    
    private String message;
}