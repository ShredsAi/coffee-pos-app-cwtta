package ai.shreds.shared.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for events indicating an update to a product attribute.
 */
public class SharedProductAttributeUpdatedEvent {
    private final UUID eventId;
    private final String eventType = "ATTRIBUTE_UPDATED";
    private final Instant timestamp;
    private UUID attributeId;
    private String attributeCode;
    private String changeType;
    private List<UUID> affectedProductIds;
    private String userId;

    public SharedProductAttributeUpdatedEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public UUID getAttributeId() { return attributeId; }
    public void setAttributeId(UUID attributeId) { this.attributeId = attributeId; }
    public String getAttributeCode() { return attributeCode; }
    public void setAttributeCode(String attributeCode) { this.attributeCode = attributeCode; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public List<UUID> getAffectedProductIds() { return affectedProductIds; }
    public void setAffectedProductIds(List<UUID> affectedProductIds) { this.affectedProductIds = affectedProductIds; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}