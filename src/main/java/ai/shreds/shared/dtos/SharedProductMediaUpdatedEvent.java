package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedMediaType;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for events indicating an update to a product's media.
 */
public class SharedProductMediaUpdatedEvent {
    private final UUID eventId;
    private final String eventType = "MEDIA_UPDATED";
    private final Instant timestamp;
    private UUID mediaId;
    private UUID productId;
    private String changeType;
    private SharedMediaType mediaType;
    private String url;
    private String userId;

    public SharedProductMediaUpdatedEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public UUID getMediaId() { return mediaId; }
    public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public SharedMediaType getMediaType() { return mediaType; }
    public void setMediaType(SharedMediaType mediaType) { this.mediaType = mediaType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}