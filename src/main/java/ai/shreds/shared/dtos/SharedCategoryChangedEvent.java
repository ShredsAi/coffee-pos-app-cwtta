package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedCategoryChangedEvent {

    private UUID eventId;

    @Builder.Default
    private String eventType = "CATEGORY_CHANGED";

    private UUID categoryId;

    private String categoryName;

    private String changeType;

    private UUID parentCategoryId;

    private List<UUID> affectedProductIds;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String userId;

    public static SharedCategoryChangedEvent fromCategory(DomainCategoryEntity category,
                                                          String changeType,
                                                          List<UUID> affectedProducts,
                                                          String userId) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null when creating event");
        }
        return SharedCategoryChangedEvent.builder()
                .eventId(UUID.randomUUID())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .changeType(changeType)
                .parentCategoryId(category.getParentCategoryId())
                .affectedProductIds(affectedProducts)
                .userId(userId)
                .timestamp(Instant.now())
                .build();
    }
}
