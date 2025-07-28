package ai.shreds.shared.dtos;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import ai.shreds.domain.entities.DomainProductEntity;

public class SharedProductUpdatedEvent {

    private UUID eventId;
    private String eventType = "PRODUCT_UPDATED";
    private UUID productId;
    private String productName;
    private Map<String, Object> changes;
    private Instant timestamp;
    private String userId;

    public SharedProductUpdatedEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    public SharedProductUpdatedEvent(UUID eventId, UUID productId, String productName, Map<String, Object> changes, Instant timestamp, String userId) {
        this.eventId = eventId;
        this.productId = productId;
        this.productName = productName;
        this.changes = changes;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public static SharedProductUpdatedEvent fromProduct(DomainProductEntity product, Map<String, Object> changes, String userId) {
        SharedProductUpdatedEvent event = new SharedProductUpdatedEvent();
        event.productId = product.getId();
        event.productName = product.getName();
        event.changes = changes;
        event.userId = userId;
        return event;
    }

    // Getters and setters
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Map<String, Object> getChanges() { return changes; }
    public void setChanges(Map<String, Object> changes) { this.changes = changes; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}