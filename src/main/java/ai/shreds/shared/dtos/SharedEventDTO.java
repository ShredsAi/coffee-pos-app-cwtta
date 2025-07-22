package ai.shreds.shared.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all domain events in the system.
 * Contains common metadata for event tracking and processing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SharedEventDTO {

    @NotNull(message = "Event ID must not be null")
    private UUID eventId;
    
    @NotBlank(message = "Event type must not be blank")
    private String eventType;
    
    @NotNull(message = "Aggregate ID must not be null")
    private UUID aggregateId;
    
    @NotBlank(message = "Aggregate type must not be blank")
    private String aggregateType;
    
    @NotNull(message = "Occurred at timestamp must not be null")
    private LocalDateTime occurredAt;
    
    /**
     * Creates a new event with auto-generated event ID and current timestamp.
     *
     * @param eventType The type of the event
     * @param aggregateId The ID of the aggregate that generated the event
     * @param aggregateType The type of the aggregate
     * @return A new event DTO with generated values
     */
    public static SharedEventDTO create(String eventType, UUID aggregateId, String aggregateType) {
        return SharedEventDTO.builder()
            .eventId(UUID.randomUUID())
            .eventType(eventType)
            .aggregateId(aggregateId)
            .aggregateType(aggregateType)
            .occurredAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Gets the event type in a format suitable for message headers.
     *
     * @return The normalized event type string
     */
    public String getEventTypeName() {
        return eventType.toLowerCase().replace(" ", "_");
    }
    
    /**
     * Gets the event age from its occurrence time.
     *
     * @return The number of seconds since the event occurred
     */
    public long getAgeInSeconds() {
        return java.time.Duration.between(occurredAt, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Checks if this is a recent event (within the last minute).
     *
     * @return true if the event occurred within the last minute
     */
    public boolean isRecent() {
        return getAgeInSeconds() < 60;
    }
}