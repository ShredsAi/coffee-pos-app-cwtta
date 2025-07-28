package ai.shreds.shared.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ai.shreds.domain.entities.DomainProductEntity;
import ai.shreds.domain.entities.DomainProductCategoryEntity;

public class SharedProductCreatedEvent {

    private UUID eventId;
    private String eventType = "PRODUCT_CREATED";
    private UUID productId;
    private String productName;
    private List<UUID> categoryIds;
    private Instant timestamp;
    private String userId;

    public SharedProductCreatedEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    public SharedProductCreatedEvent(UUID eventId, UUID productId, String productName, List<UUID> categoryIds, Instant timestamp, String userId) {
        this.eventId = eventId;
        this.productId = productId;
        this.productName = productName;
        this.categoryIds = categoryIds;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public static SharedProductCreatedEvent fromProduct(DomainProductEntity product, String userId) {
        SharedProductCreatedEvent event = new SharedProductCreatedEvent();
        event.productId = product.getId();
        event.productName = product.getName();
        event.categoryIds = product.getCategories().stream()
            .map(DomainProductCategoryEntity::getCategoryId)
            .collect(Collectors.toList());
        event.userId = userId;
        return event;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public List<UUID> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<UUID> categoryIds) { this.categoryIds = categoryIds; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}