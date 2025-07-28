package ai.shreds.shared.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Alternative event class to test if the issue is file-name specific.
 */
public class SharedAttributeChangeEvent {
    
    private UUID eventId;
    private String eventType = "ATTRIBUTE_CHANGED";
    private UUID attributeId;
    private String attributeCode;
    private String changeType;
    private List<UUID> affectedProductIds;
    private Instant timestamp;
    private String userId;
    
    public SharedAttributeChangeEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public UUID getAttributeId() { return attributeId; }
    public void setAttributeId(UUID attributeId) { this.attributeId = attributeId; }
    public String getAttributeCode() { return attributeCode; }
    public void setAttributeCode(String attributeCode) { this.attributeCode = attributeCode; }
}