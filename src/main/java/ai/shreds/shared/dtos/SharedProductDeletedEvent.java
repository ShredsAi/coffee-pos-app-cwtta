package ai.shreds.shared.dtos;

import java.time.Instant;
import java.util.UUID;

public class SharedProductDeletedEvent {

    private UUID eventId;
    private String eventType = "PRODUCT_DELETED";
    private UUID productId;
    private String productName;
    private Instant timestamp;
    private String userId;

    public SharedProductDeletedEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    public SharedProductDeletedEvent(UUID eventId, UUID productId, String productName, Instant timestamp, String userId) {
        this.eventId = eventId;
        this.productId = productId;
        this.productName = productName;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public static SharedProductDeletedEvent fromProduct(UUID productId, String productName, String userId) {
        SharedProductDeletedEvent event = new SharedProductDeletedEvent();
        event.productId = productId;
        event.productName = productName;
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
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}